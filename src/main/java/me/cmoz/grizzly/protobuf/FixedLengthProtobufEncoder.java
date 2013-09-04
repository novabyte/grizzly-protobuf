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

import org.glassfish.grizzly.utils.BufferOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

import lombok.extern.slf4j.Slf4j;

/**
 * Encodes Protocol Buffers messages to the output stream with a fixed length
 * header containing the size of the message.
 */
@Slf4j
public class FixedLengthProtobufEncoder extends AbstractProtobufEncoder {

    /** The length of the fixed header storing the size of the message. */
    private final int headerLength;

    /**
     * A protobuf encoder that uses the supplied {@code headerSize} to add a
     * header to the message containing the size of the message.
     *
     * @param headerLength The length of the header storing the size of the
     *                     message.
     */
    public FixedLengthProtobufEncoder(final int headerLength) {
        this.headerLength = headerLength;
    }

    /** {@inheritDoc} */
    @Override
    public void writeHeader(
            final BufferOutputStream outputStream, final int messageLength)
            throws IOException {
        if (outputStream == null) {
            throw new IllegalArgumentException("'outputStream' cannot be null.");
        }
        if (messageLength < 0) {
            throw new IllegalArgumentException("'messageLength' cannot be negative.");
        }
        log.debug("encodedMessageLength={}", messageLength);

        final ByteBuffer buf = ByteBuffer.allocate(headerLength);
        buf.putInt(messageLength);

        outputStream.write(buf.array());
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return FixedLengthProtobufEncoder.class.getName();
    }

}
