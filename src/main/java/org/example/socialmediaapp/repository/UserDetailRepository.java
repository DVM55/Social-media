package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    Optional<UserDetail> findByAccountId(Long accountId);
}
