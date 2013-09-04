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
import org.glassfish.grizzly.utils.BufferInputStream;

import java.nio.ByteBuffer;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static org.glassfish.grizzly.TransformationResult.createCompletedResult;
import static org.glassfish.grizzly.TransformationResult.createErrorResult;
import static org.glassfish.grizzly.TransformationResult.createIncompletedResult;

/**
 * Decodes Protocol Buffers messages from the input stream using a fixed header
 * to determine the size of a message.
 */
@Slf4j
public class FixedLengthProtobufDecoder extends AbstractTransformer<Buffer, MessageLite> {

    /** The error code for a failed protobuf parse of a message. */
    public static final int IO_PROTOBUF_PARSE_ERROR = 0;
    /** The name of the decoder attribute for the size of the message. */
    public static final String MESSAGE_LENGTH_ATTR =
            "grizzly-protobuf-message-length";

    /** The base protocol buffers serialization unit. */
    private final MessageLite prototype;
    /** A table of known extensions, searchable by name or field number. */
    private final ExtensionRegistryLite extensionRegistry;
    /** The attribute for the length of the message. */
    private final Attribute<Integer> messageLengthAttr;
    /** The length of the fixed header storing the size of the message. */
    private final int headerLength;

    /**
     * A protobuf decoder that uses the supplied {@code headerLength} to
     * determine the size of the message to be decoded.
     *
     * @param prototype The base protocol buffers serialization unit.
     * @param extensionRegistry A table of known extensions, searchable by name
     *                          or field number, may be {@code null}.
     * @param headerLength The length of the fixed header storing the size of
     *                     the message.
     */
    public FixedLengthProtobufDecoder(
            final @NonNull MessageLite prototype,
            final ExtensionRegistryLite extensionRegistry,
            final int headerLength) {
        this.prototype = prototype;
        this.extensionRegistry = extensionRegistry;
        this.headerLength = headerLength;
        messageLengthAttr = attributeBuilder.createAttribute(MESSAGE_LENGTH_ATTR);
    }

    /** {@inheritDoc} */
    @Override
    protected TransformationResult<Buffer, MessageLite> transformImpl(
            final AttributeStorage storage, final @NonNull Buffer input)
            throws TransformationException {
        log.debug("inputRemaining={}", input.remaining());

        Integer messageLength = messageLengthAttr.get(storage);
        if (messageLength == null) {
            if (input.remaining() < headerLength) {
                return createIncompletedResult(input);
            }

            final byte[] messageLengthArr = new byte[headerLength];
            input.get(messageLengthArr);
            log.debug("inputRemaining={}", input.remaining());

            messageLength = ByteBuffer.wrap(messageLengthArr).getInt();
            log.debug("messageLength={}", messageLength);
            messageLengthAttr.set(storage, messageLength);
        }

        if (input.remaining() < messageLength) {
            return createIncompletedResult(input);
        }
        log.debug("bufferRemaining={}", input.remaining());

        final int position = input.position();
        log.debug("position={}", position);

        final BufferInputStream inputStream =
                new BufferInputStream(input, position, (position + messageLength));
        final MessageLite message;
        try {
            if (extensionRegistry != null) {
                message = prototype.getParserForType()
                        .parseFrom(inputStream, extensionRegistry);
            } else {
                message = prototype.getParserForType().parseFrom(inputStream);
            }
        } catch (final InvalidProtocolBufferException e) {
            final String msg = "Error decoding protobuf message from input stream.";
            log.warn(msg, e);
            return createErrorResult(IO_PROTOBUF_PARSE_ERROR, msg);
        }

        return createCompletedResult(message, input);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return FixedLengthProtobufDecoder.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasInputRemaining(
            final AttributeStorage storage, final Buffer input) {
        return (input != null) && input.hasRemaining();
    }

}
