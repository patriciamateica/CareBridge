package com.carebridge.backend.seeder;

import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SeederGraphController {

    private final SeederService seederService;

    public SeederGraphController(SeederService seederService) {
        this.seederService = seederService;
    }

    @MutationMapping
    public Boolean seedRepository() {
        seederService.seedRepository();
        return true;
    }

    @MutationMapping
    public Boolean unseedRepository() {
        seederService.unseedRepository();
        return true;
    }

    @MutationMapping
    public Boolean startSeederLoop() {
        seederService.startLoop();
        return true;
    }

    @MutationMapping
    public Boolean stopSeederLoop() {
        seederService.stopLoop();
        return true;
    }
}

