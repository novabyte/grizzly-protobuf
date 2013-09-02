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
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.TransformationException;
import org.glassfish.grizzly.TransformationResult;
import org.glassfish.grizzly.attributes.AttributeStorage;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Encodes Protocol Buffers messages to the output stream with a fixed size
 * header containing the size of the message.
 */
@Slf4j
public final class FixedHeaderProtobufEncoder extends ProtobufEncoder {

    /** The size of the fixed header storing the length of the message. */
    private final int headerSize;

    /**
     * A protobuf encoder that uses the supplied {@code headerSize} to add a
     * header to the message containing the size of the message.
     *
     * @param headerSize The size of the fixed header storing the length of the
     *                   message.
     */
    public FixedHeaderProtobufEncoder(final int headerSize) {
        this.headerSize = headerSize;
    }

    /** {@inheritDoc} */
    @Override
    protected TransformationResult<MessageLite, Buffer> transformImpl(
            final AttributeStorage storage, final @NonNull MessageLite input)
            throws TransformationException {
        log.debug("headerSize={}", headerSize);
        headerSizeAttr.set(storage, headerSize);

        return super.transformImpl(storage, input);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return FixedHeaderProtobufEncoder.class.getName();
    }

}
