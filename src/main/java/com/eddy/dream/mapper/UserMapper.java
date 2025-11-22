package com.eddy.dream.mapper;

import com.eddy.dream.dto.response.UserResponse;
import com.eddy.dream.entity.UserEntity;
import org.mapstruct.*;

/**
 * User Mapper - Uses MapStruct for object conversion
 * Entity <-> DTO
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {
    
    /**
     * Entity -> UserResponse
     */
    @Mapping(source = "status", target = "status")
    UserResponse entityToResponse(UserEntity entity);
}

