# CopMap - Domain Research & Operational Context

## 1. Domain Background

Police patrolling, security bandobast, and highway/urban nakabandi represent the operational backbone of modern law enforcement in India and municipal police jurisdictions. Despite the mission-critical nature of these field operations, many control rooms still rely on manual GD (General Diary) registers, ad-hoc WhatsApp groups, and verbal walkie-talkie check-ins.

CopMap provides an integrated, real-time command, control, and audit system tailored to the operational realities of field policing.

---

## 2. Core Operation Types

### 2.1 Beat & Mobile Patrolling
- **Beat Patrols (Foot):** Constables and Head Constables patrol designated urban neighborhoods, commercial hubs, and sensitive residential lanes. Focus is on high-visibility deterrence, foot-checks of night-patrol waypoints, and neighborhood safety.
- **Mobile/PCR Patrols (Vehicle):** Quick-reaction police control room (PCR) vans and motorcycle patrols cover arterial routes, expressways, and inter-sector boundaries. Focus is on rapid response times, route adherence, and high-speed geofence monitoring.
- **Key Challenges:** Route skipping, officer unreachability in radio shadows, ghost patrolling (falsified paper logs), and delayed incident escalation.

### 2.2 Bandobast (Event & VIP Security Deployments)
- **Deployment Structure:** Large-scale security mobilizations during religious festivals, protests, sporting events, or VIP motorcades.
- **Sectors and Points:** The venue or route is carved into discrete sectors with fixed duty points (barricades, stage gates, perimeter watch points). Each point mandates a minimum required strength (headcount and rank distribution).
- **Commander Oversight:** A designated sector commander or Deputy Superintendent of Police (DySP) tracks roll calls, shift handovers, and static perimeter integrity.

### 2.3 Nakabandi (Static Checkpoint Enforcement)
- **Roadblock Checkpoints:** Static cordons placed on strategic transit bottlenecks, interstate borders, and drunk-driving choke points.
- **Operation Cycle:** Typically executed for defined duration windows (e.g., 23:00 to 03:00). Officers inspect suspect vehicles, verify license plates, and transmit SOS alerts if a vehicle breaks through the barrier.

---

## 3. Organizational Roles and Command Hierarchy

| Role | Field Rank / Persona | Core Responsibilities | System Capabilities |
|---|---|---|---|
| `OFFICER` | Constable, Head Constable, Sub-Inspector (SI) | Ground execution, waypoint visits, vehicle checks | View assigned duties, start/end shift, send location pings, trigger panic SOS |
| `PLANNER` | Station House Officer (SHO), Inspector | Station-level roster scheduling, route design, patrol closure | Create and publish patrols/bandobasts, allocate personnel, review post-shift reports |
| `SUPERVISOR` | ACP, DySP, SP, Control Room Duty Officer | Multi-station jurisdiction monitoring, command oversight | Real-time map tracking across stations, acknowledge emergency alerts, incident override |

---

## 4. Ground Realities & Technical Mitigations

### 4.1 Flaky Connectivity & High Latency
- Field officers operate in basements, dense narrow alleys, and remote outposts where cell tower reception drops intermittently.
- **Technical Mitigation:** Officers do not maintain persistent raw TCP/WebSocket connections. Mobile clients buffer GPS pings locally and transmit batches via HTTP POST (`/api/v1/tracking/pings`). Pings carry client-generated `recordedAt` timestamps. A unique composite database index (`officer_id`, `patrol_id`, `recorded_at`) coupled with `ON CONFLICT DO NOTHING` ensures idempotent retries without data duplication.

### 4.2 Ghost Patrolling & Geofence Adherence
- Officers historically logged visits without physically walking the beat.
- **Technical Mitigation:** Routes are defined by an ordered list of waypoints. Each ping computes the orthogonal point-to-segment distance to the route polyline. Breaches beyond the station's configured geofence radius (e.g., 200 m) automatically generate deduped alert events published to the live supervisor dashboard.

### 4.3 Double-Booking & Fatigue Prevention
- Shift planners managing hundreds of personnel often accidentally double-book officers across concurrent patrols or bandobast duties.
- **Technical Mitigation:** Strict temporal interval intersection query (`starts_at < new_end AND ends_at > new_start`) prevents assigning any officer who already has an `ASSIGNED` or `ON_DUTY` slot.

### 4.4 Chain of Custody & Legal Defensibility
- In post-incident investigations (court trials, internal inquiries, complaints against police), manual registers are scrutinized for tampering.
- **Technical Mitigation:** Tamper-evident, append-only `audit_logs` records every transition (creation, publishing, duty start, SOS trigger, alert acknowledgement, patrol closure). Closed patrols generate immutable PDF evidence reports via OpenPDF with complete officer timelines, waypoint logs, and closure remarks.

