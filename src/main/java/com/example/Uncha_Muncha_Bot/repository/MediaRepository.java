package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.MediaEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface MediaRepository extends CrudRepository<MediaEntity,Integer> {
    @Query("from MediaEntity where ownerId=?1")
    Iterable<MediaEntity> getByOwnerId(Long ownerId);
}
