# Testing Framework for "Расход" System

This directory contains the complete testing framework for the "Расход" (Attendance Tracker) system.

## Directory Structure

```
src/
└── test/
    ├── java/
    │   └── gde/
    │       └── gde_search/
    │           ├── unit/                 # Unit tests for services
    │           ├── integration/          # Integration tests for controllers
    │           ├── repository/           # Repository layer tests
    │           └── e2e/                  # End-to-end tests
    └── resources/
        └── application-test.properties   # Test configuration

Documents/
└── Tests/
    ├── TestPlan.md                      # Detailed test plan
    └── TestResults.md                   # Test results template
```

## Test Categories

### Unit Tests
- Test individual service methods in isolation
- Use Mockito for mocking dependencies
- Located in `src/test/java/gde/gde_search/unit/`

### Integration Tests
- Test controller endpoints with mocked services
- Use @WebMvcTest for isolated controller testing
- Located in `src/test/java/gde/gde_search/integration/`

### Repository Tests
- Test data access layer with real database interactions
- Use @DataJpaTest with H2 in-memory database
- Located in `src/test/java/gde/gde_search/repository/`

### End-to-End Tests
- Test complete application flow
- Use @SpringBootTest with real application context
- Located in `src/test/java/gde/gde_search/e2e/`

## Running Tests

### All Tests
```bash
mvn test
```

### Unit Tests Only
```bash
mvn test -Dtest="**/unit/**"
```

### Integration Tests Only
```bash
mvn test -Dtest="**/integration/**"
```

### Generate Test Coverage Report
```bash
mvn jacoco:report
```

## Test Coverage Areas

### Service Layer
- GdeService functionality
- TelegramBotService functionality
- Authentication and authorization
- Location updates
- Vzvod management

### Controller Layer
- Web interface endpoints
- Session management
- Error handling
- Authentication flow

### Data Layer
- Repository methods
- Database queries
- Entity relationships
- Data validation

### Integration Points
- Web interface flow
- Telegram bot interactions
- Database operations
- Session handling

## Test Data ([[TestPlan]])

Test data is created during the test execution with:
- Sample group members
- Login credentials
- Telegram user mappings
- Different presence statuses
- Multiple vzvod groups

## Expected Test Results

- All tests should pass
- Code coverage should be above 80%
- Performance tests should meet time requirements
- Security tests should validate proper authentication