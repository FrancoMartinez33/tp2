/**
 * Tabla de dispersion generica con resolucion de colisiones por
 * encadenamiento (listas enlazadas propias, una por cubeta).
 *
 * La funcion de compresion usada es:
 *     h(k) = (k.hashCode() & 0x7fffffff) % m
 * El & 0x7fffffff evita los hashCode negativos (en particular
 * Integer.MIN_VALUE).
 *
 * Tras cada insercion, si n > m (factor de carga alfa > 1) se duplica la
 * cantidad de cubetas y se reubican todas las claves (rehash).
 *
 * El contador de sondas cuenta cada nodo de cadena visitado en obtener,
 * insertar y eliminar. Es analogo al contador de visitas del arbol.
 *
 * El dato viaja como E: en IndiceDoble, E es la referencia al Nodo del ABB.
 *
 * @param <K> tipo de la clave
 * @param <E> tipo del dato almacenado
 */
public class TablaEncadenada<K, E> {

    /** Nodo de lista enlazada propia (por cubeta). */
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

    /** Tabla con m=11 y alfaMax=1 (rehash al superar). */
    public TablaEncadenada() {
        this(TAM_INICIAL, 1.0);
    }

    /** Tabla con m y alfaMax configurables para el experimento. */
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

    /**
     * Inserta (clave, dato). Si la clave ya existia, actualiza el dato.
     * Puede disparar rehash.
     */
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

    /**
     * Retorna el dato asociado a la clave, o null si no esta.
     */
    public E obtener(K clave) {
        if (clave == null) {
            return null;
        }
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

    /**
     * Elimina la clave y retorna su dato, o null si no estaba.
     */
    public E eliminar(K clave) {
        if (clave == null) {
            return null;
        }
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

    /** Cantidad de cubetas. */
    public int capacidad() {
        return cubetas.length;
    }

    /** Cantidad de claves almacenadas. */
    public int size() {
        return n;
    }

    /** Factor de carga alfa = n / m. */
    public double factorCarga() {
        return (double) n / cubetas.length;
    }

    /** Sondas acumuladas desde la creacion o el ultimo reinicio. */
    public long sondas() {
        return sondas;
    }

    /** Reinicia el contador de sondas. */
    public void reiniciarSondas() {
        sondas = 0;
    }

    /**
     * Duplica m y reubica todas las claves. O(n) en una sola llamada.
     */
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

    /**
     * Una linea por cubeta: "[cubeta] clave1 clave2 ..."
     * El orden dentro de la cubeta no se evalua.
     */
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
            if (actual == null) {
                continue;
            }
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
