#  Smart Campus — Sensor & Room Management API

A robust, scalable RESTful API built with JAX-RS (Jersey) and an embedded Grizzly HTTP server for managing university rooms and IoT sensors across campus. Developed as part of the 5COSC022W Client-Server Architectures coursework at the University of Westminster.

# API Design Overview

The API follows a clean hierarchical resource model reflecting the physical structure of the campus:

```
/api/v1
├── /rooms
│   ├── GET    /           → List all rooms
│   ├── POST   /           → Create a new room
│   ├── GET    /{roomId}   → Get room by ID
│   └── DELETE /{roomId}   → Delete a room (safety-checked)
│
└── /sensors
    ├── GET    /                       → List all sensors (with optional ?type= filter)
    ├── POST   /                       → Register a new sensor (validates roomId)
    ├── GET    /{sensorId}             → Get sensor by ID
    └── /{sensorId}/readings
        ├── GET  /                     → Get reading history for a sensor
        └── POST /                     → Append a new reading (updates sensor's currentValue)
```

# Technology Stack
Component - Technology 
Language        - Java 11+                      
Framework       - JAX-RS (Jersey 2.x)           
HTTP Server     - Grizzly (embedded)           
JSON Binding    - Jackson (via JacksonFeature)  
Build Tool      - Apache Maven                  
Data Storage    - ConcurrentHashMap (in-memory) 

##  Getting Started

### Prerequisites

- **JDK 11** or higher ([Download](https://adoptium.net/))
- **Apache Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))

Verify installations:
bash
java -version
mvn -version

### Step 1: Clone the Repository

bash
git clone https://github.com/Gopithan/Smart-Campus-API.git
cd Smart-Campus-API


### Step 2: Build the Project

bash
mvn clean package

This compiles the source, runs any tests, and packages a JAR file in the `/target` directory.

### Step 3: Launch the Server
bash
mvn exec:java

You should see output confirming the server started:
Smart Campus API started at: http://localhost:8080/api/v1
Press ENTER to stop the server...

The API is now live at: `http://localhost:8080/api/v1`


# Sample curl Commands

# 1. Discover the API
bash
curl -X GET http://localhost:8080/api/v1/
Expected: `200 OK` — Returns API version, contact info, and resource links.

# 2. Create a New Room
bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50}'
Expected: `201 Created` — Returns the created room object.

# 3. Register a New Sensor (linked to an existing room)
bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-001", "type": "Temperature", "status": "ACTIVE", "currentValue": 22.5, "roomId": "LIB-301"}'

Expected: `201 Created` — Returns the registered sensor object.

# 4. Filter Sensors by Type
bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"

Expected: `200 OK` — Returns only sensors with type `Temperature`.


# 5. Add a Sensor Reading (updates currentValue)
bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 26.8}'

Expected: `201 Created` — Returns the new reading; sensor's 

# 6. Get Reading History for a Sensor
bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings

Expected: `200 OK` — Returns the full historical log of readings for `TEMP-001`.


# 7. Attempt to Delete a Room with Sensors (Error Demo)
bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301

Expected: `409 Conflict` — Room cannot be deleted while sensors are assigned.

# 8. Attempt to Register Sensor with Non-Existent Room (Error Demo)
bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "CO2-999", "type": "CO2", "status": "ACTIVE", "currentValue": 0.0, "roomId": "FAKE-ROOM"}'
Expected: `422 Unprocessable Entity` — Room ID does not exist.

# Conceptual Report

# Part 1: Service Architecture & Setup

Q: Explain the default lifecycle of a JAX-RS Resource class and how it impacts in-memory data management.

By default, JAX-RS creates a new instance of a resource class for every incoming HTTP request (per-request lifecycle). This means no state should be stored in instance variables of the resource class, as that data would be lost after the request completes.

To manage shared in-memory data safely across all requests, I implemented a dedicated `DataStore` class using static fields backed by `ConcurrentHashMap`. Since `ConcurrentHashMap` is thread-safe by design, concurrent requests can read and write data without causing race conditions or data corruption. This architectural choice — separating the storage layer from the resource classes — ensures data persists for the lifetime of the server process and is consistently accessible regardless of how many resource instances are created.


Q: Why is HATEOAS considered a hallmark of advanced RESTful design, and how does it benefit client developers?

HATEOAS (Hypermedia as the Engine of Application State) means that API responses include links that guide the client to related or next-step resources — rather than forcing clients to construct URLs from documentation. This provides two major benefits:

1. Decoupling: Clients do not hardcode URLs. If the server changes a path, clients following links continue to work without modification.
2. Discoverability: A developer starting at the root endpoint (`GET /api/v1`) immediately learns what resources exist and where to find them — the API becomes self-documenting at runtime. This reduces the need to maintain separate, potentially outdated API documentation.

# Part 2: Room Management

Q: When returning a list of rooms, what are the implications of returning only IDs versus returning full room objects?

The optimal choice depends on the use case. For a dashboard listing many rooms, returning full objects avoids round-trips. For a high-frequency internal system only needing IDs, the lightweight approach is better. My implementation returns full objects for usability, which suits facilities management clients.


Q: Is the DELETE operation idempotent in your implementation? Justify with multiple DELETE scenarios.

Yes, DELETE is idempotent in this implementation. Idempotency means that making the same request multiple times produces the same final server state.

First DELETE on an existing, empty room: The room is removed; server returns `204 No Content`.
Second DELETE on the same (now non-existent) room: The room is already gone; the server returns `204 No Content` again — the state of the server (room absent) is identical.

The final state is always the same: the room does not exist. This satisfies the HTTP specification's definition of idempotency. My implementation intentionally returns `204` (not `404`) on repeat deletions to make this idempotent behaviour explicit and predictable for clients.


# Part 3: Sensor Operations & Linking

Q: What happens if a client sends data in a format other than JSON to a method annotated with `@Consumes(MediaType.APPLICATION_JSON)`?

If a client sends a request with a `Content-Type` of `text/plain` or `application/xml` to an endpoint annotated with `@Consumes(MediaType.APPLICATION_JSON)`, the JAX-RS runtime intercepts the request before the resource method is ever invoked and automatically returns an HTTP 415 Unsupported Media Type response. This is handled entirely by the framework — no custom code is needed. It ensures the application only processes data in formats it is designed to deserialise, protecting against malformed or unexpected input structures.


Q: Contrast `@QueryParam` filtering with path-based filtering (e.g., `/sensors/type/CO2`). Why is query parameter filtering superior?

Optional filtering - Naturally optional — omit to get all - Requires a separate route with no type filter 
Combining filters - Easy: `?type=CO2&status=ACTIVE` - Requires complex path nesting 
REST semantics - Correct — filters a collection - Implies `CO2` is a sub-resource identifier 
URL cleanliness - Clean, conventional - Adds noise; looks like a resource, not a filter 

Query parameters are the REST-standard mechanism for filtering, searching, and sorting collections. Path segments are reserved for identifying a specific resource (e.g., `/sensors/TEMP-001`). Using a path segment for filtering violates this convention and makes the API semantically misleading.

### Part 4: Deep Nesting with Sub-Resources

Q: Discuss the architectural benefits of the Sub-Resource Locator pattern.

The Sub-Resource Locator pattern delegates responsibility for nested paths to a separate, dedicated class. In this project, `SensorResource` locates and returns a `SensorReadingResource` instance for any request under `/{sensorId}/readings`, rather than handling all reading logic inline.

Benefits:

1. Single Responsibility: Each class has one clear purpose. `SensorResource` manages sensors; `SensorReadingResource` manages readings. No single class becomes a "God object."
2. Maintainability:Adding new endpoints under `/readings` (e.g., `DELETE /{readingId}`) only requires changes to `SensorReadingResource`, not the parent class.
3. Testability: Each resource class can be unit tested in isolation.
4. Scalability: As the API grows (e.g., adding `/sensors/{id}/alerts`, `/sensors/{id}/config`), new sub-resource classes can be added cleanly without touching existing code.

In contrast, putting every nested path in one massive controller quickly leads to a class with hundreds of methods, poor readability, and high risk of regression bugs.


### Part 5: Advanced Error Handling

Q: Why is HTTP 422 often more semantically accurate than 404 when a JSON payload references a missing resource?

HTTP 404 Not Found means the requested resource at the URL was not found. It answers the question: "Did this endpoint exist?"
HTTP 422 Unprocessable Entity means the request was received and understood (the URL was valid, the JSON was well-formed), but the content failed semantic validation — in this case, the `roomId` field referenced a room that does not exist.

Using 422 communicates precisely: "Your request arrived, your JSON was parseable, but the data inside it is logically invalid." A 404 would mislead the client into thinking the `/sensors` endpoint itself was missing. The 422 guides the developer to inspect the *payload content*, not the URL — which is the real problem.

Q: From a cybersecurity standpoint, what risks arise from exposing internal Java stack traces to external API consumers?

Exposing raw stack traces is a serious security vulnerability for several reasons:

1. Technology Fingerprinting: Stack traces reveal exact library names, versions, and package structures (e.g., `org.glassfish.jersey`, `com.mycompany.smart.campus`). Attackers can cross-reference these against known CVE databases to find exploitable vulnerabilities in specific versions.

2. Internal Path Disclosure: Package names and class paths reveal the internal architecture and naming conventions, which can be used to craft targeted attacks.

3. Logic Disclosure: The sequence of method calls in a trace reveals business logic flow, helping attackers identify weak points or exploit edge cases.

4. Information Gathering for Social Engineering: Internal server details (hostnames, OS info) sometimes appear in traces and can aid broader attack planning.

The `GlobalExceptionMapper` in this project prevents all of this by catching any `Throwable`, logging it server-side only, and returning a clean, generic `500 Internal Server Error` JSON body — giving the attacker nothing useful.

# Project Structure
```
smart-campus-api/
├── src/main/java/com/mycompany/smart/campus/api/
│   ├── Main.java                          # Grizzly server bootstrap
│   ├── DiscoveryResource.java             # GET /api/v1 — Discovery endpoint
│   ├── config/
│   │   └── SmartCampusApp.java            # JAX-RS Application config (@ApplicationPath)
│   ├── db/
│   │   └── DataStore.java                 # Thread-safe in-memory storage (ConcurrentHashMap)
│   ├── models/
│   │   ├── Room.java                      # Room POJO
│   │   ├── Sensor.java                    # Sensor POJO
│   │   └── SensorReading.java             # SensorReading POJO
│   ├── resources/
│   │   ├── RoomResource.java              # /api/v1/rooms
│   │   ├── SensorResource.java            # /api/v1/sensors
│   │   └── SensorReadingResource.java     # /api/v1/sensors/{id}/readings (sub-resource)
│   ├── exceptions/
│   │   ├── RoomNotEmptyException.java             # Thrown on DELETE room with sensors
│   │   ├── RoomNotEmptyExceptionMapper.java       # → 409 Conflict
│   │   ├── LinkedResourceNotFoundException.java   # Thrown on sensor with bad roomId
│   │   ├── LinkedResourceNotFoundExceptionMapper.java  # → 422 Unprocessable Entity
│   │   ├── SensorUnavailableException.java        # Thrown on reading to MAINTENANCE sensor
│   │   ├── SensorUnavailableExceptionMapper.java  # → 403 Forbidden
│   │   └── GlobalExceptionMapper.java             # Catch-all → 500 Internal Server Error
│   └── filters/
│       └── LoggingFilter.java             # Request/response logging
└── pom.xml
```

# Author
Krishnakumar Shangopithasarma
Student ID: w2152963
Module: 5COSC022W — Client-Server Architectures
