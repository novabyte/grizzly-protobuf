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
import org.glassfish.grizzly.filterchain.AbstractCodecFilter;

import lombok.NonNull;

/**
 * A filter for Google's Protocol Buffers serialization format that uses a
 * {@code Varint32} encoding to store the length of the encoded message.
 */
public class Varint32ProtobufFilter extends AbstractCodecFilter<Buffer, MessageLite> {

    /**
     * A Protocol Buffers filter that uses a {@code Varint32} encoding for the
     * message length.
     *
     * @param prototype The base protocol buffers serialization unit.
     */
    public Varint32ProtobufFilter(final @NonNull MessageLite prototype) {
        this(prototype, null);
    }

    /**
     * A Protocol Buffers filter that uses a {@code Varint32} encoding for the
     * message length.
     *
     * @param prototype The base protocol buffers serialization unit.
     * @param extensionRegistry A table of known extensions, searchable by name
     *                          or field number, may be {@code null}.
     */
    public Varint32ProtobufFilter(
            final @NonNull MessageLite prototype,
            final ExtensionRegistryLite extensionRegistry) {
        super(new Varint32ProtobufDecoder(prototype, extensionRegistry),
                new Varint32ProtobufEncoder());
    }

}
