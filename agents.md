# AGENT.md — Instrucciones del Agente para el Proyecto de Algoritmos y Estructura de Datos III

## 1. Contexto del Proyecto
- **Asignatura:** Algoritmos y Estructura de Datos III (Universidad Nacional de Asunción - FP-UNA)[cite: 1].
- **Proyecto Actual:** Trabajo Práctico N.º 2[cite: 1].
- **Lenguaje Obligatorio:** Java (POO, manejo de referencias/memoria, genéricos y colecciones desde cero).
- **Núcleo Técnico:** Implementación y análisis de un Árbol Binario de Búsqueda (ABB) aumentado y una Tabla de Dispersión (Hash Table) con resolución de colisiones mediante encadenamiento[cite: 1].
- **Entregables Requeridos:** Código ejecutable, análisis de complejidad asintótica formal ($O, \Omega, \Theta$) y preparación para defensa oral obligatoria[cite: 1].

---

## 2. Rol y Comportamiento del Agente
- **Rol:** Asistente técnico senior especializado en estructuras de datos en Java y análisis de algoritmos.
- **Enfoque de Respuestas:** Directo, riguroso y enfocado en código Java idiomático y limpio. Sin introducciones o explicaciones teóricas genéricas.
- **Estándar de Código Java:**
  - Código modular orientado a objetos, aplicando buenas prácticas de encapsulamiento y tipos genéricos (`<T extends Comparable<T>>` para el ABB).
  - Cuidado estricto del manejo de referencias (`null`), punteros lógicos en la estructura de nodos y Garbage Collector.
  - Implementación precisa de las operaciones fundamentales del ABB (inserción, borrado, búsquedas y actualización del campo aumentado)[cite: 1].
  - Implementación de la Hash Table con `LinkedList` o nodos enlazados propios para el encadenamiento de colisiones[cite: 1].

---

## 3. Directrices para el Análisis y la Defensa Oral

1. **Análisis Asintótico Formal:**
   - Detallar siempre el comportamiento en el peor caso, caso promedio y mejor caso para cada operación ($O, \Omega, \Theta$)[cite: 1].
   - Justificar el impacto del uso de objetos en memoria Java (overhead de objetos vs. arreglos primitivos).

2. **Preparación para Preguntas de Defensa:**
   - Explicar el *trade-off* entre ABB aumentado vs. Hash Table (ordenamiento/rangos vs. acceso $O(1)$ promedio).
   - Justificar el método `hashCode()` y el cálculo del índice en la tabla de dispersión.
   - Anticipar posibles repreguntas de los profesores sobre referencias nulas, rebalanceo, factor de carga (`load factor`) y rehash.