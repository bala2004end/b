package com.aidb.assistant.service;

import com.aidb.assistant.dto.SavedQueryDTO;
import com.aidb.assistant.entity.SavedQuery;
import com.aidb.assistant.repository.SavedQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavedQueryService {

    private final SavedQueryRepository savedQueryRepository;

    public SavedQueryService(SavedQueryRepository savedQueryRepository) {
        this.savedQueryRepository = savedQueryRepository;
    }

    public SavedQueryDTO saveQuery(SavedQueryDTO dto) {
        SavedQuery query = SavedQuery.builder()
                .title(dto.getTitle())
                .sqlQuery(dto.getSqlQuery())
                .category(dto.getCategory() != null ? dto.getCategory() : "General")
                .description(dto.getDescription())
                .build();

        SavedQuery saved = savedQueryRepository.save(query);
        return mapToDTO(saved);
    }

    public List<SavedQueryDTO> getAllSavedQueries() {
        return savedQueryRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteSavedQuery(Long id) {
        savedQueryRepository.deleteById(id);
    }

    private SavedQueryDTO mapToDTO(SavedQuery q) {
        return SavedQueryDTO.builder()
                .id(q.getId())
                .title(q.getTitle())
                .sqlQuery(q.getSqlQuery())
                .category(q.getCategory())
                .description(q.getDescription())
                .createdAt(q.getCreatedAt())
                .build();
    }
}
