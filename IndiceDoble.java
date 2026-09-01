/**
 * Indice doble: un ABB aumentado que ordena las claves y una tabla de
 * dispersion por encadenamiento que apunta a los MISMOS nodos del arbol.
 *
 * IndiceDoble no reimplementa nada: compone dos estructuras sobre el
 * mismo conjunto de nodos.
 *
 *   - arbol:   ABBAumentado<K, V>, dueno de los nodos.
 *   - tabla:   TablaEncadenada<K, Nodo<K, V>>, guarda referencias a los
 *              nodos del arbol (no copias del valor).
 *
 * La regla clave: obtener(k) se resuelve SOLO por la tabla, siguiendo la
 * referencia al nodo. Asi no incrementa arbol.visitas(). Quien busca con
 * la tabla paga el recorrido de una cubeta, no O(h) comparaciones.
 *
 * eliminar debe tocar las DOS estructuras: si solo se borra del arbol,
 * la tabla queda con una referencia colgante; si solo se borra de la
 * tabla, el arbol sigue respondiendo kEsimo.
 *
 * @param <K> tipo de la clave, comparable
 * @param <V> tipo del valor asociado
 */
public class IndiceDoble<K extends Comparable<? super K>, V> {

    private final ABBAumentado<K, V> arbol;
    private final TablaEncadenada<K, ABBAumentado.Nodo<K, V>> tabla;

    /** Indice doble vacio (m = 11, alfaMax = 1). */
    public IndiceDoble() {
        this.arbol = new ABBAumentado<>();
        this.tabla = new TablaEncadenada<>();
    }

    /** Indice doble con tabla configurable (experimento del TP). */
    public IndiceDoble(int m, double alfaMax) {
        this.arbol = new ABBAumentado<>();
        this.tabla = new TablaEncadenada<>(m, alfaMax);
    }

    /**
     * Inserta (o actualiza) en el arbol y registra en la tabla la
     * referencia al nodo correspondiente.
     */
    public void agregar(K clave, V valor) {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        arbol.agregar(clave, valor);
        tabla.insertar(clave, arbol.nodoDe(clave));
    }

    /**
     * Resuelve solo por la tabla. No incrementa arbol.visitas().
     */
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

    /** Mismo que obtener pero devolviendo el Nodo (util para pruebas). */
    public ABBAumentado.Nodo<K, V> obtenerNodo(K clave) {
        if (clave == null) {
            return null;
        }
        return tabla.obtener(clave);
    }

    /** Elimina de ambas estructuras y retorna el valor. */
    public V eliminar(K clave) throws ClaveInexistenteException {
        if (clave == null) {
            throw new ClaveNulaException("No se admiten claves nulas.");
        }
        V valor = arbol.eliminar(clave);
        tabla.eliminar(clave);
        return valor;
    }

    /** k-esimo menor; delega en el arbol (O(h)). */
    public K kEsimo(int k) {
        return arbol.kEsimo(k);
    }

    /** Cantidad de claves en [a, b]; delega en el arbol (O(h)). */
    public int consultarRango(K a, K b) {
        return arbol.consultarRango(a, b);
    }

    /** Indica si la clave esta (por la tabla). */
    public boolean contiene(K clave) {
        return tabla.obtener(clave) != null;
    }

    /** Las dos estructuras tienen que reportar el mismo n. */
    public int size() {
        return arbol.size();
    }

    /** Arbol aumentado interno (para pruebas y medidas). */
    public ABBAumentado<K, V> arbol() {
        return arbol;
    }

    /** Tabla interna (para pruebas y medidas). */
    public TablaEncadenada<K, ABBAumentado.Nodo<K, V>> tabla() {
        return tabla;
    }
}