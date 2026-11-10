#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080/api/v1}"

echo "Connecting to ${BASE_URL}..."

PLANNER_RES=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"badgeNo":"PLN001","password":"Copmap@123"}')

HTTP_CODE=$(echo "${PLANNER_RES}" | tail -n1)
BODY=$(echo "${PLANNER_RES}" | sed '$d')

if [ "${HTTP_CODE}" != "200" ]; then
  echo "Error: Failed to connect or login (HTTP ${HTTP_CODE})."
  echo "Server response: ${BODY}"
  echo "Please verify the CopMap backend is running and accessible at ${BASE_URL}."
  exit 1
fi

PLANNER_TOKEN=$(echo "${BODY}" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "${PLANNER_TOKEN}" ]; then
  echo "Error: accessToken not found in response: ${BODY}"
  exit 1
fi

echo "Authenticated as PLN001"

PATROL_RES=$(curl -s -X POST "${BASE_URL}/patrols" \
  -H "Authorization: Bearer ${PLANNER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Connaught Place Night Beat",
    "type": "FOOT",
    "stationId": 1,
    "shiftStart": "2026-11-10T20:00:00Z",
    "shiftEnd": "2026-11-11T04:00:00Z",
    "geofenceRadiusM": 200,
    "waypoints": [
      {"seq": 1, "name": "Block A Inner Circle", "lat": 28.6328, "lng": 77.2197},
      {"seq": 2, "name": "Block D Inner Circle", "lat": 28.6315, "lng": 77.2212},
      {"seq": 3, "name": "Palika Gate 3", "lat": 28.6304, "lng": 77.2185}
    ]
  }')

PATROL_ID=$(echo "${PATROL_RES}" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "${PATROL_ID}" ]; then
  echo "Error: Failed to create patrol: ${PATROL_RES}"
  exit 1
fi

echo "Created patrol ${PATROL_ID}"

curl -s -f -X POST "${BASE_URL}/patrols/${PATROL_ID}/publish" \
  -H "Authorization: Bearer ${PLANNER_TOKEN}" > /dev/null

echo "Published patrol ${PATROL_ID}"

curl -s -f -X POST "${BASE_URL}/patrols/${PATROL_ID}/assignments" \
  -H "Authorization: Bearer ${PLANNER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "officerIds": [3, 4],
    "startsAt": "2026-11-10T20:00:00Z",
    "endsAt": "2026-11-11T04:00:00Z"
  }' > /dev/null

echo "Assigned officers to patrol ${PATROL_ID}"

OFFICER_RES=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"badgeNo":"OFF001","password":"Copmap@123"}')

OFFICER_TOKEN=$(echo "${OFFICER_RES}" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

curl -s -f -X POST "${BASE_URL}/patrols/${PATROL_ID}/start" \
  -H "Authorization: Bearer ${OFFICER_TOKEN}" > /dev/null

echo "Officer OFF001 started duty on patrol ${PATROL_ID}"

NOW=$(date -u +%Y-%m-%dT%H:%M:%SZ)
curl -s -f -X POST "${BASE_URL}/locations" \
  -H "Authorization: Bearer ${OFFICER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"patrolId\": ${PATROL_ID},
    \"pings\": [
      {\"lat\": 28.6328, \"lng\": 77.2197, \"accuracy\": 4.5, \"speed\": 1.2, \"recordedAt\": \"${NOW}\"}
    ]
  }" > /dev/null

echo "Sent live ping batch for patrol ${PATROL_ID}"
echo "Demo patrol created successfully: patrolId=${PATROL_ID}"
