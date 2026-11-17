# Audit Log Tracker

A Spring Boot REST API that records audit events for banking and healthcare-style actions, validates each record, and flags high-risk activity for review.

## Overview

Every sensitive action in a banking or hospital system should leave a trace: who did it, in what role, what they did, and what value was involved. This service stores those audit records, rejects malformed input, and automatically flags actions that need a second look, such as very large transactions or critical operations like deleting records or exporting data.

## Tech Stack

- Java 17+ (tested on Java 27)
- Spring Boot 4.1.2
- Spring Web MVC
- Spring Data JPA with Hibernate
- H2 in-memory database
- JUnit 5 and Mockito for testing

## Project Structure

```
src/main/java/com/example/demo/
├── DemoApplication.java          Application entry point
├── AuditLog.java                 JPA entity
├── AuditLogRequest.java          Request payload (record)
├── AuditLogRepository.java       Spring Data repository
├── AuditLogService.java          Validation and risk rules
├── AuditLogController.java       REST endpoints
├── AuditExceptionHandler.java    Returns 400 errors for invalid input
└── InvalidAuditLogException.java Thrown on invalid input

src/test/java/com/example/demo/
├── AuditLogServiceTest.java      Unit tests (Mockito)
└── DemoApplicationTests.java     Spring context test
```

## Endpoints

| Method | Path                 | Description                                              |
|--------|----------------------|----------------------------------------------------------|
| POST   | `/api/logs`          | Record an audit event. Returns `201 Created`.            |
| GET    | `/api/logs/flagged`  | List all flagged audit records, newest first.            |

### Request body for `POST /api/logs`

```json
{
  "userId": "U2",
  "role": "MANAGER",
  "actionType": "TRANSFER",
  "amountOrValue": 250000
}
```

| Field           | Type    | Rules                             |
|-----------------|---------|-----------------------------------|
| `userId`        | string  | Required, not blank               |
| `role`          | string  | Required, not blank               |
| `actionType`    | string  | Required, not blank               |
| `amountOrValue` | number  | Required, zero or positive        |

## Validation and Risk Rules

**Validation.** Requests are rejected with `400 Bad Request` if any field is missing or blank, or if `amountOrValue` is negative. The response body contains an error message, for example:

```json
{ "error": "userId is required" }
```

Invalid records are never saved.

**Flagging.** Valid records are saved, and the service marks a record as flagged when either rule applies:

1. **High-value transaction:** `amountOrValue` is greater than **100000**.
2. **Critical action:** `actionType` is one of:
   - `DELETE_RECORD`
   - `EXPORT_DATA`
   - `OVERRIDE_LIMIT`

Each flagged record stores a reason, such as `High-value transaction: 250000` or `Critical action: EXPORT_DATA`.

## How to Run

**Requirements:** JDK 17 or newer.

1. Clone the repository and open the project in IntelliJ IDEA.
2. Run `DemoApplication`, or from a terminal in the project folder run:

   ```
   ./mvnw spring-boot:run
   ```

   On Windows PowerShell use `.\mvnw spring-boot:run`.

3. The application starts on `http://localhost:8080`.

The H2 console is available at `http://localhost:8080/h2-console` for inspecting data. Use the JDBC URL shown in the startup log, username `SA`, and no password.

**Note:** The database is in memory, so all records are cleared when the application stops.

## How to Test

Run all tests with:

```
./mvnw test
```

The suite contains six unit tests for the service layer, covering normal logs, high-value flagging, critical-action flagging, blank user IDs, negative amounts, and retrieval of flagged records, plus one Spring context test.

## Postman Examples

Set the method and URL, then choose **Body → raw → JSON** for POST requests.

**1. Normal event (saved, not flagged)**

```
POST http://localhost:8080/api/logs
```
```json
{
  "userId": "U1",
  "role": "TELLER",
  "actionType": "VIEW_BALANCE",
  "amountOrValue": 500
}
```
Expected: `201 Created` with `"flagged": false`.

**2. High-value transaction (saved and flagged)**

```
POST http://localhost:8080/api/logs
```
```json
{
  "userId": "U2",
  "role": "MANAGER",
  "actionType": "TRANSFER",
  "amountOrValue": 250000
}
```
Expected: `201 Created` with `"flagged": true`.

**3. Critical action (saved and flagged)**

```
POST http://localhost:8080/api/logs
```
```json
{
  "userId": "U3",
  "role": "ADMIN",
  "actionType": "DELETE_RECORD",
  "amountOrValue": 0
}
```
Expected: `201 Created` with `"flagged": true`.

**4. Invalid input (rejected)**

```
POST http://localhost:8080/api/logs
```
```json
{
  "userId": " ",
  "role": "TELLER",
  "actionType": "VIEW",
  "amountOrValue": 10
}
```
Expected: `400 Bad Request` with `{ "error": "userId is required" }`.

**5. Retrieve flagged records**

```
GET http://localhost:8080/api/logs/flagged
```
Expected: `200 OK` with a JSON array containing the flagged records from examples 2 and 3.

## Possible Improvements

- Move the high-value threshold and the critical-action list into `application.yml` so they can change without a code change.
- Add pagination to the flagged-records endpoint.
- Record the authenticated user from a security context rather than trusting the request body.
- Replace H2 with PostgreSQL for persistent storage.

## Author

Built as a portfolio project to demonstrate REST API design, input validation, business rules, and unit testing with Spring Boot.
