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
import com.google.protobuf.MessageLite;
import org.glassfish.grizzly.Buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Decodes Protocol Buffers messages from the input stream using a fixed header
 * to determine the size of a message.
 */
@Slf4j
public class FixedLengthProtobufDecoder extends AbstractProtobufDecoder {

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
        super(prototype, extensionRegistry);
        if (headerLength < 0) {
            throw new IllegalArgumentException("'headerLength' cannot be negative.");
        }
        if (headerLength > 8192) {
            log.warn("Fixed length header size exceeds default buffer size.");
        }
        this.headerLength = headerLength;
    }

    /** {@inheritDoc} */
    @Override
    public int readHeader(final Buffer input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("'input' cannot be null.");
        }

        final byte[] messageLengthArr = new byte[headerLength];
        input.get(messageLengthArr);
        log.debug("inputRemaining={}", input.remaining());

        return ByteBuffer.wrap(messageLengthArr).getInt();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return FixedLengthProtobufDecoder.class.getName();
    }

}
