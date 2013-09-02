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

    /** The size of the fixed header storing the length of the message. */
    protected final int headerSize;

    /**
     * A protobuf encoder that uses the supplied {@code headerSize} to add a
     * header to the message containing the size of the message.
     *
     * @param headerSize The size of the fixed header storing the length of the
     *                   message.
     */
    public ProtobufEncoder(final int headerSize) {
        this.headerSize = headerSize;
    }

    /** {@inheritDoc} */
    @Override
    protected TransformationResult<MessageLite, Buffer> transformImpl(
            final AttributeStorage storage, final @NonNull MessageLite input)
            throws TransformationException {
        final MemoryManager memoryManager = obtainMemoryManager(storage);
        final BufferOutputStream outputStream = new BufferOutputStream(memoryManager);

        try {
            input.writeTo(outputStream);
            outputStream.close();
        } catch (final IOException e) {
            final String msg = "Error writing protobuf message to output stream.";
            log.warn(msg, e);
            return createErrorResult(IO_FAILED_WRITE, msg);
        }

        final Buffer buffer = outputStream.getBuffer().flip();
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
