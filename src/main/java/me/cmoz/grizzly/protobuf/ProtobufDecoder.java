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

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import static org.glassfish.grizzly.TransformationResult.createCompletedResult;
import static org.glassfish.grizzly.TransformationResult.createErrorResult;
import static org.glassfish.grizzly.TransformationResult.createIncompletedResult;

/**
 * Decodes Protocol Buffer messages from the input stream.
 */
@Slf4j
abstract class ProtobufDecoder extends AbstractTransformer<Buffer, MessageLite> {

    /** The error code for a failed protobuf parse of a message. */
    public static final int IO_FAILED_PROTOBUF_PARSE = 0;
    /** The name of the decoder attribute for the size of the message. */
    public static final String MESSAGE_SIZE_ATTR = "grizzly-protobuf-message-size";

    /** The base protocol buffers serialization unit. */
    private final MessageLite prototype;
    /** A table of known extensions, searchable by name or field number. */
    private final ExtensionRegistryLite extensionRegistry;
    /** The attribute for the size of the fixed header. */
    protected final Attribute<Integer> messageSizeAttr;

    /**
     * A Protocol Buffers decoder, with registered extensions.
     *
     * @param prototype The base protocol buffers serialization unit.
     * @param extensionRegistry A table of known extensions, searchable by name
     *                          or field number.
     */
    public ProtobufDecoder(
            final @NonNull MessageLite prototype,
            final ExtensionRegistryLite extensionRegistry) {
        this.prototype = prototype;
        this.extensionRegistry = extensionRegistry;
        messageSizeAttr = attributeBuilder.createAttribute(MESSAGE_SIZE_ATTR);
    }

    /** {@inheritDoc} */
    @Override
    protected TransformationResult<Buffer, MessageLite> transformImpl(
            final AttributeStorage storage, final Buffer input)
            throws TransformationException {
        final Integer messageSize = messageSizeAttr.get(storage);
        log.debug("messageSize={}", messageSize);

        if (input.remaining() < messageSize) {
            return createIncompletedResult(input);
        }

        log.debug("bufferRemaining={}", input.remaining());

        final int position = input.position();
        log.debug("position={}", position);

        final BufferInputStream inputStream =
                new BufferInputStream(input, position, (position + messageSize));
        final MessageLite message;
        try {
            if (extensionRegistry != null) {
                message = prototype.getParserForType().parseFrom(inputStream, extensionRegistry);
            } else {
                message = prototype.getParserForType().parseFrom(inputStream);
            }
        } catch (final InvalidProtocolBufferException e) {
            final String msg = "Error decoding protobuf message from input stream.";
            log.warn(msg, e);
            return createErrorResult(IO_FAILED_PROTOBUF_PARSE, msg);
        }

        return createCompletedResult(message, input);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return ProtobufDecoder.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasInputRemaining(
            final AttributeStorage storage, final Buffer input) {
        return (input != null) && input.hasRemaining();
    }

}
