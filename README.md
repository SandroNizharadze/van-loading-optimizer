# Van Loading Optimizer

## Tech Stack

- Java 17, Spring Boot 3.3.5
- Spring Data JPA, PostgreSQL 16
- Flyway for database migrations
- Lombok, Jakarta Validation
- JUnit 5, Mockito, MockMvc for testing
- Maven, Docker Compose

## Quick Start

### 1. Start the database

```bash
   docker compose up -d
```

This starts a PostgreSQL 16 container on port **5432** with database `vanopt`.

### 2. Run the application with maven or Intellij

```bash
   ./mvnw spring-boot:run
```

Flyway will automatically create the required tables on first startup.

### 3. Build executable JAR

```bash
   ./mvnw clean package -DskipTests
java -jar target/van-loading-optimizer-0.0.1-SNAPSHOT.jar
```

### 4. Run tests

```bash
   ./mvnw test
```

Tests use an embedded H2 database — no Docker required.

## API Endpoints

| Method | Path                              | Description                        |
|--------|-----------------------------------|------------------------------------|
| POST   | `/api/v1/optimizations`           | Submit a new optimization request  |
| GET    | `/api/v1/optimizations/{requestId}` | Retrieve a past result by ID     |
| GET    | `/api/v1/optimizations`           | List all past optimization results |


## Example cURL Requests

### POST — Optimize shipments

```bash
   curl -s -X POST http://localhost:8080/api/v1/optimizations \
  -H "Content-Type: application/json" \
  -d '{
    "maxVolume": 15,
    "availableShipments": [
      { "name": "Parcel A", "volume": 5, "revenue": 120 },
      { "name": "Parcel B", "volume": 10, "revenue": 200 },
      { "name": "Parcel C", "volume": 3, "revenue": 80 },
      { "name": "Parcel D", "volume": 8, "revenue": 160 }
    ]
  }' | jq
```

**Response (200 OK):**

```json
{
  "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "selectedShipments": [
    { "name": "Parcel A", "volume": 5.0, "revenue": 120.0 },
    { "name": "Parcel B", "volume": 10.0, "revenue": 200.0 }
  ],
  "totalVolume": 15.0,
  "totalRevenue": 320.0,
  "createdAt": "2025-06-01T10:00:00Z"
}
```

### GET — Retrieve by ID

```bash
   curl -s http://localhost:8080/api/v1/optimizations/a1b2c3d4-e5f6-7890-abcd-ef1234567890 | jq
```

**Response (200 OK):** Same structure as POST response.

**Response (404 Not Found):**

```json
{
  "error": "Optimization result not found: a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### GET — List all results

```bash
   curl -s http://localhost:8080/api/v1/optimizations | jq
```

**Response (200 OK):**

```json
[
  {
    "requestId": "...",
    "selectedShipments": [...],
    "totalVolume": 15.0,
    "totalRevenue": 320.0,
    "createdAt": "2025-06-01T10:00:00Z"
  }
]
```

### POST — Invalid input

```bash
   curl -s -X POST http://localhost:8080/api/v1/optimizations \
  -H "Content-Type: application/json" \
  -d '{ "availableShipments": [] }' | jq
```

**Response (400 Bad Request):**

```json
{
  "error": "Invalid input",
  "details": [
    "maxVolume: maxVolume is required",
    "availableShipments: availableShipments must not be empty"
  ]
}
```

## Database Schema

### Tables

**optimization_result** — Stores each optimization run.

| Column     | Type             | Description                       |
|------------|------------------|-----------------------------------|
| id         | UUID (PK)        | Unique request identifier         |
| max_volume | DOUBLE PRECISION | Van capacity for this request     |
| total_volume | DOUBLE PRECISION | Sum of selected shipment volumes  |
| total_revenue| DOUBLE PRECISION | Sum of selected shipment revenues |
| created_at | TIMESTAMP WITH TIME ZONE | When the request was processed    |

**selected_shipment** — Stores each shipment selected in an optimization run.

| Column           | Type          | Description                            |
|------------------|---------------|----------------------------------------|
| id               | BIGSERIAL (PK) | Auto-increment surrogate key           |
| optimization_result_id | UUID (FK)     | References optimization_result.id      |
| name             | VARCHAR(255)  | Shipment label                         |
| volume           | DOUBLE PRECISION | Shipment volume in dm³                 |
| revenue          | DOUBLE PRECISION | Shipment revenue                       |

### Indexes

| Index                                | Column(s)                | Rationale                                           |
|--------------------------------------|--------------------------|-----------------------------------------------------|
| PK on `optimization_result.id`       | `id`                     | Fast lookup by request ID (GET by ID endpoint)      |
| `idx_selected_shipment_result_id`    | `optimization_result_id` | Fast JOIN when loading shipments for a result       |
| `idx_optimization_result_created_at` | `created_at`             | Supports ordering by date in the list endpoint      |

The FK index on selected_shipment.optimization_result_id is critical because JPA eagerly or lazily loads the @OneToMany collection via a JOIN — without this index, every GET request would trigger a full table scan on selected_shipment.

## Algorithm

The optimization uses a **0/1 Knapsack** algorithm solved with **bottom-up dynamic programming**.

- **Time complexity:** O(n × capacity)
- **Space complexity:** O(n × capacity)

Volumes are scaled to integers (×100) before running the DP to avoid floating-point precision issues. After solving, backtracking through the DP table identifies which shipments were selected.
