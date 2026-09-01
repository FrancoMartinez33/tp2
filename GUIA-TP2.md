================================================================================
GUÍA DE CÓDIGO PASO A PASO - TP2 ABB Aumentado + Índice Doble (g_ts2)
================================================================================

Cómo leer esta guía: para cada archivo primero verás el CÓDIGO COMPLETO en un
bloque, y debajo la EXPLICACIÓN LÍNEA POR LÍNEA. Las palabras reservadas de Java
aparecen explicadas la primera vez que se usan (marcadas como [reservada]),
para no tener que volver a un glosario. Donde ayuda, se compara con Python.

Compilar y ejecutar (desde la carpeta):
    javac *.java
    java TestABBAumentado   # traza + visitas + tabla N aleatorio vs ordenado
    java TestIndiceDoble    # traza del índice doble + los 2 experimentos
    java TestValidacion     # suite exhaustiva contra un oráculo (oráculo = fuerza bruta)

================================================================================
ARCHIVO: ClaveInexistenteException.java
================================================================================

--------------------------------------------------------------------------------
CÓDIGO
--------------------------------------------------------------------------------
/**
 * Excepcion CHEQUEADA: se lanza cuando se busca una clave que no esta
 * en la estructura. Es chequeada porque es una situacion que el codigo
 * que llama puede prever y manejar (un cliente busca un registro que
 * puede o no existir).
 */
public class ClaveInexistenteException extends Exception {

    public ClaveInexistenteException() {
        super();
    }

    public ClaveInexistenteException(String mensaje) {
        super(mensaje);
    }
}

--------------------------------------------------------------------------------
EXPLICACIÓN LÍNEA POR LÍNEA
--------------------------------------------------------------------------------
L1-L5  Comentario de bloque Javadoc (/** ... */): documenta la clase. Es el
       lugar dónde el enunciado pide JUSTIFICAR por qué cada excepción es
       chequeada o no.
L6  public [reservada: visibilidad, accesible desde cualquier clase]
    class [reservada: declara un molde/tipo de objeto] ClaveInexistenteException
    extends [reservada: herencia, "es un subtipo de"] Exception.
    -> Extensiones de "Exception" son ERRORES CHEQUEADOS: el compilador
       OBLIGA a quien llame a obtener()/eliminar() a manejarlos (try/catch o
       throws). En Python:  class ClaveInexistenteException(Exception): pass
L8  public ClaveInexistenteException() -> constructor sin argumentos.
    super() [reservada: "llama al constructor de la clase padre"] delega en
    Exception().
L10  public ClaveInexistenteException(String mensaje) -> guarda un texto que
     después se lee con getMessage(). En Python sería self.args = (mensaje,).

================================================================================
ARCHIVO: IndiceFueraDeRangoException.java
================================================================================

--------------------------------------------------------------------------------
CÓDIGO
--------------------------------------------------------------------------------
/**
 * Excepcion NO CHEQUEADA: se lanza cuando se pide un indice invalido
 * (ej. kEsimo con k fuera de [1, size]). Representa un error de
 * programacion del que llama, no una situacion operativa previsible.
 */
public class IndiceFueraDeRangoException extends RuntimeException {

    public IndiceFueraDeRangoException() {
        super();
    }

    public IndiceFueraDeRangoException(String mensaje) {
        super(mensaje);
    }
}

--------------------------------------------------------------------------------
EXPLICACIÓN LÍNEA POR LÍNEA
--------------------------------------------------------------------------------
L6  extends RuntimeException [reservada: herencia]. RuntimeException es la
    base de los ERRORES NO CHEQUEADOS: el compilador NO obliga a capturarlos.
    El criterio del enunciado: "kEsimo(0) no lo provoca un cliente, lo provoca
    un índice mal calculado" -> es error de programación, no caso operativo.
    En Python:  class IndiceFueraDeRangoException(RuntimeError): pass

(Las clases ClaveNulaException y RangoInvalidoException son idénticas en forma:
extienden RuntimeException con los dos mismos constructores. ClaveNulaException
se lanza al recibir null, y RangoInvalidoException cuando a > b en
consultarRango. Cambia solo el comentario de justificación y el nombre.)

================================================================================
ARCHIVO: ABBAumentado.java  (el corazón del Ejercicio 1)
================================================================================

--------------------------------------------------------------------------------
CÓDIGO
--------------------------------------------------------------------------------
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ABBAumentado<K extends Comparable<? super K>, V> implements Iterable<K> {

    public static class Nodo<K, V> {
        K clave;
        V valor;
        Nodo<K, V> izq;
        Nodo<K, V> der;
        int tamano;

        public Nodo(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
            this.tamano = 1;
        }

        public K clave() { return clave; }
        public V valor() { return valor; }
        public int tamano() { return tamano; }
    }

    private Nodo<K, V> raiz;
    private long visitas;

    public ABBAumentado() {
        this.raiz = null;
        this.visitas = 0;
    }

    private void avisarVisita() { visitas++; }

    public void agregar(K clave, V valor) {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        raiz = agregarRec(raiz, clave, valor);
    }

    private Nodo<K, V> agregarRec(Nodo<K, V> n, K clave, V valor) {
        if (n == null) {
            return new Nodo<>(clave, valor);
        }
        avisarVisita();
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) {
            n.izq = agregarRec(n.izq, clave, valor);
        } else if (cmp > 0) {
            n.der = agregarRec(n.der, clave, valor);
        } else {
            n.valor = valor;
            return n;
        }
        n.tamano = 1 + tamano(n.izq) + tamano(n.der);
        return n;
    }

    public V eliminar(K clave) throws ClaveInexistenteException {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        Nodo<K, V>[] resultado = eliminarRec(raiz, clave);
        raiz = resultado[1];
        if (resultado[0] == null) {
            throw new ClaveInexistenteException("Clave no encontrada: " + clave);
        }
        return resultado[0].valor;
    }

    @SuppressWarnings("unchecked")
    private Nodo<K, V>[] eliminarRec(Nodo<K, V> n, K clave) {
        Nodo<K, V>[] res = (Nodo<K, V>[]) new Nodo[2];
        if (n == null) {
            res[0] = null;
            res[1] = null;
            return res;
        }
        avisarVisita();
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) {
            Nodo<K, V>[] sub = eliminarRec(n.izq, clave);
            n.izq = sub[1];
            res[0] = sub[0];
            res[1] = (res[0] != null) ? recalcular(n) : n;
            return res;
        } else if (cmp > 0) {
            Nodo<K, V>[] sub = eliminarRec(n.der, clave);
            n.der = sub[1];
            res[0] = sub[0];
            res[1] = (res[0] != null) ? recalcular(n) : n;
            return res;
        } else {
            res[0] = n;
            res[1] = reemplazarYNuevo(n);
            return res;
        }
    }

    private Nodo<K, V> reemplazarYNuevo(Nodo<K, V> n) {
        if (n.izq == null) { return n.der; }
        if (n.der == null) { return n.izq; }
        // Dos hijos: mudar el sucesor inorden sobre la posicion de n.
        Nodo<K, V>[] mudanza = extraerMinimo(n.der);
        Nodo<K, V> sucesor = mudanza[0];
        sucesor.izq = n.izq;
        sucesor.der = mudanza[1];
        sucesor.tamano = 1 + tamano(sucesor.izq) + tamano(sucesor.der);
        return sucesor;
    }

    @SuppressWarnings("unchecked")
    private Nodo<K, V>[] extraerMinimo(Nodo<K, V> n) {
        Nodo<K, V>[] res = (Nodo<K, V>[]) new Nodo[2];
        if (n.izq == null) {
            res[0] = n;
            res[1] = n.der;
            return res;
        }
        Nodo<K, V>[] sub = extraerMinimo(n.izq);
        n.izq = sub[1];
        n.tamano = 1 + tamano(n.izq) + tamano(n.der);
        res[0] = sub[0];
        res[1] = n;
        return res;
    }

    private Nodo<K, V> recalcular(Nodo<K, V> n) {
        n.tamano = 1 + tamano(n.izq) + tamano(n.der);
        return n;
    }

    private int tamano(Nodo<K, V> n) {
        return (n == null) ? 0 : n.tamano;
    }

    public V obtener(K clave) throws ClaveInexistenteException {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        Nodo<K, V> n = obtenerRec(raiz, clave);
        if (n == null) {
            throw new ClaveInexistenteException("Clave no encontrada: " + clave);
        }
        return n.valor;
    }

    private Nodo<K, V> obtenerRec(Nodo<K, V> n, K clave) {
        if (n == null) { return null; }
        avisarVisita();
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) { return obtenerRec(n.izq, clave); }
        else if (cmp > 0) { return obtenerRec(n.der, clave); }
        else { return n; }
    }

    public Nodo<K, V> nodoDe(K clave) {
        return buscarNodo(raiz, clave);
    }

    private Nodo<K, V> buscarNodo(Nodo<K, V> n, K clave) {
        if (n == null) { return null; }
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) { return buscarNodo(n.izq, clave); }
        else if (cmp > 0) { return buscarNodo(n.der, clave); }
        else { return n; }
    }

    public boolean contiene(K clave) {
        if (clave == null) { return false; }
        return contieneRec(raiz, clave);
    }

    private boolean contieneRec(Nodo<K, V> n, K clave) {
        if (n == null) { return false; }
        avisarVisita();
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) { return contieneRec(n.izq, clave); }
        else if (cmp > 0) { return contieneRec(n.der, clave); }
        else { return true; }
    }

    public K kEsimo(int k) {
        int n = size();
        if (k < 1 || k > n) {
            throw new IndiceFueraDeRangoException(
                    "k fuera de rango [1, " + n + "]: " + k);
        }
        return kEsimoRec(raiz, k).clave;
    }

    private Nodo<K, V> kEsimoRec(Nodo<K, V> n, int k) {
        avisarVisita();
        int tamIzq = tamano(n.izq);
        if (k == tamIzq + 1) { return n; }
        else if (k <= tamIzq) { return kEsimoRec(n.izq, k); }
        else { return kEsimoRec(n.der, k - tamIzq - 1); }
    }

    public int cuantosMenores(K clave) {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        return cuantosMenoresRec(raiz, clave);
    }

    private int cuantosMenoresRec(Nodo<K, V> n, K clave) {
        if (n == null) { return 0; }
        avisarVisita();
        int cmp = clave.compareTo(n.clave);
        if (cmp <= 0) { return cuantosMenoresRec(n.izq, clave); }
        else { return 1 + tamano(n.izq) + cuantosMenoresRec(n.der, clave); }
    }

    public int consultarRango(K a, K b) {
        if (a == null || b == null) {
            throw new ClaveNulaException("Limites de rango no pueden ser null.");
        }
        if (a.compareTo(b) > 0) {
            throw new RangoInvalidoException("a > b en consultarRango.");
        }
        return cuantosMenoresOIguales(b) - cuantosMenores(a);
    }

    private int cuantosMenoresOIguales(K clave) {
        return cuantosMenoresRec(raiz, clave) + (contieneClaveEn(clave) ? 1 : 0);
    }

    private boolean contieneClaveEn(K clave) { return estaClave(raiz, clave); }

    private boolean estaClave(Nodo<K, V> n, K clave) {
        if (n == null) { return false; }
        avisarVisita();
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) { return estaClave(n.izq, clave); }
        else if (cmp > 0) { return estaClave(n.der, clave); }
        else { return true; }
    }

    public int consultarRangoIngenuo(K a, K b) {
        if (a == null || b == null) {
            throw new ClaveNulaException("Limites de rango no pueden ser null.");
        }
        if (a.compareTo(b) > 0) {
            throw new RangoInvalidoException("a > b en consultarRango.");
        }
        int[] contador = new int[1];
        consultarRangoIngenuoRec(raiz, a, b, contador);
        return contador[0];
    }

    private void consultarRangoIngenuoRec(Nodo<K, V> n, K a, K b, int[] contador) {
        if (n == null) { return; }
        avisarVisita();
        consultarRangoIngenuoRec(n.izq, a, b, contador);
        if (n.clave.compareTo(a) >= 0 && n.clave.compareTo(b) <= 0) {
            contador[0]++;
        }
        consultarRangoIngenuoRec(n.der, a, b, contador);
    }

    public int rango(K clave) throws ClaveInexistenteException {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        if (!contiene(clave)) {
            throw new ClaveInexistenteException("Clave no encontrada: " + clave);
        }
        return cuantosMenores(clave) + 1;
    }

    public K sucesor(K clave) throws ClaveInexistenteException {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        if (!contiene(clave)) {
            throw new ClaveInexistenteException("Clave no encontrada: " + clave);
        }
        Nodo<K, V> n = buscarNodo(raiz, clave);
        if (n.der != null) { return minimo(n.der).clave; }
        return sucesorDesdeRaiz(raiz, clave);
    }

    private Nodo<K, V> minimo(Nodo<K, V> n) {
        avisarVisita();
        while (n.izq != null) {
            n = n.izq;
            avisarVisita();
        }
        return n;
    }

    private K sucesorDesdeRaiz(Nodo<K, V> n, K clave) {
        Nodo<K, V> candidato = null;
        while (n != null) {
            avisarVisita();
            int cmp = clave.compareTo(n.clave);
            if (cmp < 0) { candidato = n; n = n.izq; }
            else { n = n.der; }
        }
        return (candidato == null) ? null : candidato.clave;
    }

    public K predecesor(K clave) throws ClaveInexistenteException {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        if (!contiene(clave)) {
            throw new ClaveInexistenteException("Clave no encontrada: " + clave);
        }
        Nodo<K, V> n = buscarNodo(raiz, clave);
        if (n.izq != null) { return maximo(n.izq).clave; }
        return predecesorDesdeRaiz(raiz, clave);
    }

    private Nodo<K, V> maximo(Nodo<K, V> n) {
        avisarVisita();
        while (n.der != null) {
            n = n.der;
            avisarVisita();
        }
        return n;
    }

    private K predecesorDesdeRaiz(Nodo<K, V> n, K clave) {
        Nodo<K, V> candidato = null;
        while (n != null) {
            avisarVisita();
            int cmp = clave.compareTo(n.clave);
            if (cmp > 0) { candidato = n; n = n.der; }
            else { n = n.izq; }
        }
        return (candidato == null) ? null : candidato.clave;
    }

    public int size() { return tamano(raiz); }

    public int altura() { return alturaRec(raiz); }

    private int alturaRec(Nodo<K, V> n) {
        if (n == null) { return -1; }
        return 1 + Math.max(alturaRec(n.izq), alturaRec(n.der));
    }

    public long visitas() { return visitas; }

    public void reiniciarVisitas() { visitas = 0; }

    public boolean tamanosConsistentes() {
        return tamanosConsistentesRec(raiz);
    }

    private boolean tamanosConsistentesRec(Nodo<K, V> n) {
        if (n == null) { return true; }
        int esperado = 1 + tamano(n.izq) + tamano(n.der);
        if (n.tamano != esperado) { return false; }
        return tamanosConsistentesRec(n.izq) && tamanosConsistentesRec(n.der);
    }

    public Nodo<K, V> raiz() { return raiz; }

    @Override
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            private Nodo<K, V> siguiente = minimoInicial();

            private Nodo<K, V> minimoInicial() {
                Nodo<K, V> n = raiz;
                if (n == null) { return null; }
                while (n.izq != null) { n = n.izq; }
                return n;
            }

            @Override
            public boolean hasNext() { return siguiente != null; }

            @Override
            public K next() {
                if (siguiente == null) {
                    throw new NoSuchElementException("No hay mas claves.");
                }
                Nodo<K, V> actual = siguiente;
                siguiente = sucesorNodo(actual);
                return actual.clave;
            }

            private Nodo<K, V> sucesorNodo(Nodo<K, V> n) {
                if (n.der != null) {
                    Nodo<K, V> m = n.der;
                    while (m.izq != null) { m = m.izq; }
                    return m;
                }
                Nodo<K, V> cand = null;
                Nodo<K, V> actual = raiz;
                while (actual != null) {
                    int cmp = n.clave.compareTo(actual.clave);
                    if (cmp < 0) { cand = actual; actual = actual.izq; }
                    else { actual = actual.der; }
                }
                return cand;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toStringRec(raiz, sb);
        return sb.toString();
    }

    private void toStringRec(Nodo<K, V> n, StringBuilder sb) {
        if (n == null) { return; }
        toStringRec(n.izq, sb);
        if (sb.length() > 0) { sb.append(' '); }
        sb.append(n.clave).append('(').append(n.tamano).append(')');
        toStringRec(n.der, sb);
    }
}

--------------------------------------------------------------------------------
EXPLICACIÓN LÍNEA POR LÍNEA
--------------------------------------------------------------------------------
L1-L2  import [reservada: trae una clase de otra biblioteca] java.util.Iterator
       y java.util.NoSuchElementException. Iterator es la interfaz que permite
       recorrer elementos uno a uno (equivalente conceptual al iterador de
       Python); NoSuchElementException es el error estándar "no hay más".

L4  public class ABBAumentado<K extends Comparable<? super K>, V> implements
    Iterable<K>.
    - <K extends Comparable<? super K>> [genérico con restricción]: K debe
      saber compararse consigo mismo (tener compareTo). "? super K" dice que
      puede comparar K contra un tipo superior de K. En Python no existe: el
      orden se decide por __lt__/__gt__.
    - <V>: el valor asociado, sin restricción.
    - implements Iterable<K> = "prometo ser recorrible con for-each".

L6-L20  public static class Nodo<K, V> — el eslabón de la lista... perdón, del
    árbol. public static [reservada: pertenece a la clase, no al objeto; no
    guarda referencia al ABB] para que IndiceDoble pueda usarlo.
    Campos:
    - clave: la clave, comparable (campo de paquete, sin private).
    - valor: el dato satélite.
    - izq / der: punteros lógicos a los hijos (null = no hay hijo).
    - tamano: EL CAMPO AUMENTADO. Es la cantidad de nodos del subárbol cuyo
      "padre" es este nodo, incluyéndose a sí mismo.
    En Python pegaría:  class Nodo:
                            def __init__(self, clave, valor): ...
    Constructor L11-L16: guarda clave/valor con this y pone tamano = 1 (una
    hoja tiene "tamaño" 1).

L22-L23  Atributos del árbol: raiz (o null si vacío) y visitas (contador del
    experimento).

L28-L31  avisarVisita(): un helper privado que solo suma 1 al contador. Se
    llama UNA vez por nodo que la operación "mira" (compara clave o lee
    tamaño). Así medimos cuánto cuesta una operación sin usar el reloj.

L33-L38  agregar(clave, valor):
    - Guarda: si clave == null [reservada: null = "nada", como None en Python]
      lanza ClaveNulaException.
    - raiz = agregarRec(...): el patrón clave: la recursión DEVUELVE la raíz
      del subárbol modificado, y agregar la asigna. Nunca se "corre" el árbol.

L40-L56  agregarRec: busca dónde colgar la clave.
    - L42: si el subárbol está vacío, crea el nodo nuevo (tamano = 1) y lo
      devuelve -> caso base de la recursión.
    - L45 avisarVisita(); L46 int cmp = clave.compareTo(n.clave) [método que
      implementa K: negativo = menor, 0 = igual, positivo = mayor]. Es el
      equivalente a  if clave < n.clave: ...  en Python.
    - cmp < 0 -> baja a la izquierda; cmp > 0 -> baja a la derecha; cmp == 0 ->
      clave duplicada: SOLO se reemplaza n.valor y se devuelve n sin cambiar
      estructura ni tamaños (regla del enunciado).
    - L54 LA ACTUALIZACIÓN DEL CAMPO AUMENTADO, al volver de la recursión:
      n.tamano = 1 + tamano(izq) + tamano(der). Esto mantiene el invariante.
      Si se olvida, kEsimo va a dar claves equivocadas SIN lanzar excepción.

L58-L72  eliminar(clave) throws [reservada: "este método PUEDE lanzar esta
    excepción; el llamador debe saberlo"] ClaveInexistenteException:
    - Llama eliminarRec, que devuelve un arreglo de 2 nodos: [nodoBorrado,
      nuevaRaiz]. Se usan dos posiciones porque hay que devolver DOS cosas.
    - Si resultado[0] == null, la clave no existía -> excepción chequeada.

L74-L96  eliminarRec: devuelve [nodoBorrado, nuevaRaiz] siempre.
    - L83: res (arreglo de 2) creado por cada llamada. L89: al bajar a la
      izquierda, reenlaza n.izq con la nueva raíz que viene de abajo.
    - L91/L97: res[1] = (res[0] != null) ? recalcular(n) : n — el ternario:
      si la clave se encontró más abajo, hay que RECALCULAR el tamaño de n
      mientras se sube; si no se encontró, n no cambió y se devuelve igual.
      (recalcular L186-L189: reaplica n.tamano = 1 + tamano(izq) + tamano(der).)
    - L99-L101: cuando clave == n.clave, res[0] = n (el eliminado) y
      res[1] = reemplazarYNuevo(n).

L103-L123  reemplazarYNuevo: el caso delicado del borrado.
    - 0 o 1 hijo: devuelve el hijo (o null) — el nodo muere y su único hijo
      "sube" a su puesto.
    - DOS hijos: hay que sacar al sucesor inorden del subárbol derecho
      (el mínimo de la derecha) y "mudarlo" aquí:
      extraerMinimo(n.der) devuelve [sucesor, raízDerechaSinMinimo].
      Entonces sucesor.izq = n.izq, sucesor.der = mudanza[1].
      CLAVE: el sucesor es EL MISMO OBJETO Nodo, no una copia de su clave.
      El enunciado lo exige porque la tabla de dispersión (Ejercicio 2)
      guarda referencias a nodos; si copiáramos la clave 35 sobre el nodo 30,
      la referencia externa apuntaría a un nodo con clave equivocada.

L125-L143  extraerMinimo: desengancha el mínimo del subárbol.
    - L128: si no hay hijo izquierdo, este nodo ES el mínimo; la nueva raíz de
      ese subárbol es n.der (se mueve el nodo completo, no su clave).
    - L135-L138: si hay izquierdo, baja, reenlaza n.izq con lo que vuelva y
      recalcula n.tamano en cada nivel (mantiene el invariante).

L145-L151  tamano(n): de null devuelve 0 (un hijo ausente "tiene 0 nodos").
    Es el único punto que lee el tamaño de un subárbol: evitar duplicar esa
    lógica con ternarios sueltos.

L153-L165  obtener: baja por el árbol comparando (cuenta visitas) y devuelve
    el valor, o lanza ClaveInexistenteException si no lo encuentra. Comparado
    con Python: el mismo while/recursión de búsqueda binaria de siempre.

L167-L179  nodoDe / buscarNodo: igual que obtener pero devuelve el NODO (no el
    valor) y NO cuenta visitas. Lo usa IndiceDoble para registrar la referencia
    en la tabla. Sin count sería una "búsqueda de mantenimiento" que no debe
    estropear el instrumento de medición.

L181-L198  contiene: ¿está la clave? cuenta visitas (es operación medible).
    contieneRec devuelve boolean, comparando en cada nivel.

L200-L216  kEsimo(int k): EL MÉTODO ESTRELLA.
    - Valida 1 <= k <= n, si no, IndiceFueraDeRangoException.
    - kEsimoRec aplica la idea del campo aumentado:
      L= tamano(n.izq) = cuántas claves menores que n. hay.
        - k == L+1  -> n es la respuesta.
        - k <= L    -> hay que bajar a la izquierda con el mismo k.
        - k > L+1   -> bajar a la derecha con k - L - 1.
      En el árbol de la traza, kEsimo(6): 50 tiene tam izquierdo 4, 6 > 5,
      se baja a la derecha con k=1; en 70, tam izquierdo 2, 1 <= 2, se baja a
      la izquierda; en 60, k=1 = L+1 -> respuesta 60. Solo 3 visitas.
      NUNCA recorre en inorden: recorre un camino, O(h).

L218-L238  cuantosMenores(clave): cuenta estrictamente menores que clave a lo
    largo de un camino:
      - cmp <= 0 (la clave viene antes o es la misma): nada de n ni de su
        derecha es menor -> bajar a la izquierda.
      - cmp > 0: n y todo su subárbol izquierdo son menores ->
        sumar 1 + tamano(n.izq) y bajar a la derecha.
    En el árbol de traza, cuantosMenores(65): 50 menor -> +1+4=5, luego 70
    no, 60 sí -> +2, 65 no -> total 6. Son 4 visitas.

L240-L260  consultarRango(a, b): la cuenta del intervalo cerrado
    [a, b] = (menoresOIguales que b) - (menores que a). Dos caminos, O(h).
    cuantosMenoresOIguales = cuantosMenoresRec(b) y +1 si b está en el árbol
    (estaClave b). Nada de cortar un inorden al pasar b: eso sería Theta(n).

L262-L286  consultarRangoIngenuo: la misma cuenta pero recorriendo TODOS los
    nodos en inorden y contando los que caen en el rango. Es la "columna de
    control" del experimento: siempre Theta(n) visitas por construcción.
    Observá cómo pasa un contador dentro de un arreglo int[1]: en Java los
    parámetros se copian, así que si pasaras un int no podrías acumularlo;
    pasando el arreglo sí modificás su única celda. (Python no tiene este
    problema: un int "se pasa por referencia mutable"... en realidad no, pero
    las listas sí se comportan así.)

L288-L316  rango(clave): posición 1-based en el inorden = cuantosMenores + 1.
    Primero verifica que la clave exista (contiene) y lanza si no.

L318-L356  sucesor / predecesor: el siguiente (o anterior) en inorden.
    - Si la clave tiene hijo derecho (izquierdo), el sucesor es el mínimo
      (máximo) de ese subárbol.
    - Si no lo tiene, se baja desde la raíz guardando el último nodo desde el
      que se bajó a la izquierda (derecha): O(h) tiempo, O(1) espacio extra,
      sin puntero al padre.
    sucesor(max) = null y predecesor(min) = null.

L358-L366  size(): lee raiz.tamano (0 si null). NO hay un campo size aparte:
    el tamaño se lee del nodo raíz (una sola fuente de verdad).
    altura(): -1 vacío, 0 hoja; 1 + max(altura(izq), altura(der)).

L368-L383  visitas()/reiniciarVisitas(): el instrumento de medición del
    experimento. reiniciarVisitas() se llama ANTES de cada operación a medir.

L385-L424  tamanosConsistentes(): recorre TODO el árbol verificando que en cada
    nodo se cumpla tamano == 1 + tamano(izq) + tamano(der). Es un método de
    DEPURACIÓN: se usa solo en las pruebas (recorre O(n), por eso nunca se
    llama dentro de agregar/kEsimo/consultarRango).

L426-L436  iterator(): devuelve un Iterator anónimo (new Iterator<K>(){...})
    que recorre en inorden "sin pila":
    - minimoInicial(): baja todo a la izquierda desde la raíz.
    - next(): devuelve la clave actual y calcula el sucesor. Si el nodo actual
      tiene hijo derecho, baja a la izquierda de la derecha; si no, sube
      guardando candidatos (el último nodo donde se fue a la izquierda).
    - hasNext(): ¿quedan claves? Devuelve true si siguiente != null.
    Esto es lo que habilita:  for (Integer k : arbol) { ... }
    En Python: implementar __iter__/__next__ o un generador.

L438-L455  toString(): inorden con tamaño, formato exacto
    "20(1) 30(4) 35(1) ...". StringBuilder [clase eficiente para concatenar;
    como "".join(lista) en Python] evita crear un String nuevo en cada +=.
    Si el nodo no es el primero, agrega un espacio; luego clave + "(" + tamaño
    + ")". La salida coincide carácter a carácter con la tabla del enunciado.

================================================================================
ARCHIVO: TablaEncadenada.java  (Ejercicio 2 - la tabla de dispersión)
================================================================================

--------------------------------------------------------------------------------
CÓDIGO
--------------------------------------------------------------------------------
public class TablaEncadenada<K, E> {

    private static class NodoLista<K, E> {
        K clave;
        E dato;
        NodoLista<K, E> siguiente;

        NodoLista(K clave, E dato, NodoLista<K, E> siguiente) {
            this.clave = clave;
            this.dato = dato;
            this.siguiente = siguiente;
        }
    }

    private static final int TAM_INICIAL = 11;

    private NodoLista<K, E>[] cubetas;
    private int n;
    private double alfaMax;
    private long sondas;

    public TablaEncadenada() {
        this(TAM_INICIAL, 1.0);
    }

    @SuppressWarnings("unchecked")
    public TablaEncadenada(int m, double alfaMax) {
        if (m <= 0) {
            throw new IllegalArgumentException("m debe ser positivo.");
        }
        this.cubetas = (NodoLista<K, E>[]) new NodoLista[m];
        this.n = 0;
        this.alfaMax = alfaMax;
        this.sondas = 0;
    }

    private int indice(K clave) {
        return (clave.hashCode() & 0x7fffffff) % cubetas.length;
    }

    public void insertar(K clave, E dato) {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        int i = indice(clave);
        NodoLista<K, E> actual = cubetas[i];
        while (actual != null) {
            sondas++;
            if (actual.clave.equals(clave)) {
                actual.dato = dato;
                return;
            }
            actual = actual.siguiente;
        }
        // No existe: insertar al frente de la cubeta.
        cubetas[i] = new NodoLista<>(clave, dato, cubetas[i]);
        n++;
        if (n > cubetas.length && alfaMax != Double.POSITIVE_INFINITY) {
            rehash();
        }
    }

    public E obtener(K clave) {
        if (clave == null) { return null; }
        int i = indice(clave);
        NodoLista<K, E> actual = cubetas[i];
        while (actual != null) {
            sondas++;
            if (actual.clave.equals(clave)) {
                return actual.dato;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    public E eliminar(K clave) {
        if (clave == null) { return null; }
        int i = indice(clave);
        NodoLista<K, E> actual = cubetas[i];
        NodoLista<K, E> previo = null;
        while (actual != null) {
            sondas++;
            if (actual.clave.equals(clave)) {
                if (previo == null) {
                    cubetas[i] = actual.siguiente;
                } else {
                    previo.siguiente = actual.siguiente;
                }
                n--;
                return actual.dato;
            }
            previo = actual;
            actual = actual.siguiente;
        }
        return null;
    }

    public int capacidad() { return cubetas.length; }
    public int size() { return n; }

    public double factorCarga() {
        return (double) n / cubetas.length;
    }

    public long sondas() { return sondas; }
    public void reiniciarSondas() { sondas = 0; }

    @SuppressWarnings("unchecked")
    private void rehash() {
        int mNuevo = cubetas.length * 2;
        NodoLista<K, E>[] nuevas = (NodoLista<K, E>[]) new NodoLista[mNuevo];
        for (int i = 0; i < cubetas.length; i++) {
            NodoLista<K, E> actual = cubetas[i];
            while (actual != null) {
                NodoLista<K, E> sig = actual.siguiente;
                int j = (actual.clave.hashCode() & 0x7fffffff) % mNuevo;
                actual.siguiente = nuevas[j];
                nuevas[j] = actual;
                actual = sig;
            }
        }
        cubetas = nuevas;
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cubetas.length; i++) {
            sb.append('[').append(i).append("]");
            NodoLista<K, E> actual = cubetas[i];
            if (actual == null) {
                sb.append(" (vacia)").append('\n');
                continue;
            }
            while (actual != null) {
                sb.append(' ').append(actual.clave);
                actual = actual.siguiente;
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public String dumpSoloOcupadas() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cubetas.length; i++) {
            NodoLista<K, E> actual = cubetas[i];
            if (actual == null) { continue; }
            sb.append('[').append(i).append("]");
            while (actual != null) {
                sb.append(' ').append(actual.clave);
                actual = actual.siguiente;
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}

--------------------------------------------------------------------------------
EXPLICACIÓN LÍNEA POR LÍNEA
--------------------------------------------------------------------------------
L1  public class TablaEncadenada<K, E> -> tabla genérica. E es el dato; en el
    índice doble E = ABBAumentado.Nodo<K,V> (una referencia al nodo del árbol).

L3-L16  private static class NodoLista<K, E> -> el eslabón del encadenamiento
    (lista enlazada PROPIA, prohibido java.util.LinkedList). Cada nodo guarda
    la clave, el dato, y el puntero siguiente al próximo de la cubeta.
    private static porque es un detalle interno; en Python sería otra clase o
    un simple "dict tail".

L18  private static final int TAM_INICIAL = 11 -> 11 cubetas al arrancar. Es
    primo (como en las pruebas de escritorio de la cátedra).

L20-L24  Atributos: cubetas (un arreglo de listas enlazadas: cada celda es la
    CABEZA de una lista), n (cantidad de claves), alfaMax (factor de carga
    máximo permitido), sondas (contador análogo a visitas del árbol).

L26-L28  Constructor por defecto: delega en el otro con m=11, alfaMax=1.

L30-L40  Constructor configurable (para el experimento con m=97 y sin rehash):
    - (NodoLista<K,E>[]) new NodoLista[m] crea el arreglo de cabezeras.
      El casteo es necesario porque Java no deja crear arreglos genéricos a
      secas; @SuppressWarnings("unchecked") le dice al compilador "tranqui,
      sé lo que estoy haciendo".
    - Con alfaMax = Double.POSITIVE_INFINITY no habrá rehash nunca (así corre
      el experimento de m fijo).

L42-L44  indice(clave): LA FUNCIÓN DE DISPERSIÓN (compresión):
    h(k) = (k.hashCode() & 0x7fffffff) % m.
    - hashCode() [método que todo objeto tiene] devuelve un int.
    - & 0x7fffffff: borra el bit de signo -> cualquier int negativo (incluido
      Integer.MIN_VALUE = -2147483648) queda positivo. En Python no hace falta:
      el % ya arroja positivo.
    - % m: comprime al rango de cubetas 0..m-1.
    Para Integer, hashCode() es el propio número, así que cubeta = k % 11.

L46-L72  insertar:
    - Validación de null.
    - Recorre la cubeta i. L70: cada nodo de cadena visitado suma 1 a sondas
      (el instrumento del experimento 2).
    - Si la clave ya existe (equals [comparación de contenido, "==" en
      Python para strings]), SOLO actualiza el dato.
    - Si no existe, inserta AL FRENTE (O(1)): cubetas[i] = new NodoLista(...)
      cuyo siguiente es la cabeza vieja.
    - n++ y LA REGLA DE REHASH: si n > m (alfa supera alfaMax = 1) se duplica.
      Ojo: la condición es n > cubetas.length, equivalente a alfa = n/m > 1.

L74-L91  obtener: misma caminata por la cubeta, contando sondas. Devuelve null
    si no está (el índice doble usa ese null para decidir lanzar).

L93-L118  eliminar: camina la cubeta llevando un previo. Al encontrar la clave,
    desengancha: si era la cabeza, la nueva cabeza es siguiente; si no,
    previo.siguiente = actual.siguiente. Decrementa n y devuelve el dato.
    Eliminar en lista enlazada = reenlazar 2 punteros, O(longitud cubeta).

L120-L132  capacidad() (m), size() (n) y factorCarga() (alfa = n/m, calculado
    en double para que la división no trunque: (double) n / cubetas.length).

L134-L143  sondas()/reiniciarSondas(): como visitas() del árbol: el contador de
    nodos de cadena visitados en obtener/insertar/eliminar.

L145-L162  rehash(): duplica m y reubica TODAS las claves.
    - Crea un arreglo nuevo del doble.
    - Por cada cubeta y cada nodo de su cadena, calcula la nueva cubeta con la
      nueva m y reinserta al frente de ella. Reutiliza los mismos objetos
      NodoLista (cambia solo de dónde cuelgan).
    - Sustituye cubetas por las nuevas. Costo O(n) por llamada; como m se
      duplica siempre, el total al insertar n claves desde vacío es
      n + n/2 + n/4 + ... < 2n: amortizado O(1) por inserción.

L164-L201  dump(): una línea "[i] clave1 clave2 ..." por cubeta (para la traza);
    dumpSoloOcupadas(): solo las cubetas con datos (más legible en el informe).

================================================================================
ARCHIVO: IndiceDoble.java  (Juan Pérez: compone árbol + tabla, Ejercicio 2)
================================================================================

--------------------------------------------------------------------------------
CÓDIGO
--------------------------------------------------------------------------------
public class IndiceDoble<K extends Comparable<? super K>, V> {

    private final ABBAumentado<K, V> arbol;
    private final TablaEncadenada<K, ABBAumentado.Nodo<K, V>> tabla;

    public IndiceDoble() {
        this.arbol = new ABBAumentado<>();
        this.tabla = new TablaEncadenada<>();
    }

    public IndiceDoble(int m, double alfaMax) {
        this.arbol = new ABBAumentado<>();
        this.tabla = new TablaEncadenada<>(m, alfaMax);
    }

    public void agregar(K clave, V valor) {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        arbol.agregar(clave, valor);
        tabla.insertar(clave, arbol.nodoDe(clave));
    }

    public V obtener(K clave) throws ClaveInexistenteException {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        ABBAumentado.Nodo<K, V> nodo = tabla.obtener(clave);
        if (nodo == null) {
            throw new ClaveInexistenteException("Clave no encontrada: " + clave);
        }
        return nodo.valor();
    }

    public ABBAumentado.Nodo<K, V> obtenerNodo(K clave) {
        if (clave == null) { return null; }
        return tabla.obtener(clave);
    }

    public V eliminar(K clave) throws ClaveInexistenteException {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        V valor = arbol.eliminar(clave);
        tabla.eliminar(clave);
        return valor;
    }

    public K kEsimo(int k) { return arbol.kEsimo(k); }
    public int consultarRango(K a, K b) { return arbol.consultarRango(a, b); }
    public boolean contiene(K clave) { return tabla.obtener(clave) != null; }
    public int size() { return arbol.size(); }

    public ABBAumentado<K, V> arbol() { return arbol; }
    public TablaEncadenada<K, ABBAumentado.Nodo<K, V>> tabla() { return tabla; }
}

--------------------------------------------------------------------------------
EXPLICACIÓN LÍNEA POR LÍNEA
--------------------------------------------------------------------------------
L1  public class IndiceDoble<K extends Comparable<? super K>, V> -> mismo
    genérico con restricción que el árbol (la clave tiene que ser comparable).

L3-L4  La COMPOSICIÓN (no herencia): el índice TIENE un árbol y TIENE una
    tabla. La tabla no guarda valores: guarda referencias a los nodos del
    árbol (TablaEncadenada<K, ABBAumentado.Nodo<K,V>>). Por eso la clave del
    nodo se conserva aunque el nodo se mude al eliminar un nodo con dos hijos.

L6-L12  Constructores: por defecto (m=11, alfa=1) y configurable (experimento).

L14-L23  agregar:
    1) arbol.agregar(clave, valor): inserta en el árbol (dueño de nodos).
    2) tabla.insertar(clave, arbol.nodoDe(clave)): registra en la tabla la
       REFERENCIA al nodo recién insertado (o actualizado) en el árbol.
       Si en el futuro el nodo se mueve de lugar (eliminación de un nodo con
       dos hijos), la referencia del nodo en la tabla SIGUE apuntando al mismo
       objeto, que conserva su clave.

L25-L37  obtener: LA REGLA DEL EJERCICIO. Nada de buscar en el árbol:
    se camina la cubeta (tabla.obtener), si no hay nodo se lanza
    ClaveInexistenteException, y se devuelve nodo.valor().
    Por eso arbol.visitas() queda en 0. (Verificar: en Fase C la salida dice
    arbol.visitas=0; arbol.obtener(70) por el mismo índice da 2 visitas.)

L39-L44  obtenerNodo: variante auxiliar de pruebas; devuelve la referencia o
    null.

L46-L55  eliminar: toca las DOS estructuras. Primero el árbol (que además
    valida la existencia con su excepción chequeada); después la tabla, para
    que no quede una referencia colgante. Retorna el valor eliminado.
    Si solo borrás de una estructura, "el ABB sigue respondiendo kEsimo" pero
    la tabla queda apuntando a un nodo suelto, o al revés. La Fase D prueba esto.

L57-L60  kEsimo, consultarRango: DELEGAN en el árbol. La tabla no ordena: el
    árbol es el único que sabe responder "k-ésimo menor" y "cuántos hay en
    [a,b]". Es el trade-off: árbol para ordenar/rangos, tabla para acceso O(1)
    promedio por clave.

L61  contiene: usa la tabla (rápido y sin visitar el árbol).
L62  size(): reporta arbol.size() (la fuente de verdad del tamaño).

L64-L66  arbol() / tabla(): expone las estructuras internas para que los Test
    puedan medir visitas, sondas, dump, etc. (Público solo a efectos del
    experimento del TP.)

================================================================================
ARCHIVO: TestABBAumentado.java  (pruebas del Ejercicio 1)
================================================================================

--------------------------------------------------------------------------------
CÓDIGO (solo los métodos principales)
--------------------------------------------------------------------------------
import java.util.Random;

public class TestABBAumentado {

    public static void main(String[] args) {
        ABBAumentado<Integer, String> arbol = arbolTraza();
        ejecutarFaseB(arbol);
        ejecutarFaseC(arbol);
        verificarForEach();
        tablaDeVisitas();
    }

    private static ABBAumentado<Integer, String> arbolTraza() {
        ABBAumentado<Integer, String> arbol = new ABBAumentado<>();
        int[] claves = {50, 30, 70, 20, 40, 60, 80, 35, 65};
        for (int k : claves) {
            arbol.agregar(k, "P" + k);
            System.out.printf("%-14s -> %-48s h=%-2d size=%-2d consistentes=%s%n",
                    "agregar(" + k + ")", arbol, arbol.altura(),
                    arbol.size(), arbol.tamanosConsistentes());
        }
        return arbol;
    }

    private static void ejecutarFaseB(ABBAumentado<Integer, String> arbol) {
        arbol.reiniciarVisitas();
        int r = arbol.kEsimo(6);
        System.out.println("kEsimo(6) -> " + r + " | visitas=" + arbol.visitas()
                + " (esperado: 3)");

        arbol.reiniciarVisitas();
        r = arbol.cuantosMenores(65);
        System.out.println("cuantosMenores(65) -> " + r + " | visitas=" + arbol.visitas()
                + " (esperado: 4)");

        arbol.reiniciarVisitas();
        r = arbol.consultarRango(35, 65);
        System.out.println("consultarRango(35, 65) -> " + r + " | visitas="
                + arbol.visitas() + " (O(h)");

        arbol.reiniciarVisitas();
        r = arbol.consultarRangoIngenuo(35, 65);
        System.out.println("consultarRangoIngenuo(35, 65) -> " + r + " | visitas="
                + arbol.visitas() + " (esperado: 9)");
    }

    private static void tablaDeVisitas() {
        int[] tamanos = {2000, 4000, 6000, 8000, 10000};
        System.out.printf("%-6s %-8s %-8s %-12s %-12s %-12s %-12s%n",
                "N", "h_aleat", "h_ord", "vis_kEsimo_aleat", "vis_kEsimo_ord",
                "vis_rango_aum", "vis_rango_ing");
        for (int n : tamanos) {
            int[] perm = permutacionAleatoria(n);
            int[] ordenado = secuencia(n);

            ABBAumentado<Integer, String> aleat = new ABBAumentado<>();
            for (int v : perm) { aleat.agregar(v, "P" + v); }
            ABBAumentado<Integer, String> ord = new ABBAumentado<>();
            for (int v : ordenado) { ord.agregar(v, "P" + v); }

            int hA = aleat.altura();
            int hO = ord.altura();

            aleat.reiniciarVisitas();
            aleat.kEsimo(n / 2);
            long visA = aleat.visitas();

            ord.reiniciarVisitas();
            ord.kEsimo(n / 2);
            long visO = ord.visitas();

            Integer a = aleat.kEsimo(n / 4);
            Integer b = aleat.kEsimo(3 * n / 4);
            aleat.reiniciarVisitas();
            aleat.consultarRango(a, b);
            long visRangoAum = aleat.visitas();

            aleat.reiniciarVisitas();
            aleat.consultarRangoIngenuo(a, b);
            long visRangoIng = aleat.visitas();

            System.out.printf("%-6d %-8d %-8d %-12d %-12d %-12d %-12d%n",
                    n, hA, hO, visA, visO, visRangoAum, visRangoIng);
        }
    }

    private static int[] secuencia(int n) { ... 1..n ... }

    private static int[] permutacionAleatoria(int n) {
        Random rng = new Random(2026);
        int[] a = secuencia(n);
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
        }
        return a;
    }
}

--------------------------------------------------------------------------------
EXPLICACIÓN LÍNEA POR LÍNEA
--------------------------------------------------------------------------------
L1  import java.util.Random -> números pseudoaleatorios (para Fisher-Yates).
    (Son de las pocas clases de java.util permitidas en este TP.)

L6-L12  main: la secuencia del ejercicio. Reutiliza UN MISMO árbol para Fase B
    y Fase C (así el eliminar actúa sobre el árbol ya consultado).

L14-L22  arbolTraza(): inserta las 9 claves EN EL ORDEN del enunciado
    (50, 30, 70, 20, 40, 60, 80, 35, 65) con valores "P"+clave. Después de
    cada agregar imprime el toString() — que tiene que coincidir carácter por
    carácter con la tabla de la consigna — la altura y el invariante.
    printf [formato tipo C, como f-string de Python]: %-14s alinea a la
    izquierda en 14 columnas, %d es entero, %n salto de línea.

L25-L47  ejecutarFaseB():
    - ANTES de cada consulta a medir: arbol.reiniciarVisitas().
    - kEsimo(6) -> 60 con 3 visitas (el valor del enunciado).
    - cuantosMenores(65) -> 6 con 4 visitas.
    - consultarRango(35,65) -> 5 (O(h)).
    - consultarRangoIngenuo(35,65) -> 5 con 9 visitas (= n: recorre todos).

L52-L80  tablaDeVisitas(): el experimento. Para cada N arma:
    - aleat: ABB con la permutación de semilla 2026.
    - ord: ABB con 1..N en orden (degenerado, altura N-1).
    Mide kEsimo(N/2) en ambos y consultarRango(kEsimo(N/4), kEsimo(3N/4)) en el
    aleatorio, por el método aumentado y por el ingenuo, reiniciando el
    contador entre mediciones.
    Resultado esperado (se ve en la salida):
    - h_ord = N-1 y vis_kEsimo_ord = N/2: el k-ésimo cae a la mitad, y en una
      lista se baja exactamente hasta esa posición.
    - vis_rango_ing = N: el ingenuo recorre TODO el árbol.
    - vis_rango_aum en las decenas: el aumentado baja 2 caminos de altura ~log
      N (en el árbol chico de la traza puede visitar más que el ingenuo porque
      recorre 2-3 caminos y n es ridículamente pequeño; por eso se usa N grande
      para ver el pago en la columna vis_rango_ing).

L83-L92  permutacionAleatoria: Fisher-Yates con new Random(2026). La MISMA
    semilla en la de todos => la corrida es reproducible. La columna aleatoria
    se evalúa por tendencia (~log N de altura), no por igualdad exacta.

================================================================================
ARCHIVO: TestIndiceDoble.java  (pruebas del Ejercicio 2)
================================================================================

--------------------------------------------------------------------------------
CÓDIGO (solo los métodos principales)
--------------------------------------------------------------------------------
public class TestIndiceDoble {

    public static void main(String[] args) throws Exception {
        ejecutarTraza();
        experimentoIndiceDoble();
        experimentoTablaFija97();
    }

    private static void ejecutarTraza() throws ClaveInexistenteException {
        // Fase A: insertar las 9 claves (50 30 70 20 40 60 80 35 65).
        IndiceDoble<Integer, String> indice = indiceTraza();
        System.out.println(indice.tabla().dumpSoloOcupadas());
        // Verifica cubeta real == esperada para ([2]35, [3]80, ..., [10]65).

        // Fase B: agregar(61) y agregar(41)  (chocan en cubetas 6 y 8).
        indice.agregar(61, "P61");
        indice.agregar(41, "P41");
        System.out.println(indice.tabla().dumpSoloOcupadas());
        System.out.println("arbol.toString=" + indice.arbol());

        // Fase C: medir el par que define el ejercicio.
        indice.arbol().reiniciarVisitas();
        indice.tabla().reiniciarSondas();
        String v70 = indice.obtener(70);
        System.out.println("indice.obtener(70) -> " + v70
                + " | arbol.visitas=" + indice.arbol().visitas()
                + " (esperado 0) | tabla.sondas=" + indice.tabla().sondas());

        indice.arbol().reiniciarVisitas();
        String v70Arbol = indice.arbol().obtener(70);
        System.out.println("arbol.obtener(70) -> " + v70Arbol
                + " | visitas=" + indice.arbol().visitas() + " (esperado 2)");

        // Fase D: eliminar de ambas estructuras.
        System.out.println("indice.eliminar(30) -> " + indice.eliminar(30));
        System.out.println("toString=" + indice.arbol());
        System.out.println(indice.tabla().dumpSoloOcupadas());
        System.out.println("contiene(30)=" + indice.contiene(30)
                + " | kEsimo(2)=" + indice.kEsimo(2)
                + " | obtener(35)=" + indice.obtener(35)
                + " | size=" + indice.size());
    }

    private static void experimentoIndiceDoble() throws ClaveInexistenteException {
        int[] tamanos = {2000, 4000, 6000, 8000, 10000};
        for (int n : tamanos) {
            int[] perm = permutacion(n);
            IndiceDoble<Integer, String> indice = new IndiceDoble<>();
            for (int v : perm) { indice.agregar(v, "P" + v); }
            indice.arbol().reiniciarVisitas();
            indice.tabla().reiniciarSondas();
            for (int v : perm) {
                indice.arbol().obtener(v);   // por el árbol
                indice.obtener(v);           // por el índice (tabla)
            }
            System.out.printf("%d %d %d %.3f %d%n",
                    n, indice.arbol().visitas(), indice.tabla().sondas(),
                    indice.tabla().factorCarga(), indice.tabla().capacidad());
        }
    }

    private static void experimentoTablaFija97() {
        int[] tamanos = {2000, 4000, 6000, 8000, 10000};
        for (int n : tamanos) {
            TablaEncadenada<Integer, Integer> tabla =
                    new TablaEncadenada<>(97, Double.POSITIVE_INFINITY);
            for (int i = 1; i <= n; i++) { tabla.insertar(i, i); }
            tabla.reiniciarSondas();
            for (int i = 1; i <= n; i++) { tabla.obtener(i); }
            System.out.printf("%d %.3f %.4f%n",
                    n, tabla.factorCarga(), (double) tabla.sondas() / n);
        }
    }
}

--------------------------------------------------------------------------------
EXPLICACIÓN LÍNEA POR LÍNEA
--------------------------------------------------------------------------------
L18-L20  Fase C — la pareja de filas que "delata" un índice doble cosmético:
    - indice.obtener(70) resuelve SOLO por la tabla: arbol.visitas() queda en
      0 y tabla.sondas() en 1 (70 está al frente de su cubeta [4]).
    - arbol.obtener(70) sobre el mismo índice: 2 visitas (camino 50 -> 70).
    Si tu implementación de obtener buscara en el árbol, el primer contador
    se movería y la defensa lo notaría.

L29-L34  experimentoIndiceDoble(): sobre el mismo contenido, hace N obtener por
    el árbol (compara claves, sube su contador) y N por el índice (camina una
    cubeta, sube SONDAS, no el contador del árbol). Con m > N y claves 1..N el
    módulo casi no colisiona: la salida muestra sondas_hash_get == N y
    vis_ABB_get creciendo más rápido que lineal. No es bug: es un hash casi
    perfecto sobre un universo chico, y es la razón de ser del índice doble.

L42-L50  experimentoTablaFija97(): TablaEncadenada con m=97 y alfaMax =
    Infinity (sin rehash). Insertar/buscar 1..N. La salida muestra
    sondas/N creciendo lineal con alfa = N/97 (el encadenamiento esperado es
    Theta(1 + alfa)). Si la columna te quedara constante, el contador de
    sondas no está contando.

================================================================================
RESUMEN DE CONCEPTOS CLAVE
================================================================================
- ABB aumentado: cada nodo guarda tamano = 1 + tamano(izq) + tamano(der).
  Ese único campo convierte búsqueda posicional (kEsimo) y conteo de rango en
  un camino de O(h) en vez de un inorden O(n).
- El contador de visitas es un instrumento: cada operación que "mira" un nodo
  suma 1. toString/iterator/tamanosConsistentes NO lo tocan. Es la evidencia
  objetiva de que kEsimo no recorre el árbol.
- Eliminar un nodo con dos hijos MUDE el sucesor (reenlaza punteros), no copia
  claves. Si copiara, cualquier referencia externa al Nodo quedaría con la
  clave equivocada — y el Ejercicio 2 se rompe.
- Tabla con encadenamiento: cubetas = arreglo de listas propias. h(k) =
  (hashCode & 0x7fffffff) % m. Al superar alfa max, se duplica m y se reubica
  todo (rehash, O(n) repartido entre todas las inserciones).
- Índice doble: el ABB ordena (kEsimo, rangos), la tabla apunta a los MISMOS
  nodos (obtener O(1) promedio con 0 visitas del ABB). eliminar toca TODAS las
  estructuras. Esta es la idea del trade-off: orden y rangos vs acceso directo.
- Excepciones: chequeada (previsible, la lanzan eliminar/obtener/sucesor/
  predecesor/rango) vs no chequeadas (errores de programación: kEsimo(0),
  null, a > b).