# Sentinel – Distributed Event Monitoring System

Sentinel is a distributed backend system designed to ingest, process, and analyze high volumes of event data in real time. It is built as a set of Spring Boot microservices connected through Kafka, with Redis used for fast anomaly detection and PostgreSQL for long-term storage.

The system demonstrates how event-driven architectures can be used to build scalable monitoring platforms for auditing, system observability, and security analysis.

---

## System Overview

Sentinel separates responsibilities into independent services so each component can scale and operate without blocking the others.

**Main responsibilities covered:**

- Accepting and validating incoming events  
- Streaming events reliably through Kafka  
- Persisting event history for auditing  
- Detecting abnormal patterns using sliding window logic  

This design allows the ingestion layer to stay responsive even when storage or detection services are under heavy load.

---

## Architecture

```
Client → Event Ingestion Service → Kafka → Event Processor → PostgreSQL
                                        ↘
                                         → Anomaly Detector → Redis
```

Events first enter through the ingestion service, are placed on Kafka topics, and are then consumed by downstream services for storage and analysis.

---

## Microservices

### Event Ingestion Service (Port 8081)

- Exposes REST endpoints for event submission  
- Validates incoming event structure  
- Publishes events to Kafka asynchronously  
- Returns immediately with an *Accepted* response  

### Event Processor Service (Port 8082)

- Consumes events from Kafka  
- Persists events into PostgreSQL asynchronously  
- Maintains indexed event history for auditing and queries  

### Anomaly Detector Service (Port 8083)

- Consumes events from Kafka  
- Uses Redis sorted sets to implement sliding window detection  
- Tracks event frequency and raises alerts when thresholds are exceeded  

---

## Tech Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| API Layer | Spring Boot | REST endpoints |
| Messaging | Apache Kafka | Event streaming |
| Caching | Redis | Sliding window anomaly detection |
| Database | PostgreSQL | Event storage |
| Containerization | Docker | Local orchestration |
| Testing Tools | Python | Validation and simulation scripts |

---

## Running the System

### Prerequisites

- Java 17+  
- Maven  
- Docker & Docker Compose  

---

### Start Infrastructure

```bash
docker-compose up -d kafka redis postgres
```

---

### Build Services

```bash
mvn clean install
```

---

### Start Services (in separate terminals)

```bash
# Event Ingestion
cd event-ingestion-service
mvn spring-boot:run
```

```bash
# Event Processor
cd event-processor-service
mvn spring-boot:run
```

```bash
# Anomaly Detector
cd anomaly-detector-service
mvn spring-boot:run
```

---

## Example API Request

```bash
curl -X POST http://localhost:8081/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "user.login",
    "source": "web-app",
    "severity": "INFO",
    "payload": {
      "userId": "12345",
      "ip": "192.168.1.1"
    }
  }'
```

The event is queued immediately and processed asynchronously.

---

## Anomaly Detection Logic

The anomaly detector uses a **Redis sorted set** as a sliding time window:

1. Each event is added with its timestamp as the score  
2. Events outside the time window are removed  
3. The number of events within the window is checked  
4. If the threshold is exceeded, an alert is generated  

This allows detection of unusual spikes, such as excessive login attempts from a single source.

---

## Performance Characteristics

- Ingestion latency: typically under 10ms  
- Processing throughput: thousands of events per second per instance  
- Detection delay: near real time  
- Storage: durable event history in PostgreSQL  

---

## Health Endpoints

```bash
curl http://localhost:8081/api/events/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/api/anomalies/health
```

---

## Purpose of This Project

Sentinel was built as a reference implementation of a **scalable, event-driven monitoring pipeline**. It focuses on asynchronous processing, system resilience, and real-time anomaly detection using commonly adopted open-source technologies.
