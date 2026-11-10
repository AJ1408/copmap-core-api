package in.copmap.report.model;

import in.copmap.alert.domain.Alert;
import in.copmap.common.audit.AuditLog;
import in.copmap.operations.assignment.domain.Assignment;
import in.copmap.operations.patrol.domain.Patrol;
import in.copmap.operations.patrol.domain.PatrolWaypoint;
import java.util.List;

public record PatrolReportModel(
        Patrol patrol,
        List<PatrolWaypoint> waypoints,
        List<Assignment> assignments,
        List<Alert> alerts,
        List<AuditLog> auditLogs
) {}

