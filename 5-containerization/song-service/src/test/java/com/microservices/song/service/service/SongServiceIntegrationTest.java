package com.microservices.song.service.service;

import com.microservices.song.service.dto.SongDto;
import com.microservices.song.service.entity.SongEntity;
import com.microservices.song.service.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {"eureka.client.enabled:false"}
)
@Testcontainers
public class SongServiceIntegrationTest {

    @Autowired
    private SongService songService;

    @Autowired
    private SongRepository songRepository;

    @BeforeEach
    void init() {
        songRepository.deleteAll();
    }

    @Test
    void shouldCreateSong() {
        var songDto = getSongDto();

        var songCreatedResponse = songService.createSong(songDto);
        assertNotNull(songCreatedResponse);
        assertNotNull(songCreatedResponse.id());

        var foundSongEntity = songRepository.findById(songCreatedResponse.id());
        assertTrue(foundSongEntity.isPresent());

        var actualSongEntity = foundSongEntity.get();
        assertEquals(songDto.id(), actualSongEntity.getId());
        assertEquals(songDto.name(), actualSongEntity.getName());
        assertEquals(songDto.artist(), actualSongEntity.getArtist());
        assertEquals(songDto.album(), actualSongEntity.getAlbum());
        assertEquals(songDto.length(), actualSongEntity.getLength());
        assertEquals(songDto.released(), actualSongEntity.getReleased());
    }

    @Test
    void shouldGetSongs() {
        var savedSongEntity = songRepository.save(getSongEntity());

        var songDto = songService.getSong(savedSongEntity.getId());

        assertNotNull(songDto);
        assertEquals(savedSongEntity.getId(), songDto.id());
        assertEquals(savedSongEntity.getName(), songDto.name());
        assertEquals(savedSongEntity.getArtist(), songDto.artist());
        assertEquals(savedSongEntity.getAlbum(), songDto.album());
        assertEquals(savedSongEntity.getLength(), songDto.length());
        assertEquals(savedSongEntity.getReleased(), songDto.released());
    }

    @Test
    void shouldDeleteSongs() {
        var savedSongEntity = songRepository.save(getSongEntity());

        var songDeletedResponse = songService.deleteSongs(List.of(savedSongEntity.getId()));

        assertNotNull(songDeletedResponse);
        assertNotNull(songDeletedResponse.ids());
        assertEquals(1, songDeletedResponse.ids().size());

        var foundSongEntities = songRepository.findAllById(songDeletedResponse.ids());
        assertNotNull(foundSongEntities);
        assertTrue(foundSongEntities.isEmpty());
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

    private SongEntity getSongEntity() {
        SongEntity songEntity = new SongEntity();
        songEntity.setId(1L);
        songEntity.setName("Song");
        songEntity.setArtist("John Doe");
        songEntity.setAlbum("Songs");
        songEntity.setLength("60");
        songEntity.setReleased("2020");
        return songEntity;
    }
}
