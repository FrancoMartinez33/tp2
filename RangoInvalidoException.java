/**
 * Excepcion NO CHEQUEADA: se lanza cuando a > b en consultarRango.
 * Representa un error de programacion del que llama.
 */
public class RangoInvalidoException extends RuntimeException {

    public RangoInvalidoException() {
        super();
    }

    public RangoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
