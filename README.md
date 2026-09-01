# TP2 — ABB Aumentado + Índice Doble

**Algoritmos y Estructura de Datos III (FP-UNA) — Trabajo Práctico N.º 2**
Integrantes: _(completar)_ · Aula virtual Educa · Entrega: 15/09/2026
Sin modificar el código tras la fecha límite: el entregado es el que se defiende.

---

## 1. Decisiones de diseño

- **ABB aumentado con punteros `izq`/`der`** (sin puntero al padre): `sucesor` y
  `predecesor` se resuelven bajando desde la raíz guardando candidatos, en
  O(h) tiempo y O(1) espacio extra. Evita mantener un enlace extra que agregaría
  un gasto de mantenimiento sin aportar al invariante.
- **`eliminar` muda el sucesor, no copia la clave.** Cuando el nodo tiene dos
  hijos se desengancha el mínimo del subárbol derecho con `extraerMinimo` y se
  reenlaza íntegro en la posición del eliminado. Cada `Nodo` conserva su clave
  desde la inserción hasta su muerte, requisito para que `IndiceDoble` guarde
  referencias válidas a los nodos.
- **Una sola fuente de verdad para el tamaño:** `size()` lee `raiz.tamano`
  (0 si vacío); no existe un campo `size` aparte que pueda desincronizarse.
  La actualización del campo aumentado se hace siempre al volver de la
  recursión: `n.tamano = 1 + tamano(izq) + tamano(der)`.
- **Composición, no herencia, en `IndiceDoble`:** el árbol es dueño de los
  nodos; la tabla es un `TablaEncadenada<K, Nodo<K,V>>` que guarda *referencias*
  a esos nodos (no copias del valor). `obtener` resuelve por la tabla; `kEsimo`
  y `consultarRango` delegan en el árbol.
- **Contadores de medición:** `visitas` (árbol) y `sondas` (tabla) son
  instrumentos del experimento, no parte del TAD. Solo las operaciones que
  descienden por la estructura los incrementan; `toString`, `iterator`,
  `tamanosConsistentes` no los tocan.
- **Compresión del hash:** `h(k) = (k.hashCode() & 0x7fffffff) % m`. El
  `& 0x7fffffff` descarta el bit de signo, lo que evita índices negativos
  (en particular `Integer.MIN_VALUE`). Al superar `alfaMax` (por defecto 1)
  se duplica `m` y se reubican todas las claves (rehash).
- **Excepciones propias.** Chequeada: `ClaveInexistenteException` (buscar una
  clave inexistente es una situación prevista y manejable). No chequeadas:
  `IndiceFueraDeRangoException` (k fuera de [1,n]), `ClaveNulaException`
  (null), `RangoInvalidoException` (a > b): errores de programación del
  llamador, no casos operativos.
- **Memoria (objetos Java):** cada nodo del árbol es un objeto que además de
  clave y valor transporta dos referencias y un `int`. El `int tamano` es
  variable primitiva en el objeto: no cambia el orden espacial. La tabla es un
  arreglo de referencias a cabeceras de lista; la lista usa un `NodoLista` por
  par. Todo esto mantiene el costo Θ(n+m) con mayor constante que un arreglo
  primitivo, precio que se paga por la separación índice/orden pedida.

---

## 2. Análisis asintótico formal (Ejercicio 1)

Notación: `h` es la altura del árbol, `n` el número de claves.

### 2.1 Recurrencia de `kEsimo` en función de la altura

En cada nodo se decide en Θ(1) (leer `tamano(izq)`, restar y comparar) y se baja
a **uno solo** de los subárboles → un paso menos de altura por llamada:

```
T(h) = T(h - 1) + Θ(1),   T(1) = Θ(1)
```

Desenrollando: `T(h) = T(1) + (h - 1)·Θ(1) = Θ(h)`.

### 2.2 Búsqueda en un ABB perfectamente balanceado

El subproblema se reduce a la mitad en cada llamada, siempre con Θ(1) de trabajo
local:

```
T(n) = T(n/2) + Θ(1),   T(1) = Θ(1)
```

**Teorema Maestro:** `a = 1, b = 2, f(n) = Θ(1)`. Como
`f(n) = Θ(n^{log_2 1}) = Θ(1)`, estamos en el **caso 2** y
`T(n) = Θ(n^{log_2 1} · log n) = Θ(log n)`.

### 2.3 Búsqueda en un ABB degenerado (inserción ordenada)

Cada llamada se descarta una clave y queda un subproblema de tamaño `n-1` (el
subproblema NO es de tamaño `n/b`, por eso el Teorema Maestro **no aplica**):

```
T(n) = T(n - 1) + Θ(1),   T(1) = Θ(1)
```

Desenrollando: `T(n) = Θ(1) + (n-1)·Θ(1) = Θ(n)`. Es la lista enlazada: altura
`n - 1`, y `kEsimo` baja hasta el medio.

### 2.4 `consultarRango` y su versión ingenua

- **Aumentado:** `rango[a, b] = cuantosMenoresOIguales(b) − cuantosMenores(a)`.
  Cada término baja por un camino: Θ(h) tiempo y O(1) espacio extra (implementado
  por recorridos iterativos/recursivos por camino; recursivo agrega Θ(h) de
  pila). El enunciado pide lo más ajustado: **Θ(h)**.
- **Ingenuo:** recorre todos los nodos en inorden: **Θ(n)** tiempo en todo caso.

### 2.5 Coste espacial del árbol

Cada nodo existe una sola vez y aporta una constante de memoria (clave, valor,
dos referencias, un `int`): **Θ(n)**. El campo `tamano` es un `int` por nodo,
constante; no altera el orden.

### 2.6 Relación con la tabla de visitas (ver §4, tabla 1)

- `h_ord = N - 1`: es la altura de la lista de §2.3, medible.
- `vis_kEsimo_ord = N/2`: el k-ésimo medio en una lista paga §2.3 con T(N/2)
  exacto → Θ(N).
- `vis_rango_ing = N`: el ingenuo toca los N nodos (§2.4).
- `vis_rango_aum` en decenas: dos caminos de altura ~log₂(N) (§2.4 aumentado).
- `h_aleat` ≈ 27-29 ≈ log₂(N)+ε: réplica empírica del §2.2 (Θ(log n)) sobre
  permutaciones de semilla 2026.

---

## 3. Análisis asintótico formal (Ejercicio 2)

### 3.1 Búsqueda con encadenamiento

Factor de carga α = n/m. Con un hash uniforme cada cubeta tiene α claves
esperadas, así que `obtener` recorre α + la propia: **caso esperado Θ(1 + α)**
(una sonda por búsqueda si α→0; lineal en α si α crece). **Peor caso Θ(n)**:
todas las claves caen en la misma cubeta. Espacio: **Θ(n + m)** (arreglo de `m`
cabeceras + `n` nodos de lista).

### 3.2 `agregar` del Índice Doble

Caso promedio: insertar en el árbol Θ(h) más insertar en la tabla Θ(1 + α):
**Θ(h) + Θ(1 + α)**. Con α ≤ αmax = 1 queda esperado Θ(h). Peor caso de una
llamada: la que dispara rehash reubica las `n` claves → **Θ(n)**, pero como `m`
se duplica siempre, el trabajo total de rehash tras insertar n claves desde
vacío es `n + n/2 + n/4 + … < 2n`, es decir **O(n) repartido** → **O(1)
amortizado** por inserción. El coste real de `obtener` del índice es el de la
tabla (Θ(1+α) esperado), y `kEsimo`/rangos siguen costando Θ(h): por eso
`indice.obtener` no incrementa `arbol.visitas()`.

### 3.3 Relación con las tablas del experimento (ver §4, tablas 2 y 3)

- Tabla 2: con `m > N` y claves 1..N el módulo casi no colisiona →
  `sondas_hash_get = N` (una sonda por búsqueda, Θ(1+α) con α≈1) mientras
  `vis_ABB_get` crece superlinealmente (Θ(log n) × N búsquedas).
- Tabla 3: α = N/97 crece con N y `sondas/N` crece lineal con α, en línea con
  Θ(1+α). El contador está evidenciando el factor de carga.

---

## 4. Tablas del experimento

### Tabla 1 — Visitas del ABB (N aleatorio de semilla 2026 vs. ordenado)

| N     | h_aleat | h_ord | vis_kEsimo_aleat | vis_kEsimo_ord | vis_rango_aum | vis_rango_ing |
|------:|--------:|------:|-----------------:|---------------:|--------------:|--------------:|
| 2000  | 27      | 1999  | 14               | 1000           | 47            | 2000          |
| 4000  | 26      | 3999  | 18               | 2000           | 59            | 4000          |
| 6000  | 27      | 5999  | 21               | 3000           | 46            | 6000          |
| 8000  | 29      | 7999  | 11               | 4000           | 58            | 8000          |
| 10000 | 27      | 9999  | 16               | 5000           | 52            | 10000         |

`h_ord = N−1`, `vis_kEsimo_ord = N/2` y `vis_rango_ing = N` son deterministas:
deben repetirse idénticamente en cualquier máquina.

### Tabla 2 — Índice doble con rehash (N obtener por árbol y por índice)

| N     | vis_ABB_get | sondas_hash_get | alfa  | m     |
|------:|------------:|----------------:|------:|------:|
| 2000  | 28165       | 2000            | 0.710 | 2816  |
| 4000  | 62114       | 4000            | 0.710 | 5632  |
| 6000  | 92072       | 6000            | 0.533 | 11264 |
| 8000  | 127687      | 8000            | 0.710 | 11264 |
| 10000 | 155535      | 10000           | 0.888 | 11264 |

### Tabla 3 — Tabla sola, m = 97 fijo, sin rehash (búsquedas)

| N     | alfa    | sondas/N |
|------:|--------:|---------:|
| 2000  | 20.619  | 10.8150  |
| 4000  | 41.237  | 21.1208  |
| 6000  | 61.856  | 31.4288  |
| 8000  | 82.474  | 41.7386  |
| 10000 | 103.093 | 52.0468  |

**Lectura de las tres tablas:** la tabla 1 muestra el techo del ABB aumentado
(rangos y k-ésimo en decenas de visitas pese a que n crece 1000 veces), la tabla
2 muestra la razón del índice doble (la búsqueda por clave paga cubeta O(1+α) y
aún así `vis_ABB_get` acumula el Θ(log n) de las mismas búsquedas por el árbol)
y la tabla 3 confirma que el encadenamiento degrada linealmente con α, tal como
predice Θ(1+α).
>>>>>>> c19fa58 (TP2 ABB Aumentado + Indice Doble)
