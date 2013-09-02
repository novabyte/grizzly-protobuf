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
import org.glassfish.grizzly.TransformationException;
import org.glassfish.grizzly.TransformationResult;
import org.glassfish.grizzly.attributes.AttributeStorage;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

import static org.glassfish.grizzly.TransformationResult.createIncompletedResult;

/**
 * Decodes Protocol Buffers messages from the input stream using a fixed header
 * to determine the size of a message.
 */
@Slf4j
public class FixedHeaderProtobufDecoder extends ProtobufDecoder {

    /** The size of the fixed header storing the length of the message. */
    private final int headerSize;

    /**
     * A protobuf decoder that uses the supplied {@code headerSize} to determine
     * the size of the message to be decoded.
     *
     * @param prototype The base protocol buffers serialization unit.
     * @param extensionRegistry A table of known extensions, searchable by name
     *                          or field number, may be {@code null}.
     * @param headerSize The size of the fixed header storing the length of the
     *                   message.
     */
    public FixedHeaderProtobufDecoder(
            final @NonNull MessageLite prototype,
            final ExtensionRegistryLite extensionRegistry,
            final int headerSize) {
        super(prototype, extensionRegistry);
        this.headerSize = headerSize;
    }

    /** {@inheritDoc} */
    @Override
    protected TransformationResult<Buffer, MessageLite> transformImpl(
            final AttributeStorage storage, final Buffer input)
            throws TransformationException {
        log.debug("inputRemaining={}", input.remaining());
        if (input.remaining() < this.headerSize) {
            return createIncompletedResult(input);
        }

        final byte[] messageSizeArr = new byte[headerSize];
        input.get(messageSizeArr);

        log.debug("messageSizeArr={}", messageSizeArr);
        messageSizeAttr.set(storage, ByteBuffer.wrap(messageSizeArr).getInt());

        return super.transformImpl(storage, input);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return FixedHeaderProtobufDecoder.class.getName();
    }

}
