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
    public ResponseEntity<String> seedDatabase() {
        seederService.seedDatabase();
        return ResponseEntity.ok("Repository successfully seeded with initial data.");
    }

    @PostMapping("/seed-rbac")
    public ResponseEntity<String> seedRbac() {
        seederService.seedRolesAndPermissionsOnly();
        return ResponseEntity.ok("Roles and permissions re-seeded. All existing users have been re-assigned their roles.");
    }

    @PostMapping("/unseed")
    public ResponseEntity<String> unseedDatabase() {
        seederService.clearDatabase();
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
