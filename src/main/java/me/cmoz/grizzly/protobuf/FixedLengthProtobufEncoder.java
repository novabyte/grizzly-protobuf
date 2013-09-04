package me.cmoz.grizzly.protobuf;

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

import static org.glassfish.grizzly.TransformationResult.createErrorResult;

/**
 * Encodes Protocol Buffers messages to the output stream with a fixed length
 * header containing the size of the message.
 */
@Slf4j
public class FixedLengthProtobufEncoder extends AbstractTransformer<MessageLite, Buffer> {

    /** The error code for a failed write to the output stream. */
    public static final int IO_WRITE_ERROR = 0;

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
    protected TransformationResult<MessageLite, Buffer> transformImpl(
            final AttributeStorage storage, final @NonNull MessageLite input)
            throws TransformationException {
        final MemoryManager memoryManager = obtainMemoryManager(storage);
        final BufferOutputStream outputStream = new BufferOutputStream(memoryManager);

        try {
            outputStream.write(new byte[headerLength]);
            input.writeTo(outputStream);
            outputStream.close();
        } catch (final IOException e) {
            final String msg = "Error writing protobuf message to output stream.";
            log.warn(msg, e);
            return createErrorResult(IO_WRITE_ERROR, msg);
        }

        final Buffer buffer = outputStream.getBuffer().flip();
        log.debug("bufferRemaining={}", buffer.remaining());
        buffer.putInt(0, (buffer.remaining() - headerLength));

        return TransformationResult.createCompletedResult(buffer, null);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return FixedLengthProtobufEncoder.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasInputRemaining(
            final AttributeStorage storage, final MessageLite input) {
        return (input != null);
    }

}
