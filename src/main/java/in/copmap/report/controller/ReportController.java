package in.copmap.report.controller;

import in.copmap.report.service.PatrolReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patrols")
@RequiredArgsConstructor
public class ReportController {

    private final PatrolReportService reportService;

    @GetMapping(value = "/{id}/report.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('PLANNER','SUPERVISOR')")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) {
        byte[] pdfBytes = reportService.build(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"patrol-" + id + "-report.pdf\"");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}

