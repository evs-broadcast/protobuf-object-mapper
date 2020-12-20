package com.robert2411.protobuf.swagger.mapper;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.InvalidProtocolBufferException;
import com.jayway.jsonpath.TypeRef;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;

public class ProtobufObjectMapperTest {
    private static final String JSON =
            "{\n" +
                    "  \"greeting\": \"hello\",\n" +
                    "  \"test\": [\"world\"]\n" +
                    "}";
    private ProtobufObjectMapper mapper = new ProtobufObjectMapper();

    @Test
    public void objectToProto() throws InvalidProtocolBufferException, JsonProcessingException {
        TestObject testObject = new TestObject();
        testObject.setGreeting("Hello world");
        testObject.setTest(new ArrayList<>());
        testObject.getTest().add("hello");
        testObject.getTest().add("world");
        ProtobufTest.Greeting out = mapper.map(testObject, ProtobufTest.Greeting::newBuilder);

        Assert.assertEquals("Hello world", out.getGreeting());
        Assert.assertEquals("hello", out.getTest(0));
        Assert.assertEquals("world", out.getTest(1));
    }

    @Test
    public void protoToObject() throws IOException {
        ProtobufTest.Greeting greeting = ProtobufTest.Greeting.newBuilder()
                .setGreeting("Hello world")
                .addTest("hello")
                .addTest("world")
                .build();
        TestObject out = mapper.map(greeting, TestObject.class);

        Assert.assertEquals("Hello world", out.getGreeting());
        Assert.assertEquals("hello", out.getTest().get(0));
        Assert.assertEquals("world", out.getTest().get(1));
    }

    @Test
    public void protoToJsonTest() throws InvalidProtocolBufferException {
        ProtobufTest.Greeting greeting = ProtobufTest.Greeting.newBuilder()
                .setGreeting("hello")
                .addAllTest(Collections.singletonList("world"))
                .build();

        String json = mapper.protoToJson(greeting);

        assertThatJson(json)
                .isEqualTo(json(JSON));
    }

    @Test
    public void jsonToProtoTest() throws InvalidProtocolBufferException, JsonProcessingException {
        ProtobufTest.Greeting proto = mapper.jsonToProto(JSON, ProtobufTest.Greeting::newBuilder);

        Assert.assertEquals("hello", proto.getGreeting());
        Assert.assertEquals(1, proto.getTestList().size());
        Assert.assertEquals("world", proto.getTestList().get(0));
    }

    @Test
    public void jsonToObjectTest() throws IOException {
        TestObject object = mapper.jsonToObject(JSON, TestObject.class);

        Assert.assertEquals("hello", object.getGreeting());
        Assert.assertEquals(1, object.getTest().size());
        Assert.assertEquals("world", object.getTest().get(0));
    }

    @Test
    public void readFieldFromJson() throws IOException {
        List<String> object = mapper.readFieldFromJson(JSON, "$.test.[*]", new TypeRef<List<String>>() {});

        Assert.assertEquals(1, object.size());
        Assert.assertEquals("world", object.get(0));
    }

    @Test
    public void writeValueToJson() throws IOException {
        String json = mapper.putValueInJson(JSON, "$", "testkey", Collections.singletonList("bla"));

        assertThatJson(json)
                .isEqualTo(json("{\"greeting\":\"hello\",\"test\":[\"world\"],\"testkey\":[\"bla\"]}"));
    }

    @Test
    public void readAndPut() throws IOException {
        String json = mapper.readAndPut(JSON, "$.test", "{}", "$", "testkey", i -> i);

        assertThatJson(json)
                .isEqualTo(json("{\"testkey\":[\"world\"]}"));
    }

    @Test
    public void objectToJsonTest() throws JsonProcessingException {
        TestObject testObject = new TestObject();
        testObject.setGreeting("hello");
        testObject.setTest(Collections.singletonList("world"));

        String json = mapper.objectToJson(testObject);

        assertThatJson(json)
                .isEqualTo(json(JSON));
    }
}