#!/usr/bin/env python3
"""Simulate field officer movement and GPS ping telemetry."""

import argparse
from datetime import datetime, timezone
import json
import sys
import time
import urllib.error
import urllib.request


def parse_args():
    parser = argparse.ArgumentParser(description="Simulate officer GPS telemetry")
    parser.add_argument("--base-url", default="http://localhost:8080/api/v1", help="API base URL")
    parser.add_argument("--badge-no", default="OFF001", help="Officer badge number")
    parser.add_argument("--password", default="Copmap@123", help="Officer password")
    parser.add_argument("--patrol-id", type=int, default=1, help="Target patrol ID")
    parser.add_argument("--interval", type=float, default=2.0, help="Seconds between pings")
    parser.add_argument("--breach", action="store_true", help="Simulate a geofence breach")
    return parser.parse_args()


def post_json(url, payload, token=None):
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, method="POST")
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(req) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        err_body = e.read().decode("utf-8")
        sys.stderr.write(f"HTTP {e.code} error: {err_body}\n")
        raise


def login(base_url, badge_no, password):
    url = f"{base_url}/auth/login"
    res = post_json(url, {"badgeNo": badge_no, "password": password})
    return res["data"]["accessToken"]


def generate_coordinates(breach=False):
    coords = [
        (28.6328, 77.2197),
        (28.6325, 77.2202),
        (28.6320, 77.2208),
        (28.6315, 77.2212),
        (28.6309, 77.2200),
        (28.6304, 77.2185),
    ]
    if breach:
        coords.append((28.6400, 77.2350))
    return coords


def send_ping(base_url, token, patrol_id, lat, lng):
    url = f"{base_url}/locations"
    now_iso = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    payload = {
        "patrolId": patrol_id,
        "pings": [
            {
                "lat": lat,
                "lng": lng,
                "accuracy": 4.5,
                "speed": 1.2,
                "recordedAt": now_iso
            }
        ]
    }
    return post_json(url, payload, token=token)


def main():
    args = parse_args()
    token = login(args.base_url, args.badge_no, args.password)
    coords = generate_coordinates(args.breach)

    for idx, (lat, lng) in enumerate(coords, start=1):
        send_ping(args.base_url, token, args.patrol_id, lat, lng)
        print(f"Ping {idx}/{len(coords)}: lat={lat}, lng={lng}")
        if idx < len(coords):
            time.sleep(args.interval)

    print("Officer simulation completed successfully.")


if __name__ == "__main__":
    main()

