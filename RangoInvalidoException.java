/**
 * Excepcion NO CHEQUEADA: se lanza cuando a > b en consultarRango.
 * Representa un error de programacion del que llama.
 */
public class RangoInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RangoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
