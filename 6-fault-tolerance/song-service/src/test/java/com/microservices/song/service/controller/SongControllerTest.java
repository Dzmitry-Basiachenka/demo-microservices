package com.microservices.song.service.controller;

import com.microservices.song.service.dto.SongCreatedResponse;
import com.microservices.song.service.dto.SongDto;
import com.microservices.song.service.dto.SongsDeletedResponse;
import com.microservices.song.service.service.SongService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SongController.class)
public class SongControllerTest {

    private static final String URL_PATH = "/songs";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SongService songService;

    @Test
    void shouldCreateSong() throws Exception {
        var songDto = getSongDto();
        var id = songDto.id();

        when(songService.createSong(songDto)).thenReturn(new SongCreatedResponse(id));

        mockMvc.perform(MockMvcRequestBuilders.post(URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(songDto)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(id.intValue()));

        verify(songService).createSong(songDto);
        verifyNoMoreInteractions(songService);
    }

    @Test
    void shouldGetSong() throws Exception {
        var songDto = getSongDto();
        var id = songDto.id();

        when(songService.getSong(id)).thenReturn(songDto);

        mockMvc.perform(MockMvcRequestBuilders.get(URL_PATH + "/{id}", id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(songDto.id().intValue()))
            .andExpect(jsonPath("$.name").value(songDto.name()))
            .andExpect(jsonPath("$.artist").value(songDto.artist()))
            .andExpect(jsonPath("$.album").value(songDto.album()))
            .andExpect(jsonPath("$.length").value(songDto.length()))
            .andExpect(jsonPath("$.released").value(songDto.released()));

        verify(songService).getSong(id);
        verifyNoMoreInteractions(songService);
    }

    @Test
    void shouldDeleteSongs() throws Exception {
        Long id = 1L;
        var ids = List.of(id);

        when(songService.deleteSongs(ids)).thenReturn(new SongsDeletedResponse(ids));

        mockMvc.perform(MockMvcRequestBuilders.delete(URL_PATH).param("ids", id.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.ids.length()").value(1))
            .andExpect(jsonPath("$.ids[0]").value(id));

        verify(songService).deleteSongs(ids);
        verifyNoMoreInteractions(songService);
    }

    private SongDto getSongDto() {
        return new SongDto(
            1L,
            "Song",
            "John Doe",
            "Songs",
            "60",
            "2020"
        );
    }
}
