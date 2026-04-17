package com.carebridge.backend.seeder;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seeder")
public class SeederController {

    private final SeederService seederService;

    public SeederController(SeederService seederService) {
        this.seederService = seederService;
    }

    @PostMapping("/seed")
    public ResponseEntity<String> seedRepository() {
        seederService.seedRepository();
        return ResponseEntity.ok("Repository successfully seeded with 15 fully assigned patients.");
    }

    @PostMapping("/unseed")
    public ResponseEntity<String> unseedRepository() {
        seederService.unseedRepository();
        return ResponseEntity.ok("All data has been cleared from the repository.");
    }

    @PostMapping("/start-loop")
    public ResponseEntity<String> startLoop() {
        seederService.startLoop();
        return ResponseEntity.ok("Async loop started! WebSockets should start receiving data.");
    }

    @PostMapping("/stop-loop")
    public ResponseEntity<String> stopLoop() {
        seederService.stopLoop();
        return ResponseEntity.ok("Async loop stopped.");
    }
}
