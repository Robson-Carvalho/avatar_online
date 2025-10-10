package com.avatar.avatar_online.repository;

import com.avatar.avatar_online.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    @Query("SELECT u FROM app_user u WHERE u.email = :email OR u.nickname = :nickname")
    Optional<User> findByEmailOrNickname(@Param("email") String email,
                                         @Param("nickname") String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}