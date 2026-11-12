# CLAUDE.md

## Qué es este proyecto
Plataforma de reservas (pistas/salas) para portfolio. Objetivo: practicar un stack profesional
Java/Spring Boot + React y poder defender cada decisión en entrevistas. Es un proyecto de
aprendizaje: importa más entender el código que terminarlo rápido.

## Objetivo concreto: la oferta de trabajo
El proyecto está diseñado para cubrir los requisitos de esta oferta (Senior Full Stack Engineer
– Java / React, 100 % remoto, sin importar la ubicación). Mi experiencia profesional es de unos
2,5 años; la oferta pide más de 5, así que el proyecto debe demostrar nivel y criterio.

### Requisitos de la oferta
- Más de 5 años de experiencia en desarrollo de software, con autonomía en soluciones Full Stack
  y responsabilidad de funcionalidades end-to-end.
- Experiencia sólida en APIs y microservicios con Java / Spring Boot: diseño, implementación,
  integración y evolución de APIs REST.
- Experiencia práctica con React (frontend): integración con APIs y evolución de aplicaciones
  existentes.
- Valorable: desarrollo o modificación de aplicaciones móviles.
- Bases de datos relacionales como PostgreSQL y JPA / Hibernate.
- AWS y arquitecturas cloud.
- CI/CD sólido y demostrable: automatización de pipelines, quality gates, testing y deployment.
- Testing más allá del unit testing: integración, API, funcional y end-to-end. Valorable:
  Playwright, Cypress, Selenium, Postman/Newman.
- Git, Maven, JUnit y Mockito.
- Conocimientos de arquitecturas hexagonales, desacopladas y event-driven.
- Trabajo end-to-end sobre una funcionalidad: React → API → microservicios → persistencia →
  integración → testing → deployment.
- **Se valora especialmente** la IA aplicada al ciclo de desarrollo: AI-assisted coding,
  generación y revisión de código, generación de tests, documentación, análisis/troubleshooting
  o mejora de productividad.
- Inglés mínimo B2, con fluidez en entorno profesional internacional (conversación,
  documentación técnica y reuniones). Castellano nativo.
- Stack principal: Java, Spring Boot, React, REST APIs, PostgreSQL, JPA/Hibernate, AWS, Git,
  Maven, CI/CD, automated testing.

### Cómo lo cubre el proyecto
| Requisito | Dónde lo cubre |
|---|---|
| Java / Spring Boot, API REST | Backend de reservas con contratos documentados (OpenAPI) |
| React + integración con API | Frontend con calendario y formularios (TanStack Query, TypeScript) |
| PostgreSQL + JPA/Hibernate | Modelo de reservas, migraciones Flyway, control de solapes |
| Hexagonal / desacoplada | Dominio aislado con puertos y adaptadores |
| Event-driven + AWS | Eventos con SQS/SNS (LocalStack en local), despliegue en AWS |
| Microservicios | Monolito modular primero; extraer un servicio de notificaciones en la fase 4 |
| CI/CD con quality gates | GitHub Actions: build, tests, cobertura (JaCoCo), SonarCloud |
| Testing más allá de unit | Testcontainers (integración), Newman (API), Playwright (E2E) |
| Móvil (valorable) | Frontend como PWA; React Native como extra |
| IA en el desarrollo | Documentar en el README cómo se usa Claude Code (tests, revisión, depuración) |
| Inglés | README y documentación técnica en inglés |

## Cómo trabajar conmigo
- **Pide confirmación antes de crear, modificar o borrar archivos.** Primero propón el plan.
- Explica el porqué de cada decisión de forma breve; no hagas todo el trabajo si puedo hacerlo yo.
- Si te pido revisar código, señala problemas y mejoras antes de reescribirlo.
- Responde en español. El código, los nombres, los commits y el README, en inglés.
- No inventes: si no estás seguro de algo (versiones, APIs de Spring Boot 4), dilo.
- Ayúdame a poder explicar cada decisión en una entrevista (qué, por qué y alternativas).

## Stack
- Backend: Java 25, Spring Boot 4.1.x, Maven (instalación local, `mvn`; sin wrapper), Spring Web, Spring Data JPA,
  Validation, Flyway, PostgreSQL 16.
- Frontend (más adelante): React + TypeScript + Vite, TanStack Query.
- Infra: Docker Compose (PostgreSQL), GitHub Actions. Más adelante AWS (SQS/SNS).
- Entorno: Windows, PowerShell. Usa `mvn` y `docker compose`.

## Estructura
```
reservas/
  backend/            Spring Boot (pom.xml aquí)
  frontend/           React
  docker-compose.yml  PostgreSQL
  README.md           Visión general, arquitectura y decisiones de diseño (en inglés)
```
Paquete base: `com.rubenlazaro.reservas`

## Arquitectura
- **Monolito modular hexagonal** al principio, con paquetes separados por módulo. No crear
  microservicios sin que lo decidamos: la extracción de un servicio (notificaciones) es de la
  fase 4.
- `domain/`: modelo y reglas de negocio. **Sin Spring, JPA ni anotaciones de framework.**
- `application/`: casos de uso y puertos (interfaces). Depende solo de `domain`.
- `infrastructure/`: adaptadores (controladores REST, JPA, mensajería). Depende de `application`.
- Las dependencias van siempre hacia dentro; nunca el dominio hacia infraestructura.
- Las entidades JPA son de infraestructura y se mapean al modelo de dominio.
- La seguridad (Spring Security + JWT) se añade en la fase 3; mientras tanto, la reserva ya
  tiene un usuario asociado en el modelo.

## Convenciones
- Preferir `record` para DTOs y objetos de valor; sin Lombok.
- Inyección por constructor, sin `@Autowired` en campos.
- Validar la entrada de la API con Bean Validation (`@Valid`); las reglas de negocio, en el dominio.
- Errores con excepciones de dominio y un `@RestControllerAdvice` que las traduzca a HTTP.
- Cambios de base de datos solo con migraciones Flyway (`db/migration/V<n>__descripcion.sql`).
  Nunca modifiques una migración ya aplicada: crea una nueva. `ddl-auto: validate`.
- Commits pequeños y claros (Conventional Commits: `feat:`, `fix:`, `test:`, `chore:`).

## Tests
- Dominio: JUnit 5 + Mockito, sin Spring. Escribir el test antes (TDD) cuando sea posible.
- Unitarios (dominio y casos de uso): clases `*Test`. Los ejecuta Surefire con `mvn test`;
  no necesitan Docker.
- Integración: Testcontainers (PostgreSQL). Clases `*IT` que heredan de `IntegrationTest`, que
  levanta un PostgreSQL real en un contenedor y vacía las tablas antes de cada test. Los ejecuta
  Failsafe con `mvn verify` y necesitan Docker en marcha; sin él fallan al arrancar.
- Todo test que use Spring o la base de datos se llama `*IT`. Un test de integración llamado
  `*Test` correría en `mvn test` y exigiría Docker; uno sin el sufijo correcto no correría nunca.
- No usar H2: la regla anti-solape es una restricción `EXCLUDE` de PostgreSQL que H2 no soporta.
- API: Postman/Newman. E2E: Playwright.
- El pipeline debe bloquear el merge si fallan tests, cobertura o quality gate.

## Comandos (desde `backend/`)
- Tests unitarios: `mvn test` (rápidos, sin Docker)
- Todos los tests: `mvn verify` (los de integración `*IT` requieren Docker en marcha)
- Arrancar: `mvn spring-boot:run` (requiere la base de datos levantada)
- Swagger UI: `http://localhost:8080/swagger-ui.html`; documento OpenAPI en `/v3/api-docs`
- Base de datos (desde la raíz): `docker compose up -d`

## Fases
1. ✅ Hecha. Base y dominio (Booking, regla anti-solape) + API + Flyway.
2. Frontend React y calidad (tests de integración, API y E2E, CI con cobertura).
3. Seguridad (Spring Security + JWT) y tiempo real (WebSocket).
4. Eventos (SQS/SNS con LocalStack), servicio de notificaciones y despliegue en AWS con
   SonarCloud como quality gate.