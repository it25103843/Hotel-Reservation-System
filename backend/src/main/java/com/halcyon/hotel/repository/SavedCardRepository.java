package com.halcyon.hotel.repository;

import com.halcyon.hotel.entity.SavedCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedCardRepository extends JpaRepository<SavedCard, Long> {
    List<SavedCard> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    void deleteByAccountId(Long accountId);
}
