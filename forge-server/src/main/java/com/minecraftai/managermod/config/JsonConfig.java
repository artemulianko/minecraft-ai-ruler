package com.minecraftai.managermod.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonConfig {
    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Disable FAIL_ON_EMPTY_BEANS to allow serialization of empty objects
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // You can add other configurations here
        // mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}

