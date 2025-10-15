Gde search skeleton project. This project provides a minimal Spring Boot skeleton with a simple
in-memory service that returns "hello world" for searches by default.

Structure:
- `gde.gde_search` - application entry point
- `gde.gde_search.controller.GdeController` - REST endpoint `/api/search?term=...`
- `gde.gde_search.service.GdeService` - in-memory service
- `gde.gde_search.entity.GdeArticle` - simple DTO

To run:
1. Use the Maven wrapper: `./mvnw.cmd spring-boot:run`
2. Call `http://localhost:8080/api/search?term=example`