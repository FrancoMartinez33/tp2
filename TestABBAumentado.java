import java.util.Random;

/**
 * Clase de prueba del Ejercicio 1 (ABBAumentado).
 *
 * 1. Reproduce la traza obligatoria: inserciones, Fase B (consultas con
 *    contador de visitas) y Fase C (eliminar 30, con mudanza del sucesor).
 * 2. Verifica el recorrido por for-each.
 * 3. Genera la tabla de visitas para N in {2000,4000,6000,8000,10000}
 *    comparando un arbol aleatorio (semilla 2026, Fisher-Yates) contra un
 *    arbol degenerado (insercion ordenada).
 *
 * Los resultados de las columnas deterministas deben ser exactos:
 *   h_ord = N-1, vis_kEsimo_ord = N/2, vis_rango_ing = N.
 */
public class TestABBAumentado {

    public static void main(String[] args) {
        System.out.println("=== TRAZA OBLIGATORIA (inserciones) + FASE B + FASE C ===");
        ABBAumentado<Integer, String> arbol = arbolTraza();

        System.out.println("\n--- FASE B: CONSULTAS CON VISITAS ---");
        ejecutarFaseB(arbol);

        System.out.println("\n--- FASE C: eliminar(30) ---");
        ejecutarFaseC(arbol);

        System.out.println("\n=== 4. RECORRIDO FOR-EACH (inorden) ===");
        verificarForEach();

        System.out.println("\n=== 5. TABLA DE VISITAS (N aleatorio vs ordenado) ===");
        tablaDeVisitas();
    }

    private static ABBAumentado<Integer, String> arbolTraza() {
        ABBAumentado<Integer, String> arbol = new ABBAumentado<>();
        // El enunciado quiere un arbol de la traza con 9 claves y altura 3:
        // los valores son "P" + clave; usa el mismo orden de inserción de la tabla.
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
        r = arbol.cuantosMenores(35);
        System.out.println("cuantosMenores(35) -> " + r + " | visitas=" + arbol.visitas()
                + " (esperado: 4)");

        arbol.reiniciarVisitas();
        r = arbol.consultarRango(35, 65);
        System.out.println("consultarRango(35, 65) -> " + r + " | visitas=" + arbol.visitas()
                + " (O(h)");

        arbol.reiniciarVisitas();
        r = arbol.consultarRangoIngenuo(35, 65);
        System.out.println("consultarRangoIngenuo(35, 65) -> " + r + " | visitas=" + arbol.visitas()
                + " (esperado: 9)");

        try {
            arbol.reiniciarVisitas();
            Integer s40 = arbol.sucesor(40);
            System.out.println("sucesor(40) -> " + s40 + " | visitas=" + arbol.visitas());

            arbol.reiniciarVisitas();
            Integer s80 = arbol.sucesor(80);
            System.out.println("sucesor(80) -> " + s80 + " | visitas=" + arbol.visitas());

            arbol.reiniciarVisitas();
            Integer p35 = arbol.predecesor(35);
            System.out.println("predecesor(35) -> " + p35 + " | visitas=" + arbol.visitas());

            arbol.reiniciarVisitas();
            int rango50 = arbol.rango(50);
            System.out.println("rango(50) -> " + rango50 + " | visitas=" + arbol.visitas());
        } catch (ClaveInexistenteException e) {
            System.out.println("Error inesperado en Fase B: " + e.getMessage());
        }
    }

    private static void ejecutarFaseC(ABBAumentado<Integer, String> arbol) {
        try {
            String eliminado = arbol.eliminar(30);
            System.out.println("eliminar(30) -> \"" + eliminado + "\" | " + arbol
                    + " | h=" + arbol.altura() + " size=" + arbol.size()
                    + " consistentes=" + arbol.tamanosConsistentes());
            System.out.println("kEsimo(2) = " + arbol.kEsimo(2)
                    + " | consultarRango(35,65) = " + arbol.consultarRango(35, 65)
                    + " | sucesor(20) = " + arbol.sucesor(20)
                    + " | contiene(30) = " + arbol.contiene(30));
        } catch (ClaveInexistenteException e) {
            System.out.println("Error inesperado en Fase C: " + e.getMessage());
        }
    }

    private static void verificarForEach() {
        ABBAumentado<Integer, String> arbol = new ABBAumentado<>();
        int[] claves = {50, 30, 70, 20, 40, 60, 80, 35, 65};
        for (int k : claves) {
            arbol.agregar(k, "P" + k);
        }
        StringBuilder sb = new StringBuilder();
        for (Integer k : arbol) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(k);
        }
        boolean ok = sb.toString().equals("20 30 35 40 50 60 65 70 80");
        System.out.println("for-each -> [" + sb + "]  " + (ok ? "OK" : "ERROR"));
    }

    /**
     * TENDENCIAS QUE SE VEN EN LA TABLA (justificacion):
     *
     * 1. h_ord = N-1: insertar 1..N en orden cuelga cada clave nueva a la
     *    derecha de la ultima; el arbol degenera en una lista enlazada y la
     *    altura coincide con la cantidad de aristas: N-1.
     *    vis_kEsimo_ord = N/2: el k-esimo pedido es N/2 (clave del medio de
     *    la lista), y kEsimo en una lista baja exactamente hasta esa posicion:
     *    N/2 visitas para N par (o (N-1)/2 para N impar). Es O(N): la lista
     *    NO deja de ser el peor caso del ABB.
     *
     * 2. vis_rango_ing = N: consultarRangoIngenuo recorre SIEMPRE todos los
     *    nodos en inorden, uno por visita. El aumentado baja por dos caminos
     *    de altura h ~ log2(N): por eso vis_rango_aum queda en unas decenas
     *    (46-59) mientras N crece mil veces. Esa diferencia es el techo del
     *    TP: un arreglo menor a mayor con rangos te cuesta Theta(n) siempre.
     *
     * 3. En el arbol chico de la traza (9 nodos) el aumentado consultarRango
     *    reporta 12 visitas y el ingenuo 9. No contradice el punto 2:
     *    el aumentado paga 2-3 caminos completos (cuantosMenores de ambos
     *    limites mas la verificacion de b); el ingenuo recorre una vez los 9
     *    nodos. Cuando n > h este costo fijo de los caminos queda oculto: con
     *    N=2000 el ingenuo paga 2000 visitas y el aumentado 47. El experimento
     *    grande existe justamente para que esa brecha se vea con numeros.
     */
    private static void tablaDeVisitas() {
        int[] tamanos = {2000, 4000, 6000, 8000, 10000};
        System.out.printf("%-6s %-8s %-8s %-12s %-12s %-12s %-12s%n",
                "N", "h_aleat", "h_ord", "vis_kEsimo_aleat", "vis_kEsimo_ord", "vis_rango_aum", "vis_rango_ing");
        for (int n : tamanos) {
            int[] perm = permutacionAleatoria(n);
            int[] ordenado = secuencia(n);

            ABBAumentado<Integer, String> aleat = new ABBAumentado<>();
            for (int v : perm) {
                aleat.agregar(v, "P" + v);
            }
            ABBAumentado<Integer, String> ord = new ABBAumentado<>();
            for (int v : ordenado) {
                ord.agregar(v, "P" + v);
            }

            int hA = aleat.altura();
            int hO = ord.altura();

            aleat.reiniciarVisitas();
            Integer kEsimoA = aleat.kEsimo(n / 2);
            long visA = aleat.visitas();

            ord.reiniciarVisitas();
            Integer kEsimoO = ord.kEsimo(n / 2);
            long visO = ord.visitas();

            Integer a = aleat.kEsimo(n / 4);
            Integer b = aleat.kEsimo(3 * n / 4);
            aleat.reiniciarVisitas();
            int rAum = aleat.consultarRango(a, b);
            long visRangoAum = aleat.visitas();

            aleat.reiniciarVisitas();
            int rIng = aleat.consultarRangoIngenuo(a, b);
            long visRangoIng = aleat.visitas();

            System.out.printf("%-6d %-8d %-8d %-12d %-12d %-12d %-12d%n",
                    n, hA, hO, visA, visO, visRangoAum, visRangoIng);
        }
    }

    private static int[] secuencia(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = i + 1;
        }
        return a;
    }

    // Permutacion de 1..n con semilla 2026 (Fisher-Yates).
    private static int[] permutacionAleatoria(int n) {
        Random rng = new Random(2026);
        int[] a = secuencia(n);
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
        return a;
    }
}