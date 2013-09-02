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

import com.google.protobuf.MessageLite;
import org.glassfish.grizzly.AbstractTransformer;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.TransformationException;
import org.glassfish.grizzly.TransformationResult;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.attributes.AttributeStorage;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.utils.BufferOutputStream;
import java.io.IOException;

import static org.glassfish.grizzly.TransformationResult.createErrorResult;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Encodes Protocol Buffers messages to the output stream.
 */
@Slf4j
abstract class ProtobufEncoder extends AbstractTransformer<MessageLite, Buffer> {

    /** The error code for a failed write to the buffer output stream. */
    public static final int IO_FAILED_WRITE = 0;
    /** The name of the decoder attribute for the size of the message. */
    public static final String HEADER_SIZE_ATTR = "grizzly-protobuf-header-size";

    /** The attribute for the size of the fixed header. */
    protected final Attribute<Integer> headerSizeAttr;

    /**
     * A protobuf encoder that adds a header to the message containing the size
     * of the message.
     */
    public ProtobufEncoder() {
        headerSizeAttr = attributeBuilder.createAttribute(HEADER_SIZE_ATTR);
    }

    /** {@inheritDoc} */
    @Override
    protected TransformationResult<MessageLite, Buffer> transformImpl(
            final AttributeStorage storage, final @NonNull MessageLite input)
            throws TransformationException {
        final Integer headerSize = headerSizeAttr.get(storage);

        final MemoryManager memoryManager = obtainMemoryManager(storage);
        final BufferOutputStream outputStream = new BufferOutputStream(memoryManager);

        log.warn("input={}", input.toByteArray());
        log.warn("inputSize={}", input.toByteArray().length);
        try {
            outputStream.write(new byte[headerSize]);
            input.writeTo(outputStream);
            outputStream.close();
        } catch (final IOException e) {
            final String msg = "Error writing protobuf message to output stream.";
            log.warn(msg, e);
            return createErrorResult(IO_FAILED_WRITE, msg);
        }

        final Buffer buffer = outputStream.getBuffer().flip();
        log.warn("bufferRemaining={}", buffer.remaining());
        buffer.putInt(0, (buffer.remaining() - headerSize));

        return TransformationResult.createCompletedResult(buffer, null);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return ProtobufEncoder.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasInputRemaining(
            final AttributeStorage storage, final MessageLite input) {
        return (input != null);
    }

}
