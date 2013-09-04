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

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.utils.BufferInputStream;

import java.io.IOException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Decodes Protocol Buffers messages from the input stream using a
 * {@code Varint32} encoded header to determine message size.
 */
@Slf4j
public class Varint32ProtobufDecoder extends AbstractProtobufDecoder {

    /**
     * A protobuf decoder that uses a {@code Varint32} encoded header to
     * determine the size of a message to be decoded.
     *
     * @param prototype The base protocol buffers serialization unit.
     * @param extensionRegistry A table of known extensions, searchable by name
     *                          or field number, may be {@code null}.
     */
    public Varint32ProtobufDecoder(
            final @NonNull MessageLite prototype,
            final ExtensionRegistryLite extensionRegistry) {
        super(prototype, extensionRegistry);
    }

    /** {@inheritDoc} */
    @Override
    public int readHeader(final Buffer input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("'input' cannot be null.");
        }

        final BufferInputStream inputStream = new BufferInputStream(input);
        return CodedInputStream.readRawVarint32(input.get(), inputStream);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return Varint32ProtobufDecoder.class.getName();
    }

}
