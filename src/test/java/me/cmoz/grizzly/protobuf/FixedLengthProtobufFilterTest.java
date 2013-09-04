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

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.*;
import org.glassfish.grizzly.nio.NIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.utils.DataStructures;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * {@link me.cmoz.grizzly.protobuf.FixedLengthProtobufFilter}.
 */
public class FixedLengthProtobufFilterTest {

    /** The port for the local test. */
    private static final int PORT = 20389;

    @Test
    @SuppressWarnings("unchecked")
    public void filterMessageTest()
            throws IOException, InterruptedException, ExecutionException {
        final Proto.User user = Proto.User.newBuilder()
                .setName("Albert Einstein")
                .setEmail("albert.einstein@gmail.com")
                .addPhone(
                        Proto.User.PhoneNumber.newBuilder()
                                .setNumber("555-4321")
                                .setType(Proto.User.PhoneType.HOME))
                .build();

        final FilterChainBuilder serverFilterBuilder = FilterChainBuilder.stateless()
                .add(new TransportFilter())
                .add(new FixedLengthProtobufFilter(user.getDefaultInstanceForType()))
                .add(new ProtoServerFilter(user));

        final NIOTransport transport = TCPNIOTransportBuilder.newInstance()
                .setProcessor(serverFilterBuilder.build())
                .build();

        Connection connection = null;
        try {
            transport.bind(PORT);
            transport.start();

            connection = transport.connect("localhost", PORT).get();

            final BlockingQueue<Proto.User> resultQueue = DataStructures.getLTQInstance(Proto.User.class);

            final FilterChainBuilder clientFilterBuilder = FilterChainBuilder.stateless()
                    .add(new TransportFilter())
                    .add(new FixedLengthProtobufFilter(user.getDefaultInstanceForType()))
                    .add(new ProtoClientFilter(resultQueue));

            final FilterChain clientFilter = clientFilterBuilder.build();
            connection.setProcessor(clientFilter);

            connection.write(user).get();

            assertEquals(user, resultQueue.poll(10, TimeUnit.SECONDS));
        } finally {
            if (connection != null)
                connection.close();

            transport.stop();
        }
    }

    private static class ProtoServerFilter extends BaseFilter {

        /** The message to send outbound. */
        private final Proto.User user;

        public ProtoServerFilter(final Proto.User user) {
            this.user = user;
        }

        public NextAction handleRead(final FilterChainContext context)
                throws IOException {
            final Proto.User user = (Proto.User) context.getMessage();

            assertEquals(this.user.getName(), user.getName());
            assertEquals(this.user.getEmail(), user.getEmail());
            assertEquals(this.user, user);

            context.write(user);
            return context.getStopAction();
        }

    }

    private static class ProtoClientFilter extends BaseFilter {

        /** A storage queue to send the read messages to. */
        private final BlockingQueue<Proto.User> resultQueue;

        public ProtoClientFilter(final BlockingQueue<Proto.User> resultQueue) {
            this.resultQueue = resultQueue;
        }

        public NextAction handleRead(final FilterChainContext context)
                throws IOException {
            resultQueue.add((Proto.User) context.getMessage());
            return context.getStopAction();
        }

    }

}
