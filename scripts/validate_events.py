#!/usr/bin/env python3
"""
Event validation script for testing Sentinel event pipelines.
Validates event structure and sends test events to the ingestion API.
"""

import json
import requests
import time
from datetime import datetime
from typing import Dict, Any

API_URL = "http://localhost:8081/api/events"

def validate_event(event: Dict[str, Any]) -> bool:
    """Validate event structure."""
    required_fields = ["eventId", "eventType", "source", "timestamp"]
    
    for field in required_fields:
        if field not in event:
            print(f"❌ Missing required field: {field}")
            return False
    
    print(f"✅ Event {event['eventId']} is valid")
    return True

def send_event(event: Dict[str, Any]) -> bool:
    """Send event to ingestion API."""
    try:
        response = requests.post(API_URL, json=event, timeout=5)
        
        if response.status_code == 202:
            print(f"✅ Event {event['eventId']} accepted")
            return True
        else:
            print(f"❌ Event {event['eventId']} rejected: {response.status_code}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Failed to send event: {e}")
        return False

def generate_test_event(event_type: str, source: str, severity: str = "INFO") -> Dict[str, Any]:
    """Generate a test event."""
    return {
        "eventId": f"test-{int(time.time() * 1000)}",
        "eventType": event_type,
        "source": source,
        "timestamp": datetime.utcnow().isoformat() + "Z",
        "severity": severity,
        "payload": {
            "message": "Test event",
            "value": 42
        },
        "metadata": {
            "environment": "test",
            "version": "1.0"
        }
    }

def simulate_traffic_spike(event_type: str, source: str, count: int = 150):
    """Simulate a traffic spike to trigger anomaly detection."""
    print(f"\n🚀 Simulating traffic spike: {count} events")
    
    for i in range(count):
        event = generate_test_event(event_type, source)
        send_event(event)
        
        if i % 10 == 0:
            print(f"Sent {i}/{count} events...")
    
    print(f"✅ Sent {count} events - anomaly should be detected!")

def main():
    print("=" * 60)
    print("Sentinel Event Validation Script")
    print("=" * 60)
    
    # Test 1: Validate and send a single event
    print("\n📋 Test 1: Single event validation")
    test_event = generate_test_event("user.login", "web-app", "INFO")
    
    if validate_event(test_event):
        send_event(test_event)
    
    # Test 2: Simulate traffic spike for anomaly detection
    print("\n📋 Test 2: Anomaly detection test")
    choice = input("Simulate traffic spike? (y/n): ")
    
    if choice.lower() == 'y':
        simulate_traffic_spike("api.request", "payment-service", 150)
        
        print("\n⏳ Waiting 5 seconds...")
        time.sleep(5)
        
        # Check anomaly detector
        try:
            response = requests.get("http://localhost:8083/api/anomalies/stats",
                                   params={"eventType": "api.request", "source": "payment-service"})
            if response.status_code == 200:
                stats = response.json()
                print(f"\n📊 Anomaly Stats: {json.dumps(stats, indent=2)}")
        except Exception as e:
            print(f"Could not fetch stats: {e}")
    
    print("\n✅ Validation complete!")

if __name__ == "__main__":
    main()
