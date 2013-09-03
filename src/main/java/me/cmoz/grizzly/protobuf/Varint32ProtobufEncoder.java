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

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.MessageLite;
import org.glassfish.grizzly.AbstractTransformer;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.TransformationException;
import org.glassfish.grizzly.TransformationResult;
import org.glassfish.grizzly.attributes.AttributeStorage;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.utils.BufferOutputStream;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Encodes Protocol Buffers messages to the output stream using a
 * {@code Varint32} encoded header to store the length of the serialized
 * message.
 */
@Slf4j
public class Varint32ProtobufEncoder extends AbstractTransformer<MessageLite, Buffer> {

    /** The error code for a failed write to the output stream. */
    public static final int IO_FAILED_WRITE = 0;

    /** {@inheritDoc} */
    @Override
    protected TransformationResult<MessageLite, Buffer> transformImpl(
            final AttributeStorage storage, final @NonNull MessageLite input)
            throws TransformationException {
        final MemoryManager memoryManager = obtainMemoryManager(storage);
        final BufferOutputStream outputStream = new BufferOutputStream(memoryManager);
        final CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);

        final int messageSize = input.toByteArray().length;
        final int headerSize = CodedOutputStream.computeRawVarint32Size(messageSize);
        log.debug("headerSize={}", headerSize);
        log.debug("messageSize={}", messageSize);

        try {
            codedOutputStream.writeRawVarint32(messageSize);
            codedOutputStream.flush();
            input.writeTo(codedOutputStream);
            outputStream.close();
        } catch (final IOException e) {
            final String msg = "Error writing protobuf message to output stream.";
            log.warn(msg, e);
            return TransformationResult.createErrorResult(IO_FAILED_WRITE, msg);
        }

        final Buffer buffer = outputStream.getBuffer();
        return TransformationResult.createCompletedResult(buffer, null);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return Varint32ProtobufEncoder.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasInputRemaining(
            final AttributeStorage storage, final MessageLite input) {
        return (input != null);
    }

}
