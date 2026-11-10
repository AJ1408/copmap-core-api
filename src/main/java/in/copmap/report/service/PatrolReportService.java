package in.copmap.report.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import in.copmap.alert.domain.Alert;
import in.copmap.alert.repository.AlertRepository;
import in.copmap.common.audit.AuditLog;
import in.copmap.common.audit.AuditLogRepository;
import in.copmap.common.exception.BusinessException;
import in.copmap.common.exception.ErrorCode;
import in.copmap.operations.assignment.domain.Assignment;
import in.copmap.operations.assignment.domain.OperationType;
import in.copmap.operations.assignment.repository.AssignmentRepository;
import in.copmap.operations.patrol.domain.Patrol;
import in.copmap.operations.patrol.domain.PatrolWaypoint;
import in.copmap.operations.patrol.repository.PatrolRepository;
import in.copmap.report.model.PatrolReportModel;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatrolReportService {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
    private static final Color HEADER_BG = new Color(41, 128, 185);
    private static final Color ALT_ROW_BG = new Color(245, 247, 250);

    private final PatrolRepository patrolRepository;
    private final AssignmentRepository assignmentRepository;
    private final AlertRepository alertRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public PatrolReportModel loadModel(Long patrolId) {
        Patrol patrol = patrolRepository.findById(patrolId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Patrol not found: " + patrolId));
        List<PatrolWaypoint> waypoints = patrol.getWaypoints() != null ? patrol.getWaypoints() : List.of();
        List<Assignment> assignments = assignmentRepository.findByOperationTypeAndOperationId(
                OperationType.PATROL, patrolId);
        List<Alert> alerts = alertRepository.findByOperationTypeAndOperationIdOrderByCreatedAtDesc(
                "PATROL", patrolId);
        List<AuditLog> auditLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                "PATROL", patrolId);

        return new PatrolReportModel(patrol, waypoints, assignments, alerts, auditLogs);
    }

    @Transactional(readOnly = true)
    public byte[] build(Long patrolId) {
        PatrolReportModel model = loadModel(patrolId);
        return generatePdf(model);
    }

    public byte[] generatePdf(PatrolReportModel model) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            addHeader(document, model.patrol());
            addPatrolDetails(document, model.patrol());
            addWaypointsSection(document, model.waypoints());
            addAssignmentsSection(document, model.assignments());
            addAlertsSection(document, model.alerts());
            addAuditSection(document, model.auditLogs());
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to generate patrol report PDF");
        }
    }

    private void addHeader(Document document, Patrol patrol) throws DocumentException {
        Paragraph title = new Paragraph("Patrol Report: " + patrol.getTitle(), TITLE_FONT);
        title.setSpacingAfter(4);
        document.add(title);

        Paragraph sub = new Paragraph("Patrol ID: " + patrol.getId() + " | Status: " + patrol.getStatus()
                + " | Type: " + patrol.getType(), BODY_FONT);
        sub.setSpacingAfter(14);
        document.add(sub);
    }

    private void addPatrolDetails(Document document, Patrol patrol) throws DocumentException {
        document.add(new Paragraph("Overview", SECTION_FONT));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(12);

        addKeyValueRow(table, "Station ID", String.valueOf(patrol.getStationId()));
        addKeyValueRow(table, "Shift Start", String.valueOf(patrol.getShiftStart()));
        addKeyValueRow(table, "Shift End", String.valueOf(patrol.getShiftEnd()));
        addKeyValueRow(table, "Geofence Radius", patrol.getGeofenceRadiusM() + " meters");
        addKeyValueRow(table, "Created By", String.valueOf(patrol.getCreatedBy()));
        addKeyValueRow(table, "Closed By", patrol.getClosedBy() != null ? String.valueOf(patrol.getClosedBy()) : "-");
        addKeyValueRow(table, "Close Remarks", patrol.getCloseRemarks() != null ? patrol.getCloseRemarks() : "-");

        document.add(table);
    }

    private void addWaypointsSection(Document document, List<PatrolWaypoint> waypoints) throws DocumentException {
        document.add(new Paragraph("Waypoints (" + waypoints.size() + ")", SECTION_FONT));
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(12);
        table.setWidths(new float[]{10, 40, 25, 25});

        table.addCell(createHeaderCell("Seq"));
        table.addCell(createHeaderCell("Name"));
        table.addCell(createHeaderCell("Latitude"));
        table.addCell(createHeaderCell("Longitude"));

        for (int i = 0; i < waypoints.size(); i++) {
            PatrolWaypoint wp = waypoints.get(i);
            Color bg = (i % 2 == 1) ? ALT_ROW_BG : Color.WHITE;
            table.addCell(createBodyCell(String.valueOf(wp.getSeq()), bg));
            table.addCell(createBodyCell(wp.getName() != null ? wp.getName() : "-", bg));
            table.addCell(createBodyCell(String.valueOf(wp.getLat()), bg));
            table.addCell(createBodyCell(String.valueOf(wp.getLng()), bg));
        }
        document.add(table);
    }

    private void addAssignmentsSection(Document document, List<Assignment> assignments) throws DocumentException {
        document.add(new Paragraph("Assigned Officers (" + assignments.size() + ")", SECTION_FONT));
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(12);
        table.setWidths(new float[]{15, 20, 20, 22, 23});

        table.addCell(createHeaderCell("Officer ID"));
        table.addCell(createHeaderCell("Status"));
        table.addCell(createHeaderCell("Started At"));
        table.addCell(createHeaderCell("Ended At"));
        table.addCell(createHeaderCell("Scheduled Window"));

        for (int i = 0; i < assignments.size(); i++) {
            Assignment a = assignments.get(i);
            Color bg = (i % 2 == 1) ? ALT_ROW_BG : Color.WHITE;
            table.addCell(createBodyCell(String.valueOf(a.getOfficerId()), bg));
            table.addCell(createBodyCell(String.valueOf(a.getStatus()), bg));
            table.addCell(createBodyCell(a.getStartedAt() != null ? String.valueOf(a.getStartedAt()) : "-", bg));
            table.addCell(createBodyCell(a.getEndedAt() != null ? String.valueOf(a.getEndedAt()) : "-", bg));
            table.addCell(createBodyCell(a.getStartsAt() + " to " + a.getEndsAt(), bg));
        }
        document.add(table);
    }

    private void addAlertsSection(Document document, List<Alert> alerts) throws DocumentException {
        document.add(new Paragraph("Alert Timeline (" + alerts.size() + ")", SECTION_FONT));
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(12);
        table.setWidths(new float[]{22, 18, 15, 30, 15});

        table.addCell(createHeaderCell("Time"));
        table.addCell(createHeaderCell("Type"));
        table.addCell(createHeaderCell("Severity"));
        table.addCell(createHeaderCell("Message"));
        table.addCell(createHeaderCell("Status"));

        for (int i = 0; i < alerts.size(); i++) {
            Alert a = alerts.get(i);
            Color bg = (i % 2 == 1) ? ALT_ROW_BG : Color.WHITE;
            table.addCell(createBodyCell(String.valueOf(a.getCreatedAt()), bg));
            table.addCell(createBodyCell(String.valueOf(a.getType()), bg));
            table.addCell(createBodyCell(String.valueOf(a.getSeverity()), bg));
            table.addCell(createBodyCell(a.getMessage() != null ? a.getMessage() : "-", bg));
            table.addCell(createBodyCell(String.valueOf(a.getStatus()), bg));
        }
        document.add(table);
    }

    private void addAuditSection(Document document, List<AuditLog> auditLogs) throws DocumentException {
        document.add(new Paragraph("Audit Trail (" + auditLogs.size() + ")", SECTION_FONT));
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(12);
        table.setWidths(new float[]{25, 25, 50});

        table.addCell(createHeaderCell("Time"));
        table.addCell(createHeaderCell("Action"));
        table.addCell(createHeaderCell("Actor ID"));

        for (int i = 0; i < auditLogs.size(); i++) {
            AuditLog log = auditLogs.get(i);
            Color bg = (i % 2 == 1) ? ALT_ROW_BG : Color.WHITE;
            table.addCell(createBodyCell(String.valueOf(log.getCreatedAt()), bg));
            table.addCell(createBodyCell(log.getAction(), bg));
            table.addCell(createBodyCell(log.getActorId() != null ? String.valueOf(log.getActorId()) : "SYSTEM", bg));
        }
        document.add(table);
    }

    private void addKeyValueRow(PdfPTable table, String key, String value) {
        PdfPCell keyCell = new PdfPCell(new Phrase(key, SECTION_FONT));
        keyCell.setBackgroundColor(ALT_ROW_BG);
        keyCell.setPadding(5);
        table.addCell(keyCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, BODY_FONT));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createBodyCell(String text, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BODY_FONT));
        cell.setBackgroundColor(bg);
        cell.setPadding(4);
        return cell;
    }
}

