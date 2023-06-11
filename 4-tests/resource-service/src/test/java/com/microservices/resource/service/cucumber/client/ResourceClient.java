package com.microservices.resource.service.cucumber.client;

import com.microservices.resource.service.controller.ResourceController;
import com.microservices.resource.service.service.ResourceService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;

import static com.microservices.resource.service.service.Constants.CONTENT_TYPE_AUDIO_MPEG;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

@Component
public class ResourceClient {

    private static final String URL_PATH = "/resources";

    @Autowired
    private ResourceService resourceService;

    @PostConstruct
    void init() {
        RestAssuredMockMvc.standaloneSetup(new ResourceController(resourceService));
    }

    public MockMvcResponse uploadResource(InputStream inputStream, String fileName) {
        return given()
            .multiPart("file", fileName, inputStream, CONTENT_TYPE_AUDIO_MPEG)
            .post(URL_PATH);
    }

    public MockMvcResponse downloadResource(long id) {
        return given().get(URL_PATH + "/{id}/download", id);
    }

    public MockMvcResponse deleteResource(long id) {
        return given().delete(URL_PATH + "?ids=" + id);
    }
}
