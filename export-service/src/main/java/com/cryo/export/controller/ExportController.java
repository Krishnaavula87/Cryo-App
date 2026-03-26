package com.cryo.export.controller;

import com.cryo.export.service.ExportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/export")
public class ExportController {
    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/freezers/{freezerId}/csv")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable("freezerId") String freezerId,
            @RequestParam(name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(name = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(name = "channel", required = false) String channel,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION)
            String auth
    ) throws Exception {

        validateDateRange(from, to); // 🔥 IMPORTANT

        byte[] file = exportService.exportToCsv(
                freezerId,
                from.atStartOfDay(),
                to.atTime(LocalTime.MAX),
                channel,
                auth
        );

        String currentTimestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        String filename = String.format(
                "export_%s_%s_to_%s_%s.csv",
                freezerId,
                from,
                to,
                currentTimestamp
        );

        return ResponseEntity.ok()
                .header("Cache-Control", "no-store") // 🔥 prevent caching
                .header("Pragma", "no-cache")
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .body(file);
    }

    @GetMapping("/freezers/{freezerId}/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable("freezerId") String freezerId,
            @RequestParam(name = "from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(name = "to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(name = "channel", required = false) String channel,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION)
            String auth
    ) throws Exception {

        validateDateRange(from, to); // 🔥 IMPORTANT

        byte[] file = exportService.exportToPdf(
                freezerId,
                from.atStartOfDay(),
                to.atTime(LocalTime.MAX),
                channel,
                auth
        );

        String currentTimestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        String filename = String.format(
                "export_%s_%s_to_%s_%s.pdf",
                freezerId,
                from,
                to,
                currentTimestamp
        );

        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .header("Pragma", "no-cache")
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .body(file);
    }


    private void validateDateRange(LocalDate from, LocalDate to) {

        if (to.isBefore(from)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        if (from.plusMonths(3).isBefore(to)) {
            throw new IllegalArgumentException(
                    "Maximum export range is 3 months only.");
        }
    }

}
