package com.microservices.song.service.service;

import com.microservices.song.service.dto.SongDto;
import com.microservices.song.service.entity.SongEntity;
import com.microservices.song.service.mapper.SongMapperImpl;
import com.microservices.song.service.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SongServiceTest {

    @InjectMocks
    private SongService songService;

    @Mock
    private SongRepository songRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(songService, "songMapper", new SongMapperImpl());
    }

    @Test
    void shouldCreateSong() {
        var songEntity = getSongEntity();

        when(songRepository.save(songEntity)).thenReturn(songEntity);

        var songCreatedResponse = songService.createSong(getSongDto());

        assertEquals(songEntity.getId(), songCreatedResponse.id());

        verify(songRepository).save(songEntity);
        verifyNoMoreInteractions(songRepository);
    }

    @Test
    void shouldGetSongs() {
        var songEntity = getSongEntity();

        when(songRepository.findById(songEntity.getId())).thenReturn(Optional.of(songEntity));

        var actualSongDto = songService.getSong(songEntity.getId());

        assertEquals(songEntity.getId(), actualSongDto.id());
        assertEquals(songEntity.getName(), actualSongDto.name());
        assertEquals(songEntity.getArtist(), actualSongDto.artist());
        assertEquals(songEntity.getAlbum(), actualSongDto.album());
        assertEquals(songEntity.getLength(), actualSongDto.length());
        assertEquals(songEntity.getReleased(), actualSongDto.released());

        verify(songRepository).findById(songEntity.getId());
        verifyNoMoreInteractions(songRepository);
    }

    @Test
    void shouldDeleteSongs() {
        var songEntity = getSongEntity();
        var ids = List.of(songEntity.getId());

        when(songRepository.findAllById(ids)).thenReturn(List.of(songEntity));
        doNothing().when(songRepository).deleteById(songEntity.getId());

        var songDeletedResponse = songService.deleteSongs(ids);

        assertEquals(ids, songDeletedResponse.ids());

        verify(songRepository).findAllById(ids);
        verify(songRepository).deleteById(songEntity.getId());
        verifyNoMoreInteractions(songRepository);
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
