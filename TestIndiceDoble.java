import java.util.Random;

/**
 * Clase de prueba del Ejercicio 2 (IndiceDoble).
 *
 * Reproduce la traza obligatoria (Fases A-D) y genera las dos tablas del
 * experimento:
 *
 *   (1) Indice doble con rehash: N obtener por arbol vs por indice.
 *   (2) Tabla sola con m=97 fijo y sin rehash: alfa y sondas/N.
 *
 * La regla central: indice.obtener(k) resuelve por la tabla y NO
 * incrementa arbol.visitas(). En la Fase C hay que obtener 0 visitas
 * del arbol y <= 2 sondas de la tabla.
 */
public class TestIndiceDoble {

    public static void main(String[] args) throws Exception {
        System.out.println("=== TRAZA OBLIGATORIA ===");
        ejecutarTraza();

        System.out.println("\n=== EXPERIMENTO 1: INDICE DOBLE CON REHASH ===");
        experimentoIndiceDoble();

        System.out.println("\n=== EXPERIMENTO 2: TABLA SOLA, m=97 FIJO, SIN REHASH ===");
        experimentoTablaFija97();
    }

    private static IndiceDoble<Integer, String> indiceTraza() throws ClaveInexistenteException {
        IndiceDoble<Integer, String> indice = new IndiceDoble<>();
        int[] claves = {50, 30, 70, 20, 40, 60, 80, 35, 65};
        for (int k : claves) {
            indice.agregar(k, "P" + k);
        }
        return indice;
    }

    private static void ejecutarTraza() throws ClaveInexistenteException {
        System.out.println("-- Fase A: insertar las 9 claves del Ejercicio 1 --");
        IndiceDoble<Integer, String> indice = indiceTraza();
        System.out.println(indice.tabla().dumpSoloOcupadas());
        System.out.println("arbol n=" + indice.arbol().size()
                + " | tabla n=" + indice.tabla().size()
                + " | m=" + indice.tabla().capacidad());

        int[][] esperadosCubeta = {
                {35, 2}, {80, 3}, {70, 4}, {60, 5}, {50, 6},
                {40, 7}, {30, 8}, {20, 9}, {65, 10}};
        boolean faseAOk = true;
        for (int[] par : esperadosCubeta) {
            int clave = par[0];
            int cubetaEsperada = par[1];
            int cubetaReal = (clave & 0x7fffffff) % indice.tabla().capacidad();
            boolean ok = cubetaReal == cubetaEsperada
                    && indice.obtenerNodo(clave).valor().equals("P" + clave);
            faseAOk = faseAOk && ok;
            System.out.println("  " + clave + " (cubeta " + cubetaReal + ", esperada "
                    + cubetaEsperada + ", valor P" + clave + ") "
                    + (ok ? "OK" : "ERROR"));
        }
        System.out.println("Fase A: " + (faseAOk ? "OK" : "ERROR"));

        System.out.println("-- Fase B: agregar(61) y agregar(41) --");
        indice.agregar(61, "P61");
        indice.agregar(41, "P41");
        System.out.println(indice.tabla().dumpSoloOcupadas());
        System.out.println("arbol n=" + indice.arbol().size()
                + " | tabla n=" + indice.tabla().size()
                + " | arbol.toString=" + indice.arbol());

        System.out.println("-- Fase C: indice.obtener(70) --");
        indice.arbol().reiniciarVisitas();
        indice.tabla().reiniciarSondas();
        String v70 = indice.obtener(70);
        System.out.println("resultado=" + v70
                + " | arbol.visitas=" + indice.arbol().visitas()
                + " (esperado 0) | tabla.sondas=" + indice.tabla().sondas()
                + " (esperado <= 2)");

        indice.arbol().reiniciarVisitas();
        String v70Arbol = indice.arbol().obtener(70);
        System.out.println("arbol.obtener(70) sobre el mismo indice -> " + v70Arbol
                + " | arbol.visitas=" + indice.arbol().visitas()
                + " (esperado 2: camino 50->70)");

        System.out.println("-- Fase D: indice.eliminar(30) --");
        String eliminado = indice.eliminar(30);
        System.out.println("eliminado=" + eliminado + " | toString=" + indice.arbol()
                + " | n=" + indice.size());
        System.out.println(indice.tabla().dumpSoloOcupadas());
        System.out.println("contiene(30)=" + indice.contiene(30)
                + " | arbol.contiene(30)=" + indice.arbol().contiene(30)
                + " | kEsimo(2)=" + indice.kEsimo(2)
                + " | indice.obtener(35)=" + indice.obtener(35)
                + " | size arbol=" + indice.arbol().size()
                + " | size tabla=" + indice.tabla().size());
    }

    private static void experimentoIndiceDoble() throws ClaveInexistenteException {
        int[] tamanos = {2000, 4000, 6000, 8000, 10000};
        System.out.printf("%-7s %-14s %-16s %-6s %-6s%n",
                "N", "vis_ABB_get", "sondas_hash_get", "alfa", "m");
        for (int n : tamanos) {
            int[] perm = permutacion(n);
            IndiceDoble<Integer, String> indice = new IndiceDoble<>();
            for (int v : perm) {
                indice.agregar(v, "P" + v);
            }
            indice.arbol().reiniciarVisitas();
            indice.tabla().reiniciarSondas();
            for (int v : perm) {
                indice.arbol().obtener(v);
                indice.obtener(v);
            }
            System.out.printf("%-7d %-14d %-16d %-6.3f %-6d%n",
                    n,
                    indice.arbol().visitas(),
                    indice.tabla().sondas(),
                    indice.tabla().factorCarga(),
                    indice.tabla().capacidad());
        }
    }

    private static void experimentoTablaFija97() {
        int[] tamanos = {2000, 4000, 6000, 8000, 10000};
        System.out.printf("%-7s %-8s %-10s%n", "N", "alfa", "sondas/N");
        for (int n : tamanos) {
            TablaEncadenada<Integer, Integer> tabla = new TablaEncadenada<>(97, Double.POSITIVE_INFINITY);
            for (int i = 1; i <= n; i++) {
                tabla.insertar(i, i);
            }
            tabla.reiniciarSondas();
            for (int i = 1; i <= n; i++) {
                tabla.obtener(i);
            }
            System.out.printf("%-7d %-8.3f %-10.4f%n",
                    n, tabla.factorCarga(), (double) tabla.sondas() / n);
        }
    }

    // Permutacion de 1..n con semilla 2026 (Fisher-Yates).
    private static int[] permutacion(int n) {
        Random rng = new Random(2026);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = i + 1;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
        return a;
    }
}