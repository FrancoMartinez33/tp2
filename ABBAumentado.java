import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Arbol Binario de Busqueda aumentado.
 *
 * Cada nodo guarda, ademas de clave y valor, el tamano de su subarbol
 * (cantidad de nodos, incluido el propio). Con ese campo aumentado,
 * el k-esimo menor y el conteo de un rango se responden bajando por un
 * unico camino en O(h): no hace falta recorrer el arbol en inorden.
 *
 * Invariante de tamano (debe cumplirse despues de cualquier operacion):
 *     n.tamano = 1 + tamano(n.izq) + tamano(n.der)
 *     arbol.size() = tamano(raiz)   (0 si arbol vacio)
 *
 * UNA REGLA IMPORTANTE EN ELIMINAR: cuando se borra un nodo con dos hijos,
 * el sucesor inorden se MUEVE (se reenlazan sus punteros) a la posicion
 * del eliminado. NO se copia la clave de un nodo a otro, porque una
 * referencia externa a ese objeto Nodo seguiria valida (lo usa IndiceDoble).
 *
 * @param <K> tipo de la clave, comparable
 * @param <V> tipo del valor asociado
 */
public class ABBAumentado<K extends Comparable<? super K>, V> implements Iterable<K> {

    /**
     * Nodo del ABB aumentado. Cada Nodo conserva su clave desde que se
     * inserta hasta que se elimina.
     */
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

        public K clave() {
            return clave;
        }

        public V valor() {
            return valor;
        }

        public int tamano() {
            return tamano;
        }
    }

    private Nodo<K, V> raiz;
    private long visitas;

    /** Arbol vacio. */
    public ABBAumentado() {
        this.raiz = null;
        this.visitas = 0;
    }

    private void avisarVisita() {
        visitas++;
    }

    /**
     * Inserta el par (clave, valor). Si la clave ya existia, reemplaza el
     * valor sin cambiar la estructura ni los tamanos.
     */
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

    /**
     * Elimina la clave y retorna su valor.
     */
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

    /**
     * Devuelve [nodoEliminado, nuevaRaiz]. Si la clave no existe,
     * nodoEliminado es null y nuevaRaiz es la raiz sin cambios.
     */
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

    /**
     * Dado el nodo a eliminar (que coincide con la clave), devuelve la raiz
     * del subarbol que lo reemplaza.
     */
    private Nodo<K, V> reemplazarYNuevo(Nodo<K, V> n) {
        if (n.izq == null) {
            return n.der;
        }
        if (n.der == null) {
            return n.izq;
        }
        // Dos hijos: mudar el sucesor inorden sobre la posicion de n.
        Nodo<K, V>[] mudanza = extraerMinimo(n.der);
        Nodo<K, V> sucesor = mudanza[0];
        sucesor.izq = n.izq;
        sucesor.der = mudanza[1];
        sucesor.tamano = 1 + tamano(sucesor.izq) + tamano(sucesor.der);
        return sucesor;
    }

    /**
     * Extrae el minimo del subarbol cuya raiz se pasa (no debe ser null).
     * Devuelve [minimo, nuevaRaizSinMinimo]. No copia claves.
     */
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

    /**
     * Retorna el valor asociado a la clave.
     */
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
        if (n == null) {
            return null;
        }
        avisarVisita();
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) {
            return obtenerRec(n.izq, clave);
        } else if (cmp > 0) {
            return obtenerRec(n.der, clave);
        } else {
            return n;
        }
    }

    /**
     * Devuelve el valor asociado a la clave, o null si no existe
     * (sin lanzar excepcion). No cuenta como operacion medible del TAD.
     */
    public Nodo<K, V> nodoDe(K clave) {
        return buscarNodo(raiz, clave);
    }

    private Nodo<K, V> buscarNodo(Nodo<K, V> n, K clave) {
        if (n == null) {
            return null;
        }
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) {
            return buscarNodo(n.izq, clave);
        } else if (cmp > 0) {
            return buscarNodo(n.der, clave);
        } else {
            return n;
        }
    }

    /**
     * Indica si la clave esta en el arbol.
     */
    public boolean contiene(K clave) {
        if (clave == null) {
            return false;
        }
        return contieneRec(raiz, clave);
    }

    private boolean contieneRec(Nodo<K, V> n, K clave) {
        if (n == null) {
            return false;
        }
        avisarVisita();
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) {
            return contieneRec(n.izq, clave);
        } else if (cmp > 0) {
            return contieneRec(n.der, clave);
        } else {
            return true;
        }
    }

    /**
     * k-esimo menor (1-based): kEsimo(1) es el minimo, kEsimo(size()) el maximo.
     * O(h), sin recorrer en inorden.
     */
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
        if (k == tamIzq + 1) {
            return n;
        } else if (k <= tamIzq) {
            return kEsimoRec(n.izq, k);
        } else {
            return kEsimoRec(n.der, k - tamIzq - 1);
        }
    }

    /**
     * Cantidad de claves estrictamente menores que clave. O(h). clave no
     * tiene que estar en el arbol.
     */
    public int cuantosMenores(K clave) {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        return cuantosMenoresRec(raiz, clave);
    }

    private int cuantosMenoresRec(Nodo<K, V> n, K clave) {
        if (n == null) {
            return 0;
        }
        avisarVisita();
        int cmp = clave.compareTo(n.clave);
        if (cmp <= 0) {
            return cuantosMenoresRec(n.izq, clave);
        } else {
            return 1 + tamano(n.izq) + cuantosMenoresRec(n.der, clave);
        }
    }

    /**
     * Cantidad de claves en el intervalo cerrado [a, b]. O(h). a y b no
     * tienen que estar en el arbol.
     */
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

    private boolean contieneClaveEn(K clave) {
        return estaClave(raiz, clave);
    }

    private boolean estaClave(Nodo<K, V> n, K clave) {
        if (n == null) {
            return false;
        }
        avisarVisita();
        int cmp = clave.compareTo(n.clave);
        if (cmp < 0) {
            return estaClave(n.izq, clave);
        } else if (cmp > 0) {
            return estaClave(n.der, clave);
        } else {
            return true;
        }
    }

    /**
     * La misma cuenta que consultarRango pero recorriendo todos los nodos
     * en inorden. Columna de control del experimento (Theta(n) siempre).
     */
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
        if (n == null) {
            return;
        }
        avisarVisita();
        consultarRangoIngenuoRec(n.izq, a, b, contador);
        if (n.clave.compareTo(a) >= 0 && n.clave.compareTo(b) <= 0) {
            contador[0]++;
        }
        consultarRangoIngenuoRec(n.der, a, b, contador);
    }

    /**
     * Posicion 1-based de clave en el inorden = cuantosMenores(clave) + 1.
     */
    public int rango(K clave) throws ClaveInexistenteException {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        if (!contiene(clave)) {
            throw new ClaveInexistenteException("Clave no encontrada: " + clave);
        }
        return cuantosMenores(clave) + 1;
    }

    /**
     * Sucesor inorden, o null si clave es el maximo. O(h).
     */
    public K sucesor(K clave) throws ClaveInexistenteException {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        if (!contiene(clave)) {
            throw new ClaveInexistenteException("Clave no encontrada: " + clave);
        }
        Nodo<K, V> n = buscarNodo(raiz, clave);
        if (n.der != null) {
            return minimo(n.der).clave;
        }
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
            if (cmp < 0) {
                candidato = n;
                n = n.izq;
            } else {
                n = n.der;
            }
        }
        return (candidato == null) ? null : candidato.clave;
    }

    /**
     * Predecesor inorden, o null si clave es el minimo. O(h).
     */
    public K predecesor(K clave) throws ClaveInexistenteException {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        if (!contiene(clave)) {
            throw new ClaveInexistenteException("Clave no encontrada: " + clave);
        }
        Nodo<K, V> n = buscarNodo(raiz, clave);
        if (n.izq != null) {
            return maximo(n.izq).clave;
        }
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
            if (cmp > 0) {
                candidato = n;
                n = n.der;
            } else {
                n = n.izq;
            }
        }
        return (candidato == null) ? null : candidato.clave;
    }

    /** Cantidad de claves. */
    public int size() {
        return tamano(raiz);
    }

    /** Altura: -1 si vacio, 0 si hoja. */
    public int altura() {
        return alturaRec(raiz);
    }

    private int alturaRec(Nodo<K, V> n) {
        if (n == null) {
            return -1;
        }
        return 1 + Math.max(alturaRec(n.izq), alturaRec(n.der));
    }

    /** Nodos visitados desde la creacion o el ultimo reinicio. */
    public long visitas() {
        return visitas;
    }

    /** Reinicia el contador de visitas. */
    public void reiniciarVisitas() {
        visitas = 0;
    }

    /**
     * Verifica el invariante de tamano en todo el arbol. Metodo de
     * depuracion; NO llamarlo dentro de operaciones medibles (recorre todo).
     */
    public boolean tamanosConsistentes() {
        return tamanosConsistentesRec(raiz);
    }

    private boolean tamanosConsistentesRec(Nodo<K, V> n) {
        if (n == null) {
            return true;
        }
        int esperado = 1 + tamano(n.izq) + tamano(n.der);
        if (n.tamano != esperado) {
            return false;
        }
        return tamanosConsistentesRec(n.izq) && tamanosConsistentesRec(n.der);
    }

    /** Raiz del arbol (null si vacio). Se usa en IndiceDoble. */
    public Nodo<K, V> raiz() {
        return raiz;
    }

    @Override
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            private Nodo<K, V> siguiente = minimoInicial();

            private Nodo<K, V> minimoInicial() {
                Nodo<K, V> n = raiz;
                if (n == null) {
                    return null;
                }
                while (n.izq != null) {
                    n = n.izq;
                }
                return n;
            }

            @Override
            public boolean hasNext() {
                return siguiente != null;
            }

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
                    while (m.izq != null) {
                        m = m.izq;
                    }
                    return m;
                }
                Nodo<K, V> cand = null;
                Nodo<K, V> actual = raiz;
                while (actual != null) {
                    int cmp = n.clave.compareTo(actual.clave);
                    if (cmp < 0) {
                        cand = actual;
                        actual = actual.izq;
                    } else {
                        actual = actual.der;
                    }
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
        if (n == null) {
            return;
        }
        toStringRec(n.izq, sb);
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(n.clave).append('(').append(n.tamano).append(')');
        toStringRec(n.der, sb);
    }
}
