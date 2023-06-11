package com.microservices.song.service.service;

import com.microservices.song.service.dto.SongCreatedResponse;
import com.microservices.song.service.dto.SongDto;
import com.microservices.song.service.dto.SongsDeletedResponse;
import com.microservices.song.service.exception.NotFoundException;
import com.microservices.song.service.mapper.SongMapper;
import com.microservices.song.service.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SongService {

    private final SongRepository songRepository;
    private final SongMapper songMapper;

    public SongService(SongRepository songRepository, SongMapper songMapper) {
        this.songRepository = songRepository;
        this.songMapper = songMapper;
    }

    public SongCreatedResponse createSong(SongDto songDto) {
        var songEntity = songMapper.toEntity(songDto);
        var savedSongEntity = songRepository.save(songEntity);
        return new SongCreatedResponse(savedSongEntity.getId());
    }

    public SongDto getSong(Long id) {
        return songRepository.findById(id)
            .map(songMapper::toDto)
            .orElseThrow(() -> new NotFoundException(String.format("Song with id %s not found", id)));
    }

    public SongsDeletedResponse deleteSongs(List<Long> ids) {
        var deletedIds = songRepository.findAllById(ids)
            .stream()
            .map(song -> {
                songRepository.deleteById(song.getId());
                return song.getId();
            })
            .collect(Collectors.toList());

        return new SongsDeletedResponse(deletedIds);
    }
}
