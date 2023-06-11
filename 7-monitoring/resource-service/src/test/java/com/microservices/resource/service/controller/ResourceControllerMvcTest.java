package com.microservices.resource.service.controller;

import com.microservices.resource.service.AbstractIntegrationTest;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static com.microservices.resource.service.service.Constants.CONTENT_TYPE_AUDIO_MPEG;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class ResourceControllerMvcTest extends AbstractIntegrationTest {

    private static final String URL_PATH = "/resources";
    private static final String FILE_PATH = "/audio/audio1.mp3";
    private static final String FILE_NAME = "audio1.mp3";

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/storages"))
            .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("response/storages.json"))
        );
    }

    @Test
    @Transactional
    void shouldUploadResource() throws Exception {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();
        var multipartFile = new MockMultipartFile(
            "file",
            FILE_NAME,
            CONTENT_TYPE_AUDIO_MPEG,
            content
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart(URL_PATH).file(multipartFile))
            .andExpect(status().isOk())
            .andExpect(jsonPath(".id").isNotEmpty());
    }

    @Test
    @Transactional
    void shouldGetResource() throws Exception {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();
        var multipartFile = new MockMultipartFile(
            "file",
            FILE_NAME,
            CONTENT_TYPE_AUDIO_MPEG,
            content
        );

        var uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart(URL_PATH).file(multipartFile))
            .andReturn();
        var uploadResponseContent = uploadResult.getResponse().getContentAsString();
        var id = JsonPath.read(uploadResponseContent, "$.id");

        mockMvc.perform(MockMvcRequestBuilders.get(URL_PATH + "/{id}", id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.storageId").value(1000L))
            .andExpect(jsonPath("$.key").isNotEmpty())
            .andExpect(jsonPath("$.name").value(FILE_NAME))
            .andExpect(jsonPath("$.size").value(content.length));
    }

    @Test
    @Transactional
    void shouldDownloadResource() throws Exception {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();
        var multipartFile = new MockMultipartFile(
            "file",
            FILE_NAME,
            CONTENT_TYPE_AUDIO_MPEG,
            content
        );

        var uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart(URL_PATH).file(multipartFile))
            .andReturn();
        var uploadResponseContent = uploadResult.getResponse().getContentAsString();
        var id = JsonPath.read(uploadResponseContent, "$.id");

        mockMvc.perform(MockMvcRequestBuilders.get(URL_PATH + "/{id}/download", id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(CONTENT_TYPE_AUDIO_MPEG))
            .andExpect(content().bytes(content));
    }

    @Test
    @Transactional
    void shouldCompleteResourceUpload() throws Exception {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();
        var multipartFile = new MockMultipartFile(
            "file",
            FILE_NAME,
            CONTENT_TYPE_AUDIO_MPEG,
            content
        );

        var uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart(URL_PATH).file(multipartFile))
            .andReturn();
        var uploadResponseContent = uploadResult.getResponse().getContentAsString();
        var id = JsonPath.read(uploadResponseContent, "$.id");

        mockMvc.perform(MockMvcRequestBuilders.put(URL_PATH + "/{id}/complete", id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.storageId").value(1001));
    }

    @Test
    @Transactional
    void shouldDeleteResource() throws Exception {
        var content = new ClassPathResource(FILE_PATH).getInputStream().readAllBytes();
        var multipartFile = new MockMultipartFile(
            "file",
            FILE_NAME,
            CONTENT_TYPE_AUDIO_MPEG,
            content
        );

        var uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart(URL_PATH).file(multipartFile))
            .andReturn();
        var uploadResponseContent = uploadResult.getResponse().getContentAsString();
        var id = JsonPath.read(uploadResponseContent, "$.id");

        mockMvc.perform(MockMvcRequestBuilders.delete(URL_PATH).param("ids", id.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath(".ids[0]").value(id));
    }
}
