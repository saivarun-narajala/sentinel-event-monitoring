#!/usr/bin/env python3
"""
Log analysis script for Sentinel event monitoring system.
Analyzes event patterns and generates reports.
"""

import json
import sys
from collections import defaultdict, Counter
from datetime import datetime
from typing import List, Dict

def parse_log_line(line: str) -> Dict:
    """Parse a JSON log line."""
    try:
        return json.loads(line)
    except json.JSONDecodeError:
        return None

def analyze_events(log_file: str):
    """Analyze events from log file."""
    events = []
    
    print(f"📖 Reading log file: {log_file}")
    
    try:
        with open(log_file, 'r') as f:
            for line in f:
                event = parse_log_line(line.strip())
                if event:
                    events.append(event)
    except FileNotFoundError:
        print(f"❌ File not found: {log_file}")
        return
    
    if not events:
        print("⚠️  No events found in log file")
        return
    
    print(f"\n✅ Loaded {len(events)} events\n")
    
    # Analysis 1: Event type distribution
    event_types = Counter(e.get('eventType', 'unknown') for e in events)
    print("📊 Event Type Distribution:")
    for event_type, count in event_types.most_common():
        print(f"  {event_type}: {count}")
    
    # Analysis 2: Source distribution
    sources = Counter(e.get('source', 'unknown') for e in events)
    print("\n📊 Source Distribution:")
    for source, count in sources.most_common():
        print(f"  {source}: {count}")
    
    # Analysis 3: Severity distribution
    severities = Counter(e.get('severity', 'INFO') for e in events)
    print("\n📊 Severity Distribution:")
    for severity, count in severities.most_common():
        print(f"  {severity}: {count}")
    
    # Analysis 4: Time-based patterns
    print("\n📊 Time-based Analysis:")
    timestamps = [datetime.fromisoformat(e['timestamp'].replace('Z', '+00:00')) 
                  for e in events if 'timestamp' in e]
    
    if timestamps:
        earliest = min(timestamps)
        latest = max(timestamps)
        duration = (latest - earliest).total_seconds()
        
        print(f"  Time range: {earliest} to {latest}")
        print(f"  Duration: {duration:.2f} seconds")
        print(f"  Average rate: {len(events) / max(duration, 1):.2f} events/second")
    
    # Analysis 5: Potential anomalies
    print("\n🚨 Potential Anomalies:")
    anomaly_threshold = len(events) * 0.1  # 10% of total
    
    for event_type, count in event_types.items():
        if count > anomaly_threshold:
            print(f"  High volume: {event_type} ({count} events)")

def main():
    if len(sys.argv) < 2:
        print("Usage: python analyze_logs.py <log_file.json>")
        print("\nExample log file format (one JSON object per line):")
        print('{"eventId": "1", "eventType": "user.login", "source": "web", "timestamp": "2024-06-20T10:00:00Z"}')
        sys.exit(1)
    
    log_file = sys.argv[1]
    
    print("=" * 60)
    print("Sentinel Log Analysis")
    print("=" * 60)
    
    analyze_events(log_file)
    
    print("\n✅ Analysis complete!")

if __name__ == "__main__":
    main()
