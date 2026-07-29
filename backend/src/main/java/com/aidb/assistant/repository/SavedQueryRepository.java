package com.aidb.assistant.repository;

import com.aidb.assistant.entity.SavedQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedQueryRepository extends JpaRepository<SavedQuery, Long> {
    List<SavedQuery> findAllByOrderByCreatedAtDesc();
    List<SavedQuery> findByCategory(String category);
}
