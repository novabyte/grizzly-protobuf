Grizzly-Protobuf
================

[Grizzly NIO framework](http://grizzly.java.net/) filters to encode and decode
 Google's [Protocol Buffers](https://developers.google.com/protocol-buffers/)
 serialized messages.

Grizzly-Protobuf is created and maintained by Chris Molozian (@novabyte).
<br/>
Code licensed under the [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0).
 Documentation licensed under [CC BY 3.0](http://creativecommons.org/licenses/by/3.0/).

## Usage ##

Grizzly-Protobuf is available on [Maven Central](http://search.maven.org/).

```xml
<dependency>
    <groupId>me.cmoz.grizzly</groupId>
    <artifactId>grizzly-protobuf</artifactId>
    <version>1.0.0</version>
</dependency>
```

If you're not using Maven (or a dependency resolver that's compatible with Maven
 repositories), you can download the JARs you need for your project from
 Maven Central.

### Filter Types ###

At the moment there are two filters to choose from:

1. __FixedLengthProtobufFilter__, this uses a fixed length header to store the
 size of the protobuf message being (de)serialized.
2. __Varint32ProtobufFilter__, this uses [`varint32` encoding](https://developers.google.com/protocol-buffers/docs/encoding#varints)
 to store the size of the protobuf message being (de)serialized.

If you're not sure which to use, start with the `Varint32ProtobufFilter` and
 move to the fixed length header alternative if necessary.

## Example ##

Both filters need a `MessageLite` type to use to parse all incoming messages.

The example below demonstrates the `Person` protocol format from the Protocol Buffers
 [Java Tutorial](https://developers.google.com/protocol-buffers/docs/javatutorial)
 with the `Varint32ProtobufFilter`. All messages sent/received by the filter chain
 will (de)serialize to the `Person` protocol format:

```java
final MessageLite prototype = Person.newBuilder()
        .setId(1234)
        .setName("John Doe")
        .setEmail("jdoe@example.com")
        .build();

final FilterChainBuilder serverFilterBuilder = FilterChainBuilder.stateless()
        .add(new TransportFilter())
        .add(new Varint32ProtobufFilter(prototype.getDefaultInstanceForType()));
```

For more detailed examples of how to integrate this filter into your code have a
 look at the [test cases](https://github.com/novabyte/grizzly-protobuf/tree/master/src/test/java/me/cmoz/grizzly/protobuf).

## Developer Notes ##

The codebase requires the [Gradle](http://gradle.org) build tool at version
 `1.6+` and the Java compiler at version `1.6.0` or greater.

The main external dependency for the project is [Grizzly NIO](http://grizzly.java.net/),
 at `2.3.5` or greater and [Protobuf-Java](http://search.maven.org/#artifactdetails|com.google.protobuf|protobuf-java|2.5.0|bundle)
 at `2.5.0` (for improved parsing performance [see here](http://protobuf.googlecode.com/svn/trunk/CHANGES.txt))
 although older versions are also supported.

For a full list of dependencies see the [build script](https://github.com/novabyte/grizzly-protobuf/blob/master/build.gradle).
 All dependencies are downloaded by Gradle during the build process.

### Building the codebase ###

A list of all possible build targets can be displayed by Gradle with
 `gradle tasks`.

In a regular write-compile-test cycle use `gradle test`.

A list of all project dependencies can be displayed by Gradle with
 `gradle dependencies`.

It is recommended to run Gradle with the
 [Build Daemon](http://docs.codehaus.org/display/GRADLE/Gradle+Build+Daemon)
 enabled to improve performance. e.g. `gradle --daemon` once the daemon is
 running it can be stopped with `gradle --stop`.

## Contribute ##

All contributions to the documentation and the codebase are very welcome. Feel
 free to open issues on the tracker wherever the documentation needs improving.

Also, pull requests are always welcome! `:)`
