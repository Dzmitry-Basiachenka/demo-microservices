package com.microservices.resource.service.controller;

import com.microservices.resource.service.dto.ResourceUploadedResponse;
import com.microservices.resource.service.dto.ResourcesDeletedResponse;
import com.microservices.resource.service.entity.ResourceEntity;
import com.microservices.resource.service.service.ResourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static com.microservices.resource.service.service.Constants.CONTENT_TYPE_AUDIO_MPEG;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResourceController.class)
public class ResourceControllerTest {

    private static final String URL_PATH = "/resources";
    private static final String FILE_NAME = "audio.mp3";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceService resourceService;

    @Test
    void shouldCreateResource() throws Exception {
        var content = new byte[]{0};
        var multipartFile = new MockMultipartFile(
            "file",
            FILE_NAME,
            CONTENT_TYPE_AUDIO_MPEG,
            content
        );
        var id = 1L;

        when(resourceService.uploadResource(multipartFile)).thenReturn(new ResourceUploadedResponse(id));

        mockMvc.perform(MockMvcRequestBuilders.multipart(URL_PATH).file(multipartFile))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value((int) id));

        verify(resourceService).uploadResource(multipartFile);
        verifyNoMoreInteractions(resourceService);
    }

    @Test
    void shouldGetResource() throws Exception {
        var resourceEntity = getResourceEntity();
        var id = resourceEntity.getId();

        when(resourceService.getResource(id)).thenReturn(resourceEntity);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_PATH + "/{id}", id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(resourceEntity.getId().intValue()))
            .andExpect(jsonPath("$.bucket").value(resourceEntity.getBucket()))
            .andExpect(jsonPath("$.key").value(resourceEntity.getKey()))
            .andExpect(jsonPath("$.name").value(resourceEntity.getName()))
            .andExpect(jsonPath("$.size").value(resourceEntity.getSize().intValue()));

        verify(resourceService).getResource(id);
        verifyNoMoreInteractions(resourceService);
    }

    @Test
    void shouldDownloadResource() throws Exception {
        var content = new byte[]{0};

        var resourceEntity = new ResourceEntity();
        resourceEntity.setId(1L);
        resourceEntity.setBucket("resources");
        resourceEntity.setKey("11111111-2222-3333-4444-555555555555");
        resourceEntity.setName("audio.mp3");
        resourceEntity.setSize((long) content.length);

        var id = resourceEntity.getId();

        when(resourceService.getResource(id)).thenReturn(resourceEntity);
        when(resourceService.downloadResource(resourceEntity)).thenReturn(content);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_PATH + "/{id}/download", id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(CONTENT_TYPE_AUDIO_MPEG))
            .andExpect(content().bytes(content));

        verify(resourceService).getResource(id);
        verify(resourceService).downloadResource(resourceEntity);
        verifyNoMoreInteractions(resourceService);
    }

    @Test
    void shouldDeleteResources() throws Exception {
        Long id = 1L;
        var ids = List.of(id);

        when(resourceService.deleteResources(ids)).thenReturn(new ResourcesDeletedResponse(ids));

        mockMvc.perform(MockMvcRequestBuilders.delete(URL_PATH).param("ids", id.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.ids.length()").value(1))
            .andExpect(jsonPath("$.ids[0]").value(id));

        verify(resourceService).deleteResources(ids);
        verifyNoMoreInteractions(resourceService);
    }

    private ResourceEntity getResourceEntity() {
        var resourceEntity = new ResourceEntity();
        resourceEntity.setId(1L);
        resourceEntity.setBucket("resources");
        resourceEntity.setKey("11111111-2222-3333-4444-555555555555");
        resourceEntity.setName("audio.mp3");
        resourceEntity.setSize(10L);
        return resourceEntity;
    }
}
