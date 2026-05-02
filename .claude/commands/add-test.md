# /add-test — Generate Tests for a Class

Generates unit and integration tests for a project class.

## Usage
- `/add-test UserEntityServiceImpl`
- `/add-test TokensService`
- `/add-test AuthController`

## Step 1: Locate the class

Find the file in `src/main/java/` and read it completely to understand:
- Its dependencies (constructor / `@Autowired` fields)
- All public methods
- The exceptions it can throw
- The error and success cases for each method

## Step 2: Check existing tests

Look in `src/test/java/` for existing tests for this class. If found, read them to avoid duplication.

## Step 3: Plan the tests

Before writing code, show the plan:

```
Tests to generate for [ClassName]:

Method: [name]
  ✓ happy path: [description]
  ✗ error case: [exception] when [condition]
  ✗ error case: [exception] when [condition]

Method: [name]
  ✓ happy path: [description]
  ...

Test type: [Unit with Mockito / Integration with @SpringBootTest]
File: src/test/java/galindo/raul/virtualclubs/[package]/[ClassName]Test.java

Proceed with this implementation? (yes/no/modify)
```

## Step 4: Generate the tests

### Base structure for unit tests (services):
```java
@ExtendWith(MockitoExtension.class)
class ClassNameTest {

    @Mock
    private DependencyRepository dependencyRepository; // one per dependency

    @InjectMocks
    private ClassName className;

    @Test
    @DisplayName("Readable description of the case")
    void methodHappyPath() {
        // Arrange
        when(dependency.method(any())).thenReturn(mockValue);

        // Act
        ReturnType result = className.method(param);

        // Assert
        assertThat(result).isNotNull();
        verify(dependency, times(1)).method(any());
    }

    @Test
    @DisplayName("Throws [Exception] when [condition]")
    void methodThrowsException() {
        // Arrange
        when(dependency.method(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ExpectedException.class, () -> className.method(param));
    }
}
```

### Base structure for integration tests (controllers):
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
@Transactional
class ControllerNameTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /path - happy path")
    void endpointHappyPath() throws Exception {
        var request = new NameRequest(...);

        mockMvc.perform(post("/v1/path")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").exists());
    }
}
```

### Conventions:
- One test method per case (do not mix scenarios)
- `@DisplayName` in English, human-readable ("Throws exception when email already exists")
- **Arrange / Act / Assert** structure with comments
- Mock only external dependencies (repositories, external services)
- Never mock the class under test

## Step 5: Create the file

Create the file at the correct path inside `src/test/java/`.

## Step 6: Reminder

When done, report how many cases were covered and which ones remain.
Suggest running `./mvnw test` to verify they pass.
