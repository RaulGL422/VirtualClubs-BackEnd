# /add-migration — Crear Migración Flyway

Crea una nueva migración de base de datos con el número de versión correcto y SQL seguro.

## Uso
- `/add-migration añadir columna avatar_url a users`
- `/add-migration crear tabla clubs`
- `/add-migration añadir índice en refresh_tokens.device_id`

---

## Paso 1: Determinar el siguiente número de versión

```bash
ls src/main/resources/db/migration/ | sort
```

Extrae el número más alto (`V{N}__...`) e incrementa en 1. Si el directorio está vacío, empieza en V1.

---

## Paso 2: Proponer nombre de archivo

Nombre en snake_case, sin tildes, descriptivo:
```
V[N]__[descripcion_en_snake_case].sql
```

Ejemplos: `V11__add_avatar_url_to_users.sql`, `V12__create_clubs_table.sql`

Muestra el nombre propuesto y confirma con el usuario antes de continuar.

---

## Paso 3: Generar el SQL

Genera SQL idempotente según el tipo de cambio:

**Añadir columna nullable:**
```sql
ALTER TABLE [tabla] ADD COLUMN IF NOT EXISTS [col] [tipo] NULL;
```

**Añadir columna NOT NULL con default:**
```sql
ALTER TABLE [tabla] ADD COLUMN IF NOT EXISTS [col] [tipo] NOT NULL DEFAULT [valor];
-- Luego si necesitas quitar el default:
-- ALTER TABLE [tabla] ALTER COLUMN [col] DROP DEFAULT;
```

**Crear tabla:**
```sql
CREATE TABLE IF NOT EXISTS [tabla] (
    id          BIGSERIAL PRIMARY KEY,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
```

**Añadir índice:**
```sql
CREATE INDEX IF NOT EXISTS idx_[tabla]_[col] ON [tabla] ([col]);
```

**Añadir unique constraint:**
```sql
ALTER TABLE [tabla] ADD CONSTRAINT uq_[tabla]_[col] UNIQUE ([col]);
```

**Añadir foreign key:**
```sql
ALTER TABLE [tabla]
    ADD CONSTRAINT fk_[tabla]_[ref]
    FOREIGN KEY ([col]) REFERENCES [ref_tabla]([ref_col])
    ON DELETE CASCADE;
```

**Reglas obligatorias:**
- Usar `IF NOT EXISTS` / `IF EXISTS` donde aplique
- Columnas NOT NULL nuevas deben tener `DEFAULT` o ser `NULLABLE`
- Nunca modificar un script ya ejecutado en producción — crear uno nuevo
- Nunca usar `DROP TABLE` sin confirmar primero que no hay datos críticos

---

## Paso 4: Mostrar y pedir confirmación

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  src/main/resources/db/migration/V[N]__[descripcion].sql
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[SQL completo]

¿Crear este archivo? (sí/editar/cancelar)
```

Si el usuario dice `editar [qué cambiar]`, ajusta el SQL y muestra de nuevo.

---

## Paso 5: Crear el archivo

Escribe `src/main/resources/db/migration/V[N]__[descripcion_en_snake_case].sql`.

---

## Paso 6: Verificar compilación

```bash
./mvnw compile -q
```

Con `ddl-auto=validate`, Hibernate verifica que el schema coincide con las entidades. Si falla, muestra el error y propone la corrección.

---

## Paso 7: Confirmar

```
✅ Migración creada:
  V[N]__[descripcion].sql

La migración se ejecutará automáticamente en el próximo arranque de la app.
Para resetear en desarrollo (⚠️ solo con variables _DEBUG): ./mvnw flyway:clean
```
