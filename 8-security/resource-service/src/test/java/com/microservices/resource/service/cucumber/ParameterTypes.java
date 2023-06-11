package com.microservices.resource.service.cucumber;

import com.microservices.resource.service.cucumber.definition.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.DataTableType;
import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;

import java.lang.reflect.Type;
import java.util.Map;

public class ParameterTypes {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DefaultParameterTransformer
    @DefaultDataTableEntryTransformer
    @DefaultDataTableCellTransformer
    public Object transformer(Object fromValue, Type toValueType) {
        return objectMapper.convertValue(fromValue, objectMapper.constructType(toValueType));
    }

    @DataTableType
    public Resource resource(Map<String, String> entry) {
        return new Resource(
            Long.valueOf(entry.get("id")),
            entry.get("storageId") != null ? Long.valueOf(entry.get("storageId")) : null,
            entry.get("key"),
            entry.get("name"),
            entry.get("size") != null ? Long.valueOf(entry.get("size")) : null
        );
    }
}
