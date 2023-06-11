package com.microservices.song.service.mapper;

import com.microservices.song.service.dto.SongDto;
import com.microservices.song.service.entity.SongEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SongMapper {

    SongDto toDto(SongEntity songEntity);

    SongEntity toEntity(SongDto songDto);
}
