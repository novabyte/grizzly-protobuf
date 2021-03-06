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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static org.glassfish.grizzly.TransformationResult.createCompletedResult;
import static org.glassfish.grizzly.TransformationResult.createErrorResult;

/**
 * Encodes Protocol Buffers messages to the output stream.
 */
@Slf4j
abstract class AbstractProtobufEncoder extends AbstractTransformer<MessageLite, Buffer> {

    /** The error code for a failed write to the output stream. */
    public static final int IO_WRITE_ERROR = 0;

    /**
     * Writes a header to the supplied {@code outputStream}.
     * </p>
     * <b>Note</b> Do not close the {@code outputStream} this will be handled
     * automatically.
     *
     * @param outputStream The stream to write the header to.
     * @param messageLength The length of the message to write.
     * @throws IOException If there was a problem writing the header.
     */
    public abstract void writeHeader(
            final BufferOutputStream outputStream, final int messageLength)
            throws IOException;

    /** {@inheritDoc} */
    @Override
    protected final TransformationResult<MessageLite, Buffer> transformImpl(
            final AttributeStorage storage, final @NonNull MessageLite input)
            throws TransformationException {
        final MemoryManager memoryManager = obtainMemoryManager(storage);
        final BufferOutputStream outputStream = new BufferOutputStream(memoryManager);

        final byte[] encodedMessage = input.toByteArray();
        try {
            writeHeader(outputStream, encodedMessage.length);

            outputStream.write(encodedMessage);
            outputStream.close();
        } catch (final IOException e) {
            final String msg = "Error writing protobuf message to output stream.";
            log.warn(msg, e);
            return createErrorResult(IO_WRITE_ERROR, msg);
        }

        return createCompletedResult(outputStream.getBuffer().flip(), null);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AbstractProtobufEncoder.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean hasInputRemaining(
            final AttributeStorage storage, final MessageLite input) {
        return (input != null);
    }

}
