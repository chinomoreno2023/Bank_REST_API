package com.example.bankcards.repository.card;

import com.example.bankcards.entity.card.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    List<Card> findByOwnerId(Long userId);
    Page<Card> findByOwnerId(Long userId, Pageable pageable);
    Page<Card> findByOwnerIdAndLastFourDigitsContaining(Long userId, String lastFourDigits, Pageable pageable);
    Page<Card> findAll(Pageable pageable);
}
