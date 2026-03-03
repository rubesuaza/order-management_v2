## Lógica de Dominio: Order Management

Este documento describe las reglas de negocio, entidades y invariantes del contexto de **Order Management**. Es la referencia obligatoria para implementar modelos y reglas en la capa `domain/`.

---

## 1. Estilo de Dominio

- **Arquitectura**: Hexagonal (Ports & Adapters) con enfoque de **DDD táctico**.
- **Regla clave**: El paquete `domain/` debe usar únicamente la librería estándar de Java (sin dependencias de frameworks).
- **Ubicación**:
  - Aggregates: `domain/model/aggregate`
  - Entidades: `domain/model/entity`
  - Value Objects: `domain/model/valueobject`
  - Excepciones: `domain/exception`

---

## 2. Aggregates

### 2.1 `Order` (Aggregate Root)

- **Identidad**: `OrderId` (Value Object basado en UUID).
- **Atributos principales**:
  - `orderId: OrderId`
  - `status: OrderStatus` (Enum)
  - `items: List<OrderItem>`
  - `totalAmount: Money`
  - (Opcional) `customerId` y datos asociados como `Address` si se modelan en el dominio.

- **Responsabilidades**:
  - Gestionar el ciclo de vida del pedido (estado).
  - Mantener la consistencia interna de sus `OrderItem`.
  - Recalcular el `totalAmount` cuando cambian los ítems.
  - Validar las reglas de negocio asociadas a las transiciones de estado.

- **Operaciones típicas del aggregate** (no exhaustivo):
  - `addItem(productId, quantity, unitPrice: Money)`
  - `removeItem(itemId)`
  - `placeOrder()`
  - `cancel()`
  - `pay()`
  - `ship()`

Estas operaciones deben:

- Encapsular la creación/modificación de `OrderItem`.
- Mantener las invariantes descritas en la sección siguiente.

---

## 3. Entidades de Dominio

### 3.1 `OrderItem` (Entidad interna del aggregate `Order`)

- **Identidad**: ID interno (p.ej. `UUID` o secuencia local) con significado solo dentro de `Order`.
- **Atributos**:
  - `id`
  - `productId` (UUID u otro identificador del producto externo).
  - `quantity` (int)
  - `unitPrice: Money`

- **Restricción clave**:
  - Solo puede ser creado o modificado a través de métodos del aggregate `Order`.
  - No debe existir fuera del contexto de un `Order`.

---

## 4. Value Objects

### 4.1 `Money`

- **Atributos**:
  - `amount: BigDecimal`
  - `currency: String` (código ISO, p.ej. `"USD"`)

- **Características**:
  - Inmutable: cualquier operación retorna una nueva instancia.
  - Validaciones:
    - El `amount` no debe ser `null`.
    - La `currency` no debe ser `null` ni vacía.

- **Operaciones típicas**:
  - `add(Money other)`
  - `subtract(Money other)`
  - `multiply(int factor)` o `multiply(BigDecimal factor)`

- **Regla de negocio**:
  - Si se operan cantidades con distinta moneda, lanzar `CurrencyMismatchException`.

### 4.2 `Address`

- **Uso**:
  - Representa una dirección asociada al pedido (envío, facturación, etc.) si se modela en el dominio.
- **Características**:
  - Inmutable.
  - Define campos típicos como calle, ciudad, país, código postal.

### 4.3 `OrderStatus` (Enum)

- **Valores típicos**:
  - `PENDING`
  - `PAID`
  - `SHIPPED`
  - (Opcional) `CANCELLED`, `REJECTED`, etc.

- **Uso**:
  - Controlar el flujo de estado del pedido siguiendo las reglas de transición.

### 4.4 `OrderId`

- **Descripción**:
  - Value Object que encapsula un UUID.
  - Evita el uso de `UUID` crudo en el dominio, facilitando la legibilidad y consistencia.

---

## 5. Invariantes de Modelo

Las siguientes reglas deben cumplirse **siempre**:

1. **Cálculo del Total**  
   - Cada vez que se agregue o elimine un `OrderItem`, el `Order` debe recalcular `totalAmount` sumando `quantity * unitPrice` de todos los ítems.
   - La operación debe respetar la moneda (`currency`) del Value Object `Money`.

2. **Cantidad Positiva**  
   - En la construcción de un `OrderItem`, `quantity` debe ser estrictamente mayor que 0.
   - Si no se cumple, se debe lanzar `InvalidItemException`.

3. **Consistencia de Moneda**  
   - El `Money` de `unitPrice` y el `Money` usado en cálculos dentro de `Order` deben compartir la misma `currency`.
   - Si se intenta operar con distintas monedas, se lanza `CurrencyMismatchException`.

---

## 6. Reglas de Transición de Estado

Las transiciones de estado del `Order` deben ser controladas exclusivamente por métodos del aggregate, nunca por setters directos.

- **Place Order (crear/confirmar pedido)**:
  - Requisito: `totalAmount >= 10.00` (en la moneda definida, p.ej. USD).
  - Si el total es menor a 10.00, lanzar una excepción de dominio apropiada (p.ej. `InvalidOrderStateException` o específica del negocio).

- **Cancel (cancelar pedido)**:
  - Permitido solo si el estado actual es `PENDING` o `PAID`.
  - Prohibido si el estado es `SHIPPED`.
  - Violaciones deben lanzar `InvalidOrderStateException`.

- **Ship (enviar pedido)**:
  - Requisito: el estado anterior debe ser `PAID`.
  - Cualquier otro estado previo debe producir `InvalidOrderStateException`.

Estas reglas deben implementarse en métodos del aggregate que controlen el cambio de `OrderStatus`.

---

## 7. Excepciones de Dominio

Ubicación: `domain/exception`

- **`DomainException`**
  - Clase base (checked o unchecked según la decisión de diseño).
  - Todas las excepciones de dominio deben extender de esta.

- **`InvalidOrderStateException`**
  - Usada para violaciones de reglas de transición de estado (`PENDING` → `PAID` → `SHIPPED`, etc.).

- **`InvalidItemException`**
  - Usada para errores en ítems (cantidad <= 0, precios negativos, etc.).

- **`CurrencyMismatchException`**
  - Usada cuando se intentan combinar o operar objetos `Money` con monedas distintas.

---

## 8. Restricciones de Implementación

1. **Pureza de Dominio**  
   - Prohibido usar:
     - `org.springframework.*`
     - `javax.persistence.*`
     - `jakarta.persistence.*`
   - El dominio no debe estar anotado con `@Entity`, `@Table`, `@Component`, etc.

2. **Sin Servicios "Gordos" en Dominio**  
   - No crear servicios de dominio que hagan orquestaciones de casos de uso.
   - Si la lógica pertenece a una sola entidad/aggregate, ubicarla dentro de esa clase.
   - Si es un flujo de uso (caso de uso), debe ir en `application/services`.

3. **Validaciones en Constructores/Métodos de Dominio**  
   - Todas las invariantes de negocio deben ser validadas:
     - En constructores.
     - En métodos que cambian el estado interno del aggregate o entidad.
   - No se debe delegar la validación del corazón del negocio a servicios externos o capas superiores.

---

## 9. Relación con la Capa de Aplicación

- `application/ports/in` define los casos de uso (p.ej. `CreateOrderUseCase`).
- `application/ports/out` define interfaces para persistencia y otros sistemas externos (p.ej. `OrderRepository`).
- Los servicios de aplicación (`application/services`) deben:
  - Crear y manipular `Order` y sus entidades/value objects respetando las reglas anteriores.
  - Propagar o traducir las excepciones de dominio según sea necesario hacia capas superiores.

Este documento debe usarse como contrato de referencia antes de modificar o extender el modelo de dominio del flujo **order_management**.

