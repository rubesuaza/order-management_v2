## Patrones de Diseño a Aplicar

Este documento describe los patrones de diseño que deben utilizarse en el contexto de **Order Management** y cómo aplicarlos específicamente en la pila Java + Spring Boot + Hexagonal Architecture.

Fuente: bloque [PATTERNS] proporcionado.

---

## 1. Resumen de Patrones

- **Builder (Creational)** — `java-lombok-builder`
- **Proxy (Structural)** — `java-dynamic-proxy`
- **Strategy (Behavioral)** — `java-strategy-context`
- **Repository (Architectural)** — `java-spring-data-jpa`

Cada patrón debe utilizarse donde aporte claridad, mantenibilidad y alineación con la arquitectura hexagonal.

---

## 2. Builder (`java-lombok-builder`)

- **Nombre**: Builder  
- **Categoría**: Creational  
- **Guía de implementación**:
  - Usar `@Builder` de Lombok para reducir boilerplate cuando sea adecuado.
  - Para implementación manual, usar una clase `Builder` estática interna con métodos fluentes que retornan `this` y un método `build()` terminal.

### 2.1 Uso recomendado en el proyecto

- Aplicar **Builder** principalmente en:
  - DTOs de entrada/salida de la capa web (`infrastructure/adapters/in/web`).
  - Objetos complejos de configuración.
  - Opcionalmente, en aggregates o value objects del dominio cuando mejoren la legibilidad de la creación (respetando siempre la inmutabilidad de los value objects).

### 2.2 Reglas

- En el dominio:
  - Si se usa Lombok, evitar añadir lógica pesada al `build()`. Las invariantes siguen viviendo en el constructor o métodos de dominio.
  - Mantener los value objects inmutables (atributos `final` y sin setters).
- En DTOs:
  - `@Builder` + `@AllArgsConstructor` + `@NoArgsConstructor` pueden usarse para facilitar serialización/deserialización.

---

## 3. Proxy (`java-dynamic-proxy`)

- **Nombre**: Proxy  
- **Categoría**: Structural  
- **Guía de implementación**:
  - Usar `java.lang.reflect.Proxy` para proxies dinámicos basados en interfaces.
  - Usar Spring AOP (`@Aspect`) para preocupaciones transversales (logging, métricas, seguridad, etc.).

### 3.1 Uso recomendado en el proyecto

- Para el flujo **order_management**, el patrón Proxy se usará principalmente a través de:
  - **Spring AOP**:
    - Logging de casos de uso (por ejemplo, tiempos de ejecución de servicios de aplicación).
    - Manejo transversal de auditoría.
  - Proxies dinámicos manuales solo cuando sea necesario envolver interfaces específicas sin introducir dependencias fuertes de Spring en ciertos módulos.

### 3.2 Reglas

- No introducir lógica de negocio en aspectos (aspects); deben manejar solo cross-cutting concerns.
- Mantener el dominio libre de dependencias de AOP; los aspectos deben vivir en la capa de infraestructura o configuración.

---

## 4. Strategy (`java-strategy-context`)

- **Nombre**: Strategy  
- **Categoría**: Behavioral  
- **Guía de implementación**:
  - Definir una interfaz Strategy.
  - Una clase Context mantiene una referencia a una Strategy.
  - Usar `@Component` de Spring con `@Qualifier` o un `Map<String, Strategy>` para seleccionar estrategias en tiempo de ejecución.

### 4.1 Uso recomendado en el proyecto

Ejemplos de uso potencial:

- Estrategias de cálculo de descuentos/promociones según tipo de cliente o campaña.
- Estrategias de validación adicional según tipo de pedido.

Estas estrategias pueden:

- Definirse como interfaces en el dominio si son reglas puras de negocio.
- Implementarse en infraestructura si dependen de configuraciones externas o servicios remotos.

### 4.2 Reglas

- Para estrategias puramente de negocio:
  - La interfaz debe residir en `domain` o `application/ports/in` (si se exponen como casos de uso).
  - Implementaciones específicas pueden residir en `domain` (si son puras) o en `infrastructure` (si usan frameworks).
- Para selección dinámica con Spring:
  - Usar inyección de un `Map<String, Strategy>` en servicios de aplicación.
  - La clave del mapa puede ser un código de estrategia configurable.

---

## 5. Repository (`java-spring-data-jpa`)

- **Nombre**: Repository  
- **Categoría**: Architectural  
- **Guía de implementación**:
  - Implementar una interfaz que extienda `JpaRepository`.
  - Usar entidades de dominio en las firmas de la interfaz de port (`OrderRepository`).
  - Usar entidades JPA específicas en la implementación con un Mapper.

### 5.1 Uso recomendado en el proyecto

Capas implicadas:

- `application/ports/out`:
  - Define interfaces de repositorio en términos de modelos de dominio (`Order`, etc.).
- `infrastructure/adapters/out/persistence`:
  - Define interfaces `SpringData...Repository` que extienden `JpaRepository<JpaEntity, IdType>`.
  - Implementa el port `OrderRepository` delegando en los repositorios Spring Data y usando mappers para convertir entre dominio y JPA.

### 5.2 Reglas

- El dominio no conoce ni depende de `JpaRepository` ni de entidades JPA.
- El port de salida debe ser expresivo en términos de operaciones de negocio (por ejemplo, `findById`, `save`, `findByCustomerIdAndStatus`, etc.) sin exponer detalles de JPA.
- Los métodos de repositorio deben respetar las invariantes de dominio (por ejemplo, no romper consistencia del aggregate `Order`).

---

## 6. Reglas Generales de Uso de Patrones

- **Consistencia**:
  - Usar estos patrones de forma coherente en todo el flujo **order_management**.
  - Evitar combinaciones ad-hoc de estilos que no estén alineados con estas guías.

- **Aislamiento de Frameworks**:
  - Los patrones que requieren Spring o JPA (Proxy con AOP, Repository con Spring Data) deben implementarse fuera del dominio.

- **Legibilidad sobre sofisticación**:
  - No aplicar patrones si solo añaden complejidad sin aportar claridad o extensibilidad.

Este documento debe ser la referencia al decidir cómo estructurar builders, repositorios, estrategias o proxies relacionados con la gestión de órdenes.

