# /add-test — Generar Tests para una Clase

Genera tests unitarios e integración para una clase del proyecto.

## Uso
- `/add-test UserEntityServiceImpl`
- `/add-test TokensService`
- `/add-test AuthController`

## Paso 1: Localizar la clase

Busca el archivo de la clase indicada en `src/main/java/`.

Lee la clase completa para entender:
- Sus dependencias (campos `@Autowired` / constructor)
- Todos sus métodos públicos
- Las excepciones que puede lanzar
- Los casos de error y éxito en cada método

## Paso 2: Verificar tests existentes

Busca si ya hay tests en `src/test/java/` para esta clase. Si hay, léelos para no duplicar.

## Paso 3: Planificar los tests

Antes de escribir código, muestra el plan:

```
Tests a generar para [NombreClase]:

Método: [nombre]
  ✓ caso feliz: [descripción]
  ✗ caso error: [excepción] cuando [condición]
  ✗ caso error: [excepción] cuando [condición]

Método: [nombre]
  ✓ caso feliz: [descripción]
  ...

Tipo de test: [Unitario con Mockito / Integración con @SpringBootTest]
Archivo: src/test/java/galindo/raul/virtualclubs/[paquete]/[NombreClase]Test.java

¿Continúo con esta implementación? (sí/no/modificar)
```

## Paso 4: Generar los tests

### Estructura base para test unitario (servicios):
```java
@ExtendWith(MockitoExtension.class)
class NombreClaseTest {

    @Mock
    private DependenciaRepository dependenciaRepository; // una por dependencia

    @InjectMocks
    private NombreClase nombreClase;

    @Test
    @DisplayName("Descripción legible del caso")
    void metodoCasoFeliz() {
        // Arrange
        when(dependencia.metodo(any())).thenReturn(valorMock);

        // Act
        TipoRetorno resultado = nombreClase.metodo(param);

        // Assert
        assertThat(resultado).isNotNull();
        verify(dependencia, times(1)).metodo(any());
    }

    @Test
    @DisplayName("Lanza [Excepcion] cuando [condición]")
    void metodoLanzaExcepcion() {
        // Arrange
        when(dependencia.metodo(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ExcepcionEsperada.class, () -> nombreClase.metodo(param));
    }
}
```

### Estructura base para test de integración (controllers):
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev") // usa perfil dev
class NombreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /ruta - caso feliz")
    void endpointCasoFeliz() throws Exception {
        var request = new NombreRequest(...);

        mockMvc.perform(post("/v1/ruta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

### Convenciones:
- Un método de test por caso (no mezclar escenarios)
- `@DisplayName` en español, legible ("Lanza excepción cuando el email ya existe")
- Estructura **Arrange / Act / Assert** con comentarios
- Mockear solo las dependencias externas (repositorios, servicios externos)
- Nunca mockear la clase bajo prueba

## Paso 5: Crear el archivo

Crea el archivo en la ruta correcta dentro de `src/test/java/`.

## Paso 6: Recordatorio

Al terminar indica cuántos casos se cubrieron y cuáles quedan pendientes de implementar.
Sugiere ejecutar `./mvnw test` para verificar que pasan.
