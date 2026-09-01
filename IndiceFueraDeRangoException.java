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
