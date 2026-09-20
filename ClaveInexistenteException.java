/**
 * Excepcion CHEQUEADA: se lanza cuando se busca una clave que no esta
 * en la estructura. Es chequeada porque es una situacion que el codigo
 * que llama puede prever y manejar (un cliente busca un registro que
 * puede o no existir).
 */
public class ClaveInexistenteException extends Exception {

    private static final long serialVersionUID = 1L;

    public ClaveInexistenteException(String mensaje) {
        super(mensaje);
    }
}
