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
import org.glassfish.grizzly.utils.BufferOutputStream;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

/**
 * Encodes Protocol Buffers messages to the output stream using a
 * {@code Varint32} encoded header to store the length of the serialized
 * message.
 */
@Slf4j
public class Varint32ProtobufEncoder extends AbstractProtobufEncoder {

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

        final CodedOutputStream codedOutputStream =
                CodedOutputStream.newInstance(outputStream);
        codedOutputStream.writeRawVarint32(messageLength);
        codedOutputStream.flush();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return Varint32ProtobufEncoder.class.getName();
    }

}
