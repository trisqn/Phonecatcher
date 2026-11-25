package com.example.javaproject.warning.controller;

import com.example.javaproject.warning.dto.WarningResponse;
import com.example.javaproject.warning.service.WarningQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes public warning lookup endpoints.
 */
@RestController
@RequestMapping("/api/v1/warnings")
@Tag(name = "Warnings", description = "Public warning information generated from community reports.")
public class WarningController {

    private final WarningQueryService warningQueryService;

    /**
     * Creates the controller with its warning query service.
     *
     * @param warningQueryService warning lookup operations
     */
    public WarningController(WarningQueryService warningQueryService) {
        this.warningQueryService = warningQueryService;
    }

    /**
     * Returns active warnings without reporter identities or report descriptions.
     *
     * @return active public warning summaries
     */
    @GetMapping
    @Operation(
            summary = "List active warnings",
            description = "Public endpoint returning active warnings only. Reporter identities and report descriptions are not included."
    )
    public List<WarningResponse> getActiveWarnings() {
        return warningQueryService.getActiveWarnings();
    }
}
