/**
 * Excepcion CHEQUEADA: se lanza cuando se busca una clave que no esta
 * en la estructura. Es chequeada porque es una situacion que el codigo
 * que llama puede prever y manejar (un cliente busca un registro que
 * puede o no existir).
 */
public class ClaveInexistenteException extends Exception {

    public ClaveInexistenteException() {
        super();
    }

    public ClaveInexistenteException(String mensaje) {
        super(mensaje);
    }
}
