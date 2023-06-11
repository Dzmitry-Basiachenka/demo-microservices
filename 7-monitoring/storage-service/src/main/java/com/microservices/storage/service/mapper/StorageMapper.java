package com.microservices.storage.service.mapper;

import com.microservices.storage.service.dto.StorageDto;
import com.microservices.storage.service.entity.StorageEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StorageMapper {

    StorageDto toDto(StorageEntity storageEntity);

    StorageEntity toEntity(StorageDto storageDto);
}
