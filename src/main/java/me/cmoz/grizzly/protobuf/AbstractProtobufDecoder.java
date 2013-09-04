/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.cmoz.grizzly.protobuf;

import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import org.glassfish.grizzly.AbstractTransformer;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.TransformationException;
import org.glassfish.grizzly.TransformationResult;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.attributes.AttributeStorage;

import java.io.IOException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static org.glassfish.grizzly.TransformationResult.createErrorResult;

/**
 * Decodes Protocol Buffer messages from the input stream.
 */
@Slf4j
abstract class AbstractProtobufDecoder extends AbstractTransformer<Buffer, MessageLite> {

    /** The result of runtime detection for the protobuf 2.5.+ parser. */
    private static final boolean PROTOBUF_PARSER;

    static {
        boolean hasParser = false;
        try {
            MessageLite.class.getDeclaredMethod("getParserForType");
            hasParser = true;
        } catch (final Throwable t) {
            log.info("Upgrade to protobuf version 2.5.+ for enhanced parsing.");
        } finally {
            PROTOBUF_PARSER = hasParser;
        }
    }

    /** The error code for a failed protobuf parse of a message. */
    public static final int IO_PROTOBUF_PARSE_ERROR = 0;
    /** The error code for a malformed header. */
    public static final int IO_HEADER_ENCODING_ERROR = 1;
    /** The name of the decoder attribute for the size of the message. */
    public static final String MESSAGE_LENGTH_ATTR =
            "grizzly-protobuf-message-length";

    /** The base protocol buffers serialization unit. */
    private final MessageLite prototype;
    /** A table of known extensions, searchable by name or field number. */
    private final ExtensionRegistryLite extensionRegistry;
    /** The attribute for the length of the message. */
    private final Attribute<Integer> messageLengthAttr;

    /**
     * A Protocol Buffers decoder, with (optional) registered extensions.
     *
     * @param prototype The base protocol buffers serialization unit.
     * @param extensionRegistry A table of known extensions, searchable by name
     *                          or field number, may be {@code null}.
     */
    public AbstractProtobufDecoder(
            final @NonNull MessageLite prototype,
            final ExtensionRegistryLite extensionRegistry) {
        this.prototype = prototype;
        this.extensionRegistry = extensionRegistry;
        messageLengthAttr = attributeBuilder.createAttribute(MESSAGE_LENGTH_ATTR);
    }

    /**
     * Reads the header from the supplied {@code input}.
     *
     * @param input The input buffer to read the header from.
     * @return The size of the protobuf message to parse.
     * @throws IOException If the header could not be read.
     */
    public abstract int readHeader(final Buffer input)
            throws IOException;

    /** {@inheritDoc} */
    @Override
    protected final TransformationResult<Buffer, MessageLite> transformImpl(
            final AttributeStorage storage, final @NonNull Buffer input)
            throws TransformationException {
        log.debug("inputRemaining={}", input.remaining());

        Integer messageLength = messageLengthAttr.get(storage);
        if (messageLength == null) {
            try {
                messageLength = readHeader(input);
                log.debug("messageLength={}", messageLength);
                messageLengthAttr.set(storage, messageLength);
            } catch (final IOException e) {
                final String msg = "Error finding varint32 header size.";
                log.warn(msg, e);
                return createErrorResult(IO_HEADER_ENCODING_ERROR, msg);
            }
            log.debug("inputRemaining={}", input.remaining());
        }

        if (input.remaining() < messageLength) {
            return TransformationResult.createIncompletedResult(input);
        }

        final MessageLite message;
        try {
            final byte[] buf = input.array();
            final int pos = input.position();

            if (extensionRegistry != null) {
                if (PROTOBUF_PARSER) {
                    message = prototype.getParserForType()
                            .parseFrom(buf, pos, messageLength, extensionRegistry);
                } else {
                    message = prototype.newBuilderForType()
                            .mergeFrom(buf, pos, messageLength, extensionRegistry)
                            .build();
                }
            } else {
                if (PROTOBUF_PARSER) {
                    message = prototype.getParserForType()
                            .parseFrom(buf, pos, messageLength);
                } else {
                    message = prototype.newBuilderForType()
                            .mergeFrom(buf, pos, messageLength).build();
                }
            }
        } catch (final InvalidProtocolBufferException e) {
            final String msg = "Error decoding protobuf message from input stream.";
            log.warn(msg, e);
            return createErrorResult(IO_PROTOBUF_PARSE_ERROR, msg);
        }
        log.debug("inputRemaining={}", input.remaining());

        return TransformationResult.createCompletedResult(message, input);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AbstractProtobufDecoder.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean hasInputRemaining(
            final AttributeStorage storage, final Buffer input) {
        return (input != null) && input.hasRemaining();
    }

}
