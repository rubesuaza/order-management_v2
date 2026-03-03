## Especificaciones de Infraestructura

Este documento describe las decisiones técnicas de infraestructura para el sistema de gestión de órdenes (**order_management**), incluyendo persistencia, base de datos y API REST. Sirve como contrato para la implementación en la capa `infrastructure/`.

---

## 1. Persistencia y Base de Datos

- **Framework principal**: Spring Boot + Spring Data JPA (Hibernate como provider JPA).
- **Base de datos**: PostgreSQL.
- **Pool de conexiones**: HikariCP (configuración por defecto de Spring Boot).

### 1.1 Reglas de Transacciones

- Todas las operaciones que modifican el estado de un aggregate (`Order`) deben ser **atómicas**.
- Las operaciones de casos de uso de escritura (crear pedido, pagar pedido, etc.) deben:
  - Estar anotadas con `@Transactional` en el **servicio de aplicación** correspondiente.
  - Evitar transacciones largas que abarquen lógica de presentación.

---

## 2. Esquema de Base de Datos (Modelo Lógico)

El diseño de tablas es agnóstico a la tecnología, pero la implementación se hará con PostgreSQL.

### 2.1 Tabla `orders`

Entidad principal que persiste el aggregate `Order`.

| Columna       | Tipo              | Restricciones         | Descripción                                  |
|--------------|-------------------|------------------------|----------------------------------------------|
| `id`         | UUID              | PK, NOT NULL          | Identificador único del pedido               |
| `customer_id`| UUID              | NOT NULL              | Referencia a entidad externa Customer        |
| `status`     | VARCHAR(20)       | NOT NULL              | Estado del pedido (PENDING, PAID, SHIPPED)   |
| `total_amount`| DECIMAL(19, 2)   | NOT NULL              | Valor total del pedido                       |
| `currency`   | VARCHAR(3)        | NOT NULL              | Código ISO de moneda (USD, EUR, etc.)        |
| `created_at` | TIMESTAMP         | NOT NULL              | Fecha y hora de creación                     |

### 2.2 Tabla `order_items`

Detalle de ítems para cada pedido.

| Columna      | Tipo              | Restricciones         | Descripción                                  |
|-------------|-------------------|------------------------|----------------------------------------------|
| `id`        | UUID              | PK, NOT NULL          | Identificador único del ítem                 |
| `order_id`  | UUID              | FK, NOT NULL          | Referencia al pedido padre (`orders.id`)     |
| `product_id`| UUID              | NOT NULL              | Referencia a entidad externa Product         |
| `quantity`  | INTEGER           | NOT NULL, > 0         | Cantidad de unidades                         |
| `unit_price`| DECIMAL(19, 2)    | NOT NULL              | Precio por unidad al momento de compra       |

### 2.3 Relaciones

- Relación **1:N** entre `orders` y `order_items`:
  - Un pedido puede tener varios ítems.
  - Cada ítem pertenece exactamente a un pedido.
- Regla de negocio:
  - No se permite `ON DELETE CASCADE` en la FK `order_items.order_id` para mantener trazabilidad.

### 2.4 Índices y Búsqueda

- Índice por `customer_id` en `orders` para búsquedas por cliente.
- Índice por `status` en `orders` para filtros por estado.

---

## 3. Implementación en la Capa de Infraestructura

### 3.1 Paquetes Relevantes

- `infrastructure/adapters/out/persistence`:
  - Entidades JPA (`OrderJpaEntity`, `OrderItemJpaEntity`, etc.).
  - Repositorios Spring Data (`SpringDataOrderRepository`, etc.).
  - Mappers entre modelos de dominio y entidades JPA.

- `application/ports/out`:
  - Interface de repositorio de dominio (`OrderRepository`, etc.).

La capa de infraestructura implementa las interfaces definidas en `application/ports/out` y no al revés.

### 3.2 Entidades JPA vs Modelos de Dominio

- Las entidades JPA deben vivir **solo** en `infrastructure/adapters/out/persistence`.
- No se debe anotar con `@Entity` las clases de dominio.
- Es obligatorio tener un **mapper** entre:
  - `Order` (dominio) ↔ `OrderJpaEntity` (infraestructura).
  - `OrderItem` (dominio) ↔ `OrderItemJpaEntity` (infraestructura).

### 3.3 Repositorios

- Uso del patrón Repository a través de Spring Data JPA:
  - Interfaces específicas de Spring Data (`JpaRepository`) se definen en la capa de infraestructura.
  - La interfaz de repositorio visible para la aplicación es el port de salida (`OrderRepository`) en `application/ports/out`.

---

## 4. API REST (Especificación Ligera)

Base URL: `/api/v1`

Los controladores HTTP deben vivir en `infrastructure/adapters/in/web`.

### 4.1 Crear Pedido (Create Order)

- **Método**: `POST`
- **Ruta**: `/orders`
- **Descripción**: Crea un nuevo pedido con ítems iniciales.

**Request Body (JSON)**:

```json
{
  "customerId": "uuid-string",
  "items": [
    {
      "productId": "uuid-string",
      "quantity": 2,
      "unitPrice": 15.50
    }
  ]
}
```

**Respuesta 201 (Created)**:

```json
{
  "orderId": "uuid-string",
  "status": "PENDING",
  "totalAmount": 31.00,
  "createdAt": "2023-10-27T10:00:00"
}
```

- **Errores**:
  - `400 Bad Request`: lista de ítems vacía, datos inválidos o violación de reglas de dominio (p.ej. total < 10.00).

### 4.2 Obtener Detalle de Pedido (Get Order Details)

- **Método**: `GET`
- **Ruta**: `/orders/{orderId}`
- **Descripción**: Recupera el detalle completo de un pedido.

**Respuesta 200 (OK)**:

```json
{
  "orderId": "uuid-string",
  "customerId": "uuid-string",
  "status": "PAID",
  "items": [],
  "totalAmount": 100.00,
  "currency": "USD"
}
```

- **Errores**:
  - `404 Not Found`: si el `orderId` no existe.

### 4.3 Pagar Pedido (Pay Order)

- **Método**: `POST`
- **Ruta**: `/orders/{orderId}/pay`
- **Descripción**: Procesa el pago del pedido (simulado), cambiando el estado a `PAID`.

**Respuesta 200 (OK)**:

```json
{
  "orderId": "...",
  "status": "PAID"
}
```

- **Errores**:
  - `409 Conflict`: si el pedido está cancelado o ya está pagado.

---

## 5. Integraciones Externas

En esta fase, las integraciones externas se modelan principalmente como:

- **Clientes externos lógicos**:
  - `Customer` (identificado por `customerId`).
  - `Product` (identificado por `productId`).

La implementación concreta (microservicios remotos, colas, etc.) puede extenderse más adelante a través de:

- Nuevos ports en `application/ports/out`.
- Adaptadores correspondientes en `infrastructure/adapters/out`.

---

## 6. Consideraciones de Implementación

- Manejar las excepciones de dominio (`DomainException` y derivadas) en la capa de infraestructura/adaptadores de entrada mediante:
  - `@ControllerAdvice` y `@ExceptionHandler` para mapearlas a códigos HTTP adecuados (400, 404, 409, etc.).
- Mantener el mapeo claro entre:
  - DTOs de entrada/salida (en `infrastructure/adapters/in/web`).
  - Modelos de dominio (`domain/...`).
  - Entidades JPA (`infrastructure/adapters/out/persistence`).

Este documento debe ser consultado antes de modificar o extender la infraestructura de persistencia o las APIs REST del flujo **order_management**.

