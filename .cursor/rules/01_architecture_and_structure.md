## Arquitectura y Estructura del Proyecto

Este documento define la arquitectura base y la estructura de paquetes para el contexto de **Order Management** en el proyecto `management`. Es la fuente de verdad para cualquier decisión futura sobre organización de código.

---

## Estilo Arquitectónico

- **Patrón principal**: **Arquitectura Hexagonal (Ports & Adapters)**.
- **Objetivo**: Aislar el dominio del resto de la aplicación, permitiendo:
  - Independencia de frameworks (Spring, JPA, etc.) en el dominio.
  - Sustituir tecnologías de infraestructura sin afectar la lógica de negocio.
  - Facilitar pruebas unitarias del dominio sin dependencias externas.

---

## Capas y Responsabilidades

- **Capa de Dominio (`domain/`)**
  - Contiene la lógica de negocio pura.
  - No debe depender de ningún framework (Spring, JPA, etc.).
  - Incluye:
    - `model/aggregate`: Aggregates (p.ej. `Order`).
    - `model/entity`: Entidades internas del agregado (p.ej. `OrderItem`).
    - `model/valueobject`: Value Objects (p.ej. `Money`, `Address`).
    - `exception`: Excepciones puramente de dominio.

- **Capa de Aplicación (`application/`)**
  - Orquesta casos de uso sin contener reglas de negocio complejas.
  - Define y usa **ports** de entrada y salida:
    - `ports/in`: Interfaces de casos de uso (input ports).
    - `ports/out`: Interfaces de acceso a recursos externos (repositorios, gateways).
  - Implementa servicios de aplicación:
    - `services`: Implementaciones de los casos de uso que coordinan el dominio y la infraestructura.

- **Capa de Infraestructura (`infrastructure/`)**
  - Implementa los **adapters** que conectan el mundo externo con los ports.
  - Puede usar frameworks (Spring Boot, Spring Data JPA, etc.).
  - Incluye:
    - `adapters/in`: Adaptadores de entrada (REST Controllers, CLI, etc.).
      - `web`: Controladores HTTP y DTOs.
    - `adapters/out`: Adaptadores de salida (repositorios JPA, colas, SMTP, etc.).
      - `persistence`: Repositorios y entidades JPA específicas de la base de datos.
    - `config`: Configuración de Spring y beans.

---

## Estructura de Directorios

La estructura objetivo, parametrizada por `project_name = management`, es:

```text
src/main/java/com/example/management/
├── application/                     # Capa de Aplicación (Orquestación)
│   ├── ports/                       # Ports (Interfaces)
│   │   ├── in/                      # Input Ports (Interfaces de Casos de Uso)
│   │   └── out/                     # Output Ports (Repositorios / Gateways)
│   └── services/                    # Implementaciones de Casos de Uso
├── domain/                          # Capa de Dominio (Lógica de Negocio Pura)
│   ├── model/                       # Aggregates, Entidades y Value Objects
│   │   ├── aggregate/
│   │   ├── entity/
│   │   └── valueobject/
│   └── exception/                   # Excepciones de Dominio
├── infrastructure/                  # Capa de Infraestructura (Adapters Técnicos)
│   ├── adapters/                    # Implementación de Ports
│   │   ├── in/                      # Adaptadores de entrada (REST, CLI)
│   │   │   └── web/                 # Controladores & DTOs
│   │   └── out/                     # Adaptadores de salida (DB, Mensajería)
│   │       └── persistence/         # Repositorios DB & Entidades JPA
│   └── config/                      # Configuración de Framework (Spring Beans)
└── ManagementApplication.java       # Clase principal de Spring Boot
```

---

## Reglas de Dependencia entre Capas

- `domain` **no puede depender** de `application` ni de `infrastructure`.
- `application` **puede depender** de `domain`, pero **no** de `infrastructure`.
- `infrastructure` **puede depender** de `application` y `domain`.

En términos de imports:

- **Permitido**:
  - `application` importa modelos y excepciones de `domain`.
  - `infrastructure` importa ports de `application` y modelos de `domain`.
- **Prohibido**:
  - Cualquier import de `org.springframework.*`, `javax.persistence.*` o `jakarta.persistence.*` dentro de `domain`.

---

## Convenciones de Nombres

- Aggregates: `Order`, `OrderId`, etc., en `domain/model/aggregate`.
- Entidades internas: `OrderItem`, etc., en `domain/model/entity`.
- Value Objects: `Money`, `Address`, `OrderStatus`, etc., en `domain/model/valueobject`.
- Excepciones de dominio: `DomainException`, `InvalidOrderStateException`, etc., en `domain/exception`.
- Interfaces de ports:
  - Input: `CreateOrderUseCase`, `GetOrderQuery`, etc., en `application/ports/in`.
  - Output: `OrderRepository`, etc., en `application/ports/out`.
- Adaptadores:
  - Entrantes (REST): `OrderController`, `OrderDto`, `CreateOrderRequest`, etc., en `infrastructure/adapters/in/web`.
  - Salientes (Persistencia): `JpaOrderRepository`, `OrderJpaEntity`, etc., en `infrastructure/adapters/out/persistence`.

---

## Objetivo de estas Directrices

Todas las nuevas funcionalidades del flujo **order_management** deben respetar:

- La **separación clara de responsabilidades** entre capas.
- La **pureza del dominio**.
- La utilización disciplinada de ports y adapters según la arquitectura hexagonal.

Este documento debe consultarse antes de crear nuevos paquetes, clases o módulos relacionados con la gestión de órdenes.

