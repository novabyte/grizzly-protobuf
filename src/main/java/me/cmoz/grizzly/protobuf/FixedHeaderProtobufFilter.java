package me.cmoz.grizzly.protobuf;

import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.AbstractCodecFilter;

import lombok.*;

/**
 * A filter for Google's Protocol Buffers serialization format that uses a fixed
 * size header that stores the length of the message.
 */
public final class FixedHeaderProtobufFilter extends AbstractCodecFilter<Buffer, MessageLite> {

    /** The default size of the fixed header. */
    public static final int DEFAULT_HEADER_SIZE = 4;

    /**
     * A Protocol Buffers filter that uses the {@see DEFAULT_HEADER_SIZE} as the
     * header size containing the length of the message.
     *
     * @param prototype The base protocol buffers serialization unit.
     */
    public FixedHeaderProtobufFilter(final @NonNull MessageLite prototype) {
        this(prototype, null, DEFAULT_HEADER_SIZE);
    }

    /**
     * A Protocol Buffers filter that uses the {@see DEFAULT_HEADER_SIZE} as the
     * header size containing the length of the message with the supplied
     * {@code extensionRegistry}.
     *
     * @param prototype The base protocol buffers serialization unit.
     * @param extensionRegistry A table of known extensions, searchable by name
     *                          or field number, may be {@code null}.
     */
    public FixedHeaderProtobufFilter(
            final @NonNull MessageLite prototype,
            final ExtensionRegistryLite extensionRegistry) {
        this(prototype, extensionRegistry, DEFAULT_HEADER_SIZE);
    }

    /**
     * A Protocol Buffers filter that uses the supplied {@code headerSize} as
     * the header size containing the length of the message.
     *
     * @param prototype The base protocol buffers serialization unit.
     * @param headerSize The size of the fixed header storing the length of the
     *                   message.
     */
    public FixedHeaderProtobufFilter(
            final @NonNull MessageLite prototype, final int headerSize) {
        this(prototype, null, headerSize);
    }

    /**
     * A Protocol Buffers filter that uses the supplied {@code headerSize} as
     * the header size containing the length of the message.
     *
     * @param prototype The base protocol buffers serialization unit.
     * @param extensionRegistry A table of known extensions, searchable by name
     *                          or field number, may be {@code null}.
     * @param headerSize The size of the fixed header storing the length of the
     *                   message.
     */
    public FixedHeaderProtobufFilter(
            final @NonNull MessageLite prototype,
            final ExtensionRegistryLite extensionRegistry,
            final int headerSize) {
        super(new FixedHeaderProtobufDecoder(prototype, extensionRegistry, headerSize),
                new FixedHeaderProtobufEncoder(headerSize));
    }

}
