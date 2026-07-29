package com.aidb.assistant.controller;

import com.aidb.assistant.dto.SavedQueryDTO;
import com.aidb.assistant.service.SavedQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-queries")
public class SavedQueryController {

    private final SavedQueryService savedQueryService;

    public SavedQueryController(SavedQueryService savedQueryService) {
        this.savedQueryService = savedQueryService;
    }

    @GetMapping
    public ResponseEntity<List<SavedQueryDTO>> getAllSavedQueries() {
        return ResponseEntity.ok(savedQueryService.getAllSavedQueries());
    }

    @PostMapping
    public ResponseEntity<SavedQueryDTO> saveQuery(@RequestBody SavedQueryDTO queryDTO) {
        return ResponseEntity.ok(savedQueryService.saveQuery(queryDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavedQuery(@PathVariable Long id) {
        savedQueryService.deleteSavedQuery(id);
        return ResponseEntity.ok().build();
    }
}
