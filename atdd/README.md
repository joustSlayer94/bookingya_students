# Bookingya - ATDD con Playwright + TypeScript

Pruebas de aceptación (ATDD) para la API REST de Bookingya usando Playwright.

## Estructura

```
bookingya-atdd/
├── tests/
│   └── reservation.atdd.spec.ts   # Criterios de aceptación de reservas
├── helpers/
│   └── BookingyaApiHelper.ts      # Helper para llamadas a la API
├── playwright.config.ts           # Configuración de Playwright
├── package.json
└── tsconfig.json
```

## Criterios de Aceptación cubiertos

| ID     | Criterio                                              |
|--------|-------------------------------------------------------|
| AC-01  | Crear una reserva válida exitosamente                 |
| AC-02  | Rechazar reserva con fechas inválidas (checkIn > checkOut) |
| AC-03  | Rechazar reserva en habitación no disponible          |
| AC-04  | Rechazar reserva que excede la capacidad máxima       |
| AC-05  | Verificar disponibilidad de habitación                |
| AC-06  | Consultar reserva por ID existente                    |
| AC-07  | Error al buscar reserva con ID inexistente            |

## Cómo ejecutar

### 1. Instalar dependencias
```bash
npm install
npx playwright install
```

### 2. Levantar el backend primero
```bash
# En el proyecto bookingya_students:
mvn spring-boot:run
```

### 3. Ejecutar los tests
```bash
# Todos los tests
npm test

# Con reporte HTML
npm run test:report

# Ver reporte
npm run report
```

### 4. Cambiar la URL base (si el backend corre en otro puerto)
```bash
BASE_URL=http://localhost:9090 npm test
```

## Diferencia con BDD (Cucumber)

| Aspecto       | BDD (Cucumber)                        | ATDD (Playwright)                     |
|---------------|---------------------------------------|---------------------------------------|
| Lenguaje      | Gherkin (Given/When/Then)             | TypeScript con comentarios GIVEN/WHEN/THEN |
| Enfoque       | Comportamiento del sistema            | Criterios de aceptación del cliente   |
| Capa          | Servicio (unit/integration)           | API REST (end-to-end)                 |
| Audiencia     | Desarrolladores + QA                  | Cliente + Product Owner + QA          |
