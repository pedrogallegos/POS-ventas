# Vamo POS

Sistema de punto de venta para registrar ventas, controlar caja e inventario y conservar una trazabilidad completa de las operaciones.

El proyecto comienza como una solución para un negocio familiar, pero se diseña como un monolito modular con aislamiento por empresa para permitir una futura evolución a SaaS.

## Stack

- Java 21 LTS
- Spring Boot 4.1
- PostgreSQL 17
- Flyway
- Spring Data JPA
- Spring Security
- Maven Wrapper
- React y TypeScript en una etapa posterior

## Estructura

```text
/
├── apps/
│   └── api/
├── outputs/
├── work/
├── compose.yaml
├── .env.example
└── README.md
```

## Requisitos locales

- JDK 21
- Docker Desktop o un servidor PostgreSQL 17 compatible
- Git

No es necesario instalar Maven globalmente. El repositorio contiene Maven Wrapper.

## Configuración local

Las credenciales `vamo_pos` / `vamo_pos_dev` son exclusivamente para desarrollo local. Los demás ambientes deben proporcionar secretos propios y nunca reutilizar estos valores.

Opcionalmente, copia `.env.example` como `.env` y cambia sus valores:

```powershell
Copy-Item ".env.example" ".env"
```

El archivo `.env` está excluido de Git.

## Comandos de desarrollo

Desde la raíz:

```powershell
docker compose up -d database
```

Desde `apps/api`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

En macOS o Linux:

```bash
./mvnw test
./mvnw spring-boot:run
```

Con la aplicación iniciada:

```text
GET http://localhost:8080/api/v1/health
GET http://localhost:8080/actuator/health
```

## Principios del sistema

- Las operaciones confirmadas no se eliminan para corregirlas.
- Las ventas, pagos, caja e inventario se confirman atómicamente.
- Cada operación relevante conserva ejecutor y, cuando aplique, autorizador.
- Toda información operativa pertenece a una empresa.
- Los reintentos no deben duplicar ventas ni cobros.
- Los importes usan decimales y las cantidades conservan precisión para peso y volumen.
- La base de datos protege las reglas críticas además de las validaciones de aplicación.

## Documentación funcional

- `outputs/Especificacion_funcional_MVP_POS.md`
- `outputs/Especificacion_funcional_MVP_POS.docx`
- `outputs/Plan_de_construccion_MVP_POS.md`
