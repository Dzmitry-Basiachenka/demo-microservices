package com.microservices.song.service.controller;

import com.microservices.song.service.dto.SongCreatedResponse;
import com.microservices.song.service.dto.SongDto;
import com.microservices.song.service.dto.SongsDeletedResponse;
import com.microservices.song.service.service.SongService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = SongController.class)
public abstract class AbstractSongControllerTest {

    @Autowired
    private SongController songController;

    @MockBean
    private SongService songService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.standaloneSetup(songController);

        var songDto = new SongDto(
            1L,
            "Song",
            "John Doe",
            "Songs",
            "60",
            "2020"
        );
        var id = songDto.id();

        when(songService.createSong(songDto)).thenReturn(new SongCreatedResponse(id));
        when(songService.getSong(id)).thenReturn(songDto);
        when(songService.deleteSongs(eq(List.of(id)))).thenReturn(new SongsDeletedResponse(List.of(id)));
    }
}
