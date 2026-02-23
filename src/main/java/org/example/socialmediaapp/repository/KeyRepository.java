package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.Key;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeyRepository extends JpaRepository<Key, Long> {
    Optional<Key> findByAccount_Id(Long id);
    Optional<Key> findByRefreshToken(String username);
    void deleteByAccount_Id(Long id);
}
