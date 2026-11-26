#!/usr/bin/env python3
"""Send two simultaneous booking attempts for the same show/seat.
Usage: python scripts/concurrency-test.py <JWT> <SHOW_ID> [SEAT]
"""
import json, sys, urllib.request, urllib.error
from concurrent.futures import ThreadPoolExecutor

if len(sys.argv) < 3:
    raise SystemExit("Usage: python scripts/concurrency-test.py <JWT> <SHOW_ID> [SEAT]")
token, show_id = sys.argv[1], int(sys.argv[2])
seat = sys.argv[3] if len(sys.argv) > 3 else "S01"
url = "http://localhost:8080/api/bookings"
body = json.dumps({"showId": show_id, "seatNumber": seat}).encode()

def attempt(_):
    req = urllib.request.Request(url, data=body, method="POST", headers={
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json",
    })
    try:
        with urllib.request.urlopen(req, timeout=20) as response:
            return response.status
    except urllib.error.HTTPError as error:
        return error.code

with ThreadPoolExecutor(max_workers=2) as pool:
    statuses = list(pool.map(attempt, range(2)))
print("Concurrent booking HTTP statuses:", statuses)
if statuses.count(201) == 1 and statuses.count(409) == 1:
    print("PASS: exactly one request booked the seat; the other was rejected.")
else:
    print("FAIL: expected one 201 and one 409. Check service logs and database state.")
    raise SystemExit(1)
