# ChatApp - Spring Boot REST Chat Application

This is a simple backend chat application built with Java and Spring Boot. It supports two types of users — `user` and `admin` — and provides a public chat room via REST API. The application uses an H2 database and integrates with Liquibase for database schema management.

---

## 🚀 Features

- ✅ Public chatroom via REST API
- ✅ Admin/User role support
- ✅ JWT-based authentication
- ✅ Spring Security
- ✅ H2 database
- ✅ Liquibase for schema migrations

---

## 🧰 Tech Stack

- Java 21
- Spring Boot 3.5.4
- Spring Security
- Spring Data JPA
- H2 Database (File-based)
- Liquibase
- Spock Framework
- Gradle

---

## Authentication

This application uses JWT-based authentication. There are two roles:

- **USER** – can read/send messages in the public chat.
- **ADMIN** – can register and delete other users and get statistics about users.
  
After logging in via `/auth/login`, the user receives a JWT token, which must be included in the `Authorization` header for secured endpoints.

## Default Admin User

For development/testing purposes, a default admin user is created on application startup **if none exists**:

- **Username:** `admin`
- **Password:** `admin123`
> ⚠️ For development/testing only. Do not use these credentials in production.
---

## Swagger UI

Once the application is running, Swagger UI is available at:
http://localhost:8080/swagger-ui/index.html

---

## Testing

This project uses [Spock Framework](https://spockframework.org/) for testing.

- Based on Groovy
- Supports expressive and readable `Given-When-Then` testing style
- Integrated with Spring Boot Test

