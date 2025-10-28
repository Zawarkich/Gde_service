Gde search with UI, connected to database, almost done basic stages. 
Login|Authorization done
(need to do security)

design almost done(not needed btw)

Need to add a lot people to database to test it properly

I want to play silksong instead...

Structure:
- `gde.gde_search` - application entry point
- `gde.gde_search.controller.GdeController` - REST endpoint `/api/search?term=...`
- `gde.gde_search.service.GdeService` - in-memory service
- `gde.gde_search.entity.GdeArticle` - simple DTO

To run:
1. Use the Maven wrapper: `./mvnw.cmd spring-boot:run`
2. Call `http://localhost:8080/api/search?term=example`
