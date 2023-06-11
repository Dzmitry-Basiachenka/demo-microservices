package com.microservices.resource.service.cucumber.client;

import com.microservices.resource.service.controller.ResourceController;
import com.microservices.resource.service.service.ResourceService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Autowired
    private WireMockServer wireMockServer;

    @PostConstruct
    private void init() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/storages"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("response/storages.json"))
        );

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

    public MockMvcResponse completeResourceUpload(long id) {
        return given().put(URL_PATH + "/{id}/complete", id);
    }

    public MockMvcResponse deleteResource(long id) {
        return given().delete(URL_PATH + "?ids=" + id);
    }
}
