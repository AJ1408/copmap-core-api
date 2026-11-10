# CopMap - API Contracts Specification

Base URL: `http://localhost:8080/api/v1`

All responses follow the unified envelope:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2026-11-10T12:00:00Z"
}
```

When an operation fails:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ASSIGNMENT_CONFLICT",
    "message": "Officer 101 has an overlapping assignment"
  },
  "timestamp": "2026-11-10T12:00:00Z"
}
```

---

## 1. Authentication Endpoints

### 1.1 POST `/auth/login`
Authenticates a field officer, station planner, or supervisor using badge credentials.

- **Access:** Public
- **Request Body:**
```json
{
  "badgeNo": "PLN001",
  "password": "Copmap@123"
}
```
- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "badgeNo": "PLN001",
      "fullName": "Inspector Sharma",
      "rank": "Inspector",
      "role": "PLANNER",
      "stationId": 1
    }
  },
  "error": null,
  "timestamp": "2026-11-10T12:00:00Z"
}
```

### 1.2 GET `/auth/me`
Retrieves identity and role information for the active JWT session.

- **Access:** `OFFICER`, `PLANNER`, `SUPERVISOR`
- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "badgeNo": "PLN001",
    "fullName": "Inspector Sharma",
    "rank": "Inspector",
    "role": "PLANNER",
    "stationId": 1
  },
  "error": null,
  "timestamp": "2026-11-10T12:00:00Z"
}
```

### 1.3 POST `/auth/logout`
Revokes the current JWT by adding its unique token ID (`jti`) to the Redis blacklist.

- **Access:** `OFFICER`, `PLANNER`, `SUPERVISOR`
- **Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "error": null,
  "timestamp": "2026-11-10T12:00:00Z"
}
```

---

## 2. Patrol Management Endpoints

### 2.1 POST `/patrols`
Creates a new patrol in `DRAFT` state with geofence parameters and waypoints.

- **Access:** `PLANNER`, `SUPERVISOR`
- **Request Body:**
```json
{
  "title": "Connaught Place Night Beat",
  "type": "FOOT",
  "stationId": 1,
  "shiftStart": "2026-11-10T20:00:00Z",
  "shiftEnd": "2026-11-11T04:00:00Z",
  "geofenceRadiusM": 200,
  "waypoints": [
    {
      "seq": 1,
      "name": "Block A Inner Circle",
      "lat": 28.6328,
      "lng": 77.2197
    },
    {
      "seq": 2,
      "name": "Palika Gate 3",
      "lat": 28.6304,
      "lng": 77.2185
    }
  ]
}
```
- **Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Connaught Place Night Beat",
    "type": "FOOT",
    "stationId": 1,
    "status": "DRAFT",
    "shiftStart": "2026-11-10T20:00:00Z",
    "shiftEnd": "2026-11-11T04:00:00Z",
    "geofenceRadiusM": 200,
    "createdBy": 1,
    "waypoints": [
      {
        "id": 1,
        "seq": 1,
        "name": "Block A Inner Circle",
        "lat": 28.6328,
        "lng": 77.2197
      },
      {
        "id": 2,
        "seq": 2,
        "name": "Palika Gate 3",
        "lat": 28.6304,
        "lng": 77.2185
      }
    ]
  },
  "error": null,
  "timestamp": "2026-11-10T12:00:00Z"
}
```

### 2.2 POST `/patrols/{id}/publish`
Transitions patrol status from `DRAFT` to `PLANNED`.

- **Access:** `PLANNER`, `SUPERVISOR`
- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "PLANNED"
  },
  "error": null,
  "timestamp": "2026-11-10T12:00:00Z"
}
```

### 2.3 POST `/patrols/{id}/assignments`
Assigns officers to a patrol with strict conflict validation.

- **Access:** `PLANNER`, `SUPERVISOR`
- **Request Body:**
```json
{
  "officerIds": [3, 4],
  "startsAt": "2026-11-10T20:00:00Z",
  "endsAt": "2026-11-11T04:00:00Z"
}
```
- **Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "officerId": 3,
      "operationType": "PATROL",
      "operationId": 1,
      "status": "ASSIGNED",
      "startsAt": "2026-11-10T20:00:00Z",
      "endsAt": "2026-11-11T04:00:00Z"
    }
  ],
  "error": null,
  "timestamp": "2026-11-10T12:00:00Z"
}
```

### 2.4 POST `/patrols/{id}/duty/start`
Field officer marks duty commencement. Updates assignment to `ON_DUTY` and patrol to `ACTIVE`.

- **Access:** `OFFICER`
- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "assignmentId": 1,
    "status": "ON_DUTY",
    "startedAt": "2026-11-10T20:02:15Z"
  },
  "error": null,
  "timestamp": "2026-11-10T20:02:15Z"
}
```

### 2.5 POST `/patrols/{id}/duty/end`
Field officer marks duty completion.

- **Access:** `OFFICER`
- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "assignmentId": 1,
    "status": "COMPLETED",
    "endedAt": "2026-11-11T04:01:00Z"
  },
  "error": null,
  "timestamp": "2026-11-11T04:01:00Z"
}
```

### 2.6 POST `/patrols/{id}/close`
Closes the patrol with remarks.

- **Access:** `PLANNER`, `SUPERVISOR`
- **Request Body:**
```json
{
  "remarks": "Shift completed without perimeter incidents. 2 minor traffic checks conducted."
}
```
- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "COMPLETED",
    "closedBy": 1,
    "closeRemarks": "Shift completed without perimeter incidents. 2 minor traffic checks conducted."
  },
  "error": null,
  "timestamp": "2026-11-11T04:05:00Z"
}
```

### 2.7 GET `/patrols/{id}/report.pdf`
Generates and downloads a complete OpenPDF report.

- **Access:** `PLANNER`, `SUPERVISOR`
- **Headers:** `Content-Type: application/pdf`, `Content-Disposition: inline; filename="patrol-1-report.pdf"`
- **Response (200 OK):** Raw binary PDF bytes.

---

## 3. Tracking & Live Telemetry Endpoints

### 3.1 POST `/tracking/pings`
High-throughput batch ingest of field coordinates from mobile clients.

- **Access:** `OFFICER`
- **Request Body:**
```json
{
  "patrolId": 1,
  "pings": [
    {
      "lat": 28.6328,
      "lng": 77.2197,
      "accuracy": 4.5,
      "speed": 1.2,
      "recordedAt": "2026-11-10T20:05:00Z"
    },
    {
      "lat": 28.6321,
      "lng": 77.2202,
      "accuracy": 5.0,
      "speed": 1.4,
      "recordedAt": "2026-11-10T20:06:00Z"
    }
  ]
}
```
- **Response (200 OK):**
```json
{
  "success": true,
  "data": 2,
  "error": null,
  "timestamp": "2026-11-10T20:06:01Z"
}
```

### 3.2 GET `/tracking/patrols/{id}/live`
Fetches the current real-time snapshot of all officer positions for a patrol.

- **Access:** `PLANNER`, `SUPERVISOR`
- **Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "officerId": 3,
      "lat": 28.6321,
      "lng": 77.2202,
      "lastSeenAt": "2026-11-10T20:06:00Z"
    }
  ],
  "error": null,
  "timestamp": "2026-11-10T20:06:05Z"
}
```

---

## 4. Alerts & Notifications

### 4.1 POST `/alerts/sos`
Field officer triggers an emergency panic SOS alarm.

- **Access:** `OFFICER`
- **Request Body:**
```json
{
  "patrolId": 1,
  "lat": 28.6328,
  "lng": 77.2197,
  "message": "Armed confrontation at Block A. Immediate backup requested."
}
```
- **Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "type": "SOS",
    "severity": "HIGH",
    "operationId": 1,
    "officerId": 3,
    "message": "Armed confrontation at Block A. Immediate backup requested.",
    "status": "OPEN",
    "createdAt": "2026-11-10T20:10:00Z"
  },
  "error": null,
  "timestamp": "2026-11-10T20:10:00Z"
}
```

### 4.2 POST `/alerts/{id}/ack`
Control room supervisor acknowledges an open alert.

- **Access:** `PLANNER`, `SUPERVISOR`
- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "ACKNOWLEDGED",
    "ackedBy": 2,
    "ackedAt": "2026-11-10T20:11:00Z"
  },
  "error": null,
  "timestamp": "2026-11-10T20:11:00Z"
}
```

---

## 5. Bandobast & Nakabandi Stubs (501)

### 5.1 POST `/bandobast`
- **Access:** `PLANNER`, `SUPERVISOR`
- **Response (501 Not Implemented):**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "NOT_IMPLEMENTED",
    "message": "Bandobast is designed but not implemented"
  },
  "timestamp": "2026-11-10T12:00:00Z"
}
```

### 5.2 POST `/bandobast/{id}/points/{pointId}/checkin`
- **Access:** `OFFICER`
- **Response (501 Not Implemented):**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "NOT_IMPLEMENTED",
    "message": "Bandobast is designed but not implemented"
  },
  "timestamp": "2026-11-10T12:00:00Z"
}
```

