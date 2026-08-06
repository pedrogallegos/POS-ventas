# Plan de construcción del MVP — Sistema de punto de venta

**Fecha:** 5 de agosto de 2026  
**Estado:** Propuesta inicial  
**Documento relacionado:** `Especificacion_funcional_MVP_POS.md`

## 1. Decisión tecnológica

- Backend: Java 21 LTS y Spring Boot 4.1.
- Persistencia: PostgreSQL, Spring Data JPA e Hibernate.
- Frontend: React, TypeScript y Vite, preparado como PWA.
- Pruebas: JUnit 5, Spring Boot Test y Testcontainers para backend; Vitest para frontend.
- Desarrollo local: Docker Compose para PostgreSQL y servicios auxiliares.
- Automatización: GitHub Actions.
- Arquitectura: monolito modular organizado por dominio.
- Construcción: Maven Wrapper para no depender de una instalación global de Maven.

La solución no comenzará con microservicios. Los módulos tendrán límites claros para que puedan separarse en el futuro únicamente si la operación lo justifica.

## 2. Objetivo del primer incremento ejecutable

Construir un recorrido vertical completo y demostrable:

1. Un usuario inicia sesión.
2. Abre un turno con fondo inicial.
3. Busca o escanea un producto.
4. Agrega una o varias partidas.
5. Cobra en efectivo.
6. El sistema confirma una sola venta.
7. Registra el pago y el movimiento de caja.
8. Descuenta el inventario.
9. Conserva quién realizó la operación.
10. Permite consultar la venta y generar un ticket básico.

Este recorrido será la primera versión funcional. Los pagos mixtos, productos por peso, apertura de empaques y Mercado Pago se agregarán sobre esta base.

## 3. Reglas técnicas que se fijan desde el inicio

- Los importes monetarios se almacenarán como valores decimales, nunca como `float` o `double`.
- Las cantidades admitirán precisión suficiente para gramos y mililitros.
- Toda fila operativa pertenecerá a una empresa mediante `tenant_id`.
- Las ventas confirmadas y los movimientos confirmados no se eliminarán ni editarán.
- Las correcciones se representarán mediante operaciones compensatorias.
- Completar una venta será una transacción atómica.
- Cada confirmación de venta utilizará una clave de idempotencia.
- Los datos históricos de una partida conservarán nombre, cantidad, precio y costo aplicados.
- Cada usuario tendrá una cuenta individual.
- Las acciones se autorizarán mediante permisos, no mediante nombres de roles escritos directamente en el código.
- Las fechas se almacenarán en UTC y se mostrarán con la zona horaria del negocio.
- La base de datos tendrá restricciones que protejan las reglas críticas, además de las validaciones de la aplicación.

## 4. Módulos iniciales

### Identidad

- Empresa.
- Usuario.
- Rol.
- Permiso.
- Sesión.

### Catálogo

- Categoría.
- Producto.
- Presentación.
- Código de barras.
- Precio.
- Unidad de medida.

### Operación de caja

- Sucursal.
- Caja.
- Turno.
- Movimiento de caja.

### Ventas

- Venta.
- Detalle de venta.
- Intento de pago.
- Pago.
- Ticket.

### Inventario

- Movimiento de inventario.
- Saldo de inventario.
- Motivo de ajuste.

### Auditoría

- Evento de auditoría.
- Autorización.

## 5. Orden de implementación

### Etapa 0 — Preparación

- Instalar un JDK 21 LTS.
- Inicializar el repositorio Git.
- Crear solución, proyectos y convenciones.
- Configurar PostgreSQL local.
- Agregar análisis estático, formato y pruebas en CI.
- Redactar README y decisiones de arquitectura.

### Etapa 1 — Identidad y catálogo

- Crear empresa y sucursal iniciales.
- Crear administrador inicial.
- Implementar inicio de sesión.
- Implementar permisos de administrador y vendedor.
- Crear, editar, buscar y desactivar productos.
- Registrar presentaciones, códigos y precios.

### Etapa 2 — Primera venta completa

- Abrir turno.
- Crear carrito.
- Agregar productos.
- Calcular totales.
- Cobrar en efectivo.
- Confirmar de forma idempotente.
- Generar movimientos de caja e inventario en la misma transacción.
- Consultar la venta y generar ticket básico.

### Etapa 3 — Operación real del MVP

- Pagos mixtos.
- Mercado Pago registrado manualmente.
- Cierre y arqueo de turno.
- Devoluciones autorizadas.
- Ajustes y conteos de inventario.
- Auditoría consultable.

### Etapa 4 — Productos especiales

- Captura manual controlada de peso.
- Conector local para báscula.
- Apertura de cajas y paquetes.
- Registro de merma y diferencias.

### Etapa 5 — Resiliencia e integraciones

- PWA instalable.
- Cola local y sincronización offline.
- Integración con Mercado Pago Point.
- Impresión mediante agente local.

## 6. Criterio de terminado para cada funcionalidad

Una historia no se considerará terminada hasta que:

- Tenga reglas y criterios de aceptación.
- Valide permisos.
- Incluya migración de base de datos cuando corresponda.
- Incluya pruebas de la regla principal y sus errores importantes.
- Registre auditoría cuando aplique.
- No exponga datos de otra empresa.
- Maneje reintentos sin duplicar operaciones.
- Pueda demostrarse desde la interfaz.
- Esté documentada en el README o documentación de API.

## 7. Estructura propuesta del repositorio

```text
/
├── apps/
│   ├── api/
│   └── pos-web/
├── tests/
│   ├── unit/
│   ├── integration/
│   └── architecture/
├── docs/
│   ├── requirements/
│   ├── architecture/
│   ├── decisions/
│   └── diagrams/
├── deploy/
├── compose.yaml
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .editorconfig
└── README.md
```

## 8. Decisiones que no bloquean el arranque

Pueden resolverse durante las pruebas con el negocio:

- Marca y protocolo exactos de la báscula.
- Integración directa con Mercado Pago.
- Estrategia offline definitiva.
- Promociones y clientes frecuentes.
- Compras y proveedores.
- FIFO o FEFO por lote.
- Suscripción y facturación SaaS.

## 9. Primer entregable para el portafolio

El repositorio deberá mostrar:

- Descripción del problema real.
- Diagrama de arquitectura.
- Modelo de dominio.
- Decisiones técnicas justificadas.
- API documentada con OpenAPI.
- Migraciones reproducibles.
- Pruebas automatizadas.
- Pipeline de integración continua.
- Datos de demostración sin información sensible.
- Video o demo del flujo completo.
- Instrucciones para ejecutar el sistema localmente.

## 10. Próxima acción inmediata

Instalar Eclipse Temurin JDK 21 LTS y verificar:

```powershell
winget install --id EclipseAdoptium.Temurin.21.JDK --exact
java -version
javac -version
```

Después se inicializará Git y se generará la aplicación base sin implementar todavía todas las entidades. El primer objetivo técnico será levantar la API, PostgreSQL y una prueba de integración.
