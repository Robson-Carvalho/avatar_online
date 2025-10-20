package com.avatar.avatar_online.repository;

import com.avatar.avatar_online.models.Card;
import com.avatar.avatar_online.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findAllByUserIsNull();
}
