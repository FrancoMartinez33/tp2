/**
 * Excepcion NO CHEQUEADA: se lanza cuando se pasa null como clave.
 * Es error de programacion: las claves deben ser no-nulas.
 */
public class ClaveNulaException extends RuntimeException {

    public ClaveNulaException() {
        super();
    }

    public ClaveNulaException(String mensaje) {
        super(mensaje);
    }
}
