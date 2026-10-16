package br.com.VixLegen.ProjetoVixLegen10.Exception;

public class TentativasLoginExcedidasException extends RuntimeException {
    public TentativasLoginExcedidasException() {
        super("Muitas tentativas de login. Aguarde 15 minutos.");
    }
}
