import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Validacion exhaustiva del TP2. Verifica automaticamente que la
 * implementacion sea correcta frente a un oraculo (fuerza bruta) y
 * cubre los casos limite que pide el enunciado.
 *
 * 1. Orden de insercion degenerado (lista): kEsimo vs arreglo ordenado.
 * 2. kEsimo(i) para todo i, sobre arboles aleatorios con inserciones
 *    y borrados intercalados.
 * 3. cuantosMenores / consultarRango / rango vs fuerza bruta.
 * 4. Casos limite: kEsimo(0), kEsimo(n+1), null, rango a>b.
 * 5. Clave duplicada actualiza valor sin cambiar estructura ni tamanos.
 * 6. Genoricos con String como clave.
 * 7. El sucesor MUDADO conserva su identidad de objeto (no se copio clave).
 * 8. IndiceDoble: n arbol == n tabla en todo momento, obtener por tabla
 *    con 0 visitas del arbol, sincronia tras eliminar.
 * 9. Rehash forzado (alfaMax muy chico): todas las claves accesibles.
 */
public class TestValidacion {

    private static int errores = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== 1. ABB degenerado: kEsimo vs fuerza bruta ===");
        testABBDegenerado();

        System.out.println("=== 2. Intercalar insert/delete con oracle ===");
        testAccionesAleatorias();

        System.out.println("=== 3. cuantosMenores / consultarRango / rango ===");
        testRangos();

        System.out.println("=== 4. Casos limite y excepciones ===");
        testExcepciones();

        System.out.println("=== 5. Clave duplicada actualiza valor ===");
        testDuplicada();

        System.out.println("=== 6. Genericidad: claves String ===");
        testStrings();

        System.out.println("=== 7. Identidad del nodo mudado en eliminar ===");
        testIdentidadSucesor();

        System.out.println("=== 8. IndiceDoble sincronizado ===");
        testIndiceDobleSincro();

        System.out.println("=== 9. Rehash forzado ===");
        testRehashForzado();

        System.out.println("\n=== RESULTADO: " + (errores == 0 ? "TODOS LOS TEST OK" : errores + " ERRORES") + " ===");
    }

    private static void ok(boolean condicion, String mensaje) {
        if (condicion) {
            System.out.println("  [OK] " + mensaje);
        } else {
            errores++;
            System.out.println("  [ERROR] " + mensaje);
        }
    }

    private static void testABBDegenerado() throws ClaveInexistenteException {
        ABBAumentado<Integer, String> arbol = new ABBAumentado<>();
        int n = 1000;
        for (int i = 1; i <= n; i++) {
            arbol.agregar(i, "P" + i);
        }
        boolean okTodo = true;
        for (int k = 1; k <= n; k++) {
            if (arbol.kEsimo(k) != k) {
                okTodo = false;
                break;
            }
        }
        ok(okTodo, "kEsimo i == i para i in 1..1000 (arbol lista)");
        ok(arbol.altura() == n - 1, "altura lista == n-1 (" + arbol.altura() + ")");
        ok(arbol.sucesor(n) == null, "sucesor del maximo es null");
        ok(arbol.predecesor(1) == null, "predecesor del minimo es null");
        ok(arbol.consultarRango(1, n) == n, "rango completo == n");
    }

    private static void testAccionesAleatorias() {
        Random rng = new Random(7);
        ABBAumentado<Integer, String> arbol = new ABBAumentado<>();
        List<Integer> presentes = new ArrayList<>();
        int[] todos = new int[400];
        for (int i = 0; i < todos.length; i++) {
            todos[i] = i + 1;
        }
        // mezclar los 400
        for (int i = todos.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int t = todos[i];
            todos[i] = todos[j];
            todos[j] = t;
        }
        int idx = 0;
        for (int paso = 0; paso < 400; paso++) {
            int clave = todos[idx++];
            arbol.agregar(clave, "P" + clave);
            presentes.add(clave);

            // en cada paso, verificar kEsimo de todos los presentes
            int[] ordenado = presentes.stream().mapToInt(Integer::intValue).sorted().toArray();
            boolean okTodos = true;
            for (int i = 1; i <= ordenado.length; i++) {
                if (arbol.kEsimo(i) != ordenado[i - 1]) {
                    okTodos = false;
                    break;
                }
            }
            if (!okTodos || !arbol.tamanosConsistentes()) {
                errores++;
                System.out.println("  [ERROR] tras agregar " + clave);
                return;
            }
        }
        System.out.println("  [OK] kEsimo correcto en 400 inserciones (verificado en cada paso)");

        // ahora borrar hasta la mitad
        for (int i = 0; i < 200; i++) {
            int claveAEliminar = presenteAleatorio(rng, presentes, arbol);
            try {
                arbol.eliminar(claveAEliminar);
            } catch (ClaveInexistenteException e) {
                errores++;
                System.out.println("  [ERROR] eliminar clave no existente: " + claveAEliminar);
                return;
            }
            // remover de la lista
            for (int j = 0; j < presentes.size(); j++) {
                if (presentes.get(j) == claveAEliminar) {
                    presentes.remove(j);
                    break;
                }
            }
            int[] ordenado = presentes.stream().mapToInt(Integer::intValue).sorted().toArray();
            boolean okTodos = true;
            for (int k = 1; k <= ordenado.length; k++) {
                if (arbol.kEsimo(k) != ordenado[k - 1]) {
                    okTodos = false;
                    break;
                }
            }
            if (!okTodos || !arbol.tamanosConsistentes() || arbol.size() != ordenado.length) {
                errores++;
                System.out.println("  [ERROR] tras eliminar " + claveAEliminar);
                return;
            }
        }
        System.out.println("  [OK] kEsimo correcto tras 200 borrados intercalados");
        ok(arbol.size() == 200, "size coincide (200)");
    }

    private static int presenteAleatorio(Random rng, List<Integer> presentes, ABBAumentado<Integer, String> arbol) {
        int k = rng.nextInt(presentes.size()) + 1;
        return presentes.get(k - 1);
    }

    private static void testRangos() throws ClaveInexistenteException {
        ABBAumentado<Integer, String> arbol = new ABBAumentado<>();
        int[] claves = {50, 30, 70, 20, 40, 60, 80, 35, 65, 61, 41};
        for (int c : claves) {
            arbol.agregar(c, "P" + c);
        }
        int[] ordenado = {20, 30, 35, 40, 41, 50, 60, 61, 65, 70, 80};

        // cuantosMenores contra fuerza bruta, para muchos valores
        boolean okCM = true;
        for (int x = 15; x <= 85; x += 2) {
            int esperado = 0;
            for (int c : ordenado) {
                if (c < x) {
                    esperado++;
                }
            }
            if (arbol.cuantosMenores(x) != esperado) {
                okCM = false;
                System.out.println("    cuantosMenores(" + x + ") = " + arbol.cuantosMenores(x) + " esperado " + esperado);
            }
        }
        ok(okCM, "cuantosMenores(x) vs fuerza bruta para x in 15..85");

        boolean okRangos = true;
        for (int a = 15; a <= 85; a += 3) {
            for (int b = a; b <= 85; b += 3) {
                int esperado = 0;
                for (int c : ordenado) {
                    if (c >= a && c <= b) {
                        esperado++;
                    }
                }
                int real = arbol.consultarRango(a, b);
                int realIng = arbol.consultarRangoIngenuo(a, b);
                if (real != esperado || realIng != esperado) {
                    okRangos = false;
                    System.out.println("    consultarRango(" + a + "," + b + ")=" + real
                            + " ingenuo=" + realIng + " esperado=" + esperado);
                }
            }
        }
        ok(okRangos, "consultarRango vs fuerza bruta en grilla a,b");

        ok(arbol.rango(50) == 6, "rango(50) == 6 (en arbol de 11)");
        ok(arbol.rango(20) == 1, "rango(min) == 1");
        ok(arbol.rango(80) == 11, "rango(max) == 11");
    }

    private static void testExcepciones() {
        ABBAumentado<Integer, String> arbol = new ABBAumentado<>();
        arbol.agregar(10, "x");
        boolean okEx = true;

        try {
            arbol.kEsimo(0);
            okEx = false;
        } catch (IndiceFueraDeRangoException e) {
            // bien
        } catch (Exception e) {
            okEx = false;
        }
        try {
            arbol.kEsimo(2);
            okEx = false;
        } catch (IndiceFueraDeRangoException e) {
            // bien
        } catch (Exception e) {
            okEx = false;
        }

        // arbol vacio: kEsimo debe fallar
        ABBAumentado<Integer, String> vacio = new ABBAumentado<>();
        try {
            vacio.kEsimo(1);
            okEx = false;
        } catch (IndiceFueraDeRangoException e) {
            // bien
        }

        // ClaveNula
        try {
            arbol.agregar(null, "y");
            okEx = false;
        } catch (ClaveNulaException e) {
            // bien
        }

        // RangoInvalido
        try {
            arbol.consultarRango(20, 5);
            okEx = false;
        } catch (RangoInvalidoException e) {
            // bien
        }

        // ClaveInexistente (chequeada) en obtener/eliminar/sucesor/predecesor/rango
        try {
            arbol.obtener(999);
            okEx = false;
        } catch (ClaveInexistenteException e) {
            // bien
        }
        try {
            arbol.eliminar(999);
            okEx = false;
        } catch (ClaveInexistenteException e) {
            // bien
        }
        try {
            arbol.sucesor(999);
            okEx = false;
        } catch (ClaveInexistenteException e) {
            // bien
        }
        try {
            arbol.predecesor(999);
            okEx = false;
        } catch (ClaveInexistenteException e) {
            // bien
        }
        try {
            arbol.rango(999);
            okEx = false;
        } catch (ClaveInexistenteException e) {
            // bien
        }

        ok(okEx, "todas las excepciones se lanzan correctamente");
        ok(arbol.size() == 1 && arbol.toString().equals("10(1)"), "estructura intacta tras excepciones");
    }

    private static void testDuplicada() throws ClaveInexistenteException {
        ABBAumentado<Integer, String> arbol = new ABBAumentado<>();
        arbol.agregar(50, "P50");
        arbol.agregar(30, "P30");
        arbol.agregar(70, "P70");
        String antes = arbol.toString();
        arbol.agregar(30, "P30_NUEVO");
        ok(arbol.toString().equals(antes),
                "insertar clave duplicada no cambia estructura/tamanos");
        ok(arbol.obtener(30).equals("P30_NUEVO"), "valor actualizado");
        ok(arbol.size() == 3, "size sigue siendo 3");
    }

    private static void testStrings() throws ClaveInexistenteException {
        ABBAumentado<String, Integer> arbol = new ABBAumentado<>();
        arbol.agregar("manzana", 1);
        arbol.agregar("banana", 2);
        arbol.agregar("cereza", 3);
        arbol.agregar("durazno", 4);
        ok(arbol.kEsimo(1).equals("banana"), "kEsimo(1) con String == banana");
        ok(arbol.kEsimo(4).equals("manzana"), "kEsimo(4) con String == manzana");
        ok(arbol.consultarRango("a", "c") == 1, "rango [a,c] con String == 1 (solo banana; cereza > c)");
        ok(arbol.obtener("durazno") == 4, "obtener valor entero");
        ok(arbol.eliminar("cereza") == 3, "eliminar devuelve valor");
        ok(arbol.size() == 3, "size tras eliminar == 3");
        StringBuilder sb = new StringBuilder();
        for (String s : arbol) {
            sb.append(s).append(' ');
        }
        ok(sb.toString().trim().equals("banana durazno manzana"), "inorden con String OK");
    }

    private static void testIdentidadSucesor() throws ClaveInexistenteException {
        ABBAumentado<Integer, String> arbol = new ABBAumentado<>();
        int[] claves = {50, 30, 70, 20, 40, 60, 80, 35, 65};
        for (int c : claves) {
            arbol.agregar(c, "P" + c);
        }
        ABBAumentado.Nodo<Integer, String> nodo35 = arbol.nodoDe(35);
        // 30 tiene dos hijos; el sucesor inorden es 35.
        arbol.eliminar(30);
        ABBAumentado.Nodo<Integer, String> nodo35TrasEliminar = arbol.nodoDe(35);
        ok(nodo35 == nodo35TrasEliminar,
                "el nodo 35 murio y sigue siendo el MISMO objeto (mudanza, no copia)");
        ok(nodo35.clave() == 35 && nodo35.valor().equals("P35"),
                "el nodo conserva clave y valor correctos");
        ok(arbol.nodoDe(30) == null, "el nodo 30 ya no esta");
    }

    private static void testIndiceDobleSincro() throws ClaveInexistenteException {
        Random rng = new Random(11);
        IndiceDoble<Integer, String> indice = new IndiceDoble<>();
        List<Integer> presentes = new ArrayList<>();
        boolean okSync = true;
        for (int paso = 0; paso < 300; paso++) {
            int clave = rng.nextInt(1000) + 1;
            if (rng.nextBoolean()) {
                indice.agregar(clave, "P" + clave);
                if (!presentes.contains(clave)) {
                    presentes.add(clave);
                }
            } else if (!presentes.isEmpty()) {
                int elegida = presentes.get(rng.nextInt(presentes.size()));
                String v = indice.eliminar(elegida);
                if (v == null || !v.equals("P" + elegida)) {
                    okSync = false;
                }
                for (int i = 0; i < presentes.size(); i++) {
                    if (presentes.get(i) == elegida) {
                        presentes.remove(i);
                        break;
                    }
                }
            }
            if (indice.arbol().size() != indice.tabla().size()
                    || indice.size() != presentes.size()
                    || !indice.arbol().tamanosConsistentes()) {
                okSync = false;
            }
        }
        ok(okSync, "arbol y tabla reportan el mismo n en 300 operaciones mezcladas");

        // obtener por indice: 0 visitas; por arbol: > 0
        indice.arbol().reiniciarVisitas();
        indice.tabla().reiniciarSondas();
        boolean okGet = true;
        for (Integer c : presentes) {
            indice.obtener(c);
        }
        long vIndice = indice.arbol().visitas();
        if (vIndice != 0) {
            okGet = false;
        }
        indice.arbol().reiniciarVisitas();
        boolean okArbolGet = true;
        for (Integer c : presentes) {
            indice.arbol().obtener(c);
        }
        long vArbol = indice.arbol().visitas();
        if (vArbol == 0) {
            okArbolGet = false;
        }
        ok(okGet && okArbolGet,
                "obtener por indice suma 0 visitas del arbol, obtener por arbol suma "
                        + vIndice + "/" + vArbol);

        // verificar que la tabla guarda la referencia correcta al nodo
        boolean okRefs = true;
        for (Integer c : presentes) {
            ABBAumentado.Nodo<Integer, String> nodo = indice.obtenerNodo(c);
            if (nodo == null || nodo.clave().intValue() != c
                    || !nodo.valor().equals("P" + c)
                    || indice.arbol().nodoDe(c) != nodo) {
                okRefs = false;
            }
        }
        ok(okRefs, "toda referencia de la tabla apunta al MODO nodo del arbol");
    }

    private static void testRehashForzado() throws ClaveInexistenteException {
        // alfaMax minisculo fuerza rehash continuo
        IndiceDoble<Integer, String> indice = new IndiceDoble<>(11, 0.3);
        Random rng = new Random(5);
        List<Integer> presentes = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            int clave = rng.nextInt(20000) + 1;
            if (!presentes.contains(clave)) {
                indice.agregar(clave, "P" + clave);
                presentes.add(clave);
            }
        }
        boolean okRehash = true;
        for (Integer c : presentes) {
            try {
                String v = indice.obtener(c);
                if (v == null || !v.equals("P" + c)) {
                    okRehash = false;
                    break;
                }
                if (indice.arbol().nodoDe(c).clave().intValue() != c) {
                    okRehash = false;
                    break;
                }
            } catch (ClaveInexistenteException e) {
                okRehash = false;
                break;
            }
        }
        ok(okRehash, "tras " + indice.tabla().capacidad() + " rehashes alfaMax=0.3, "
                + presentes.size() + " claves siguen accesibles");
        ok(indice.arbol().size() == indice.tabla().size() && indice.tabla().size() == presentes.size(),
                "size sincronizado tras muchos rehashes (m=" + indice.tabla().capacidad() + ")");
    }
}