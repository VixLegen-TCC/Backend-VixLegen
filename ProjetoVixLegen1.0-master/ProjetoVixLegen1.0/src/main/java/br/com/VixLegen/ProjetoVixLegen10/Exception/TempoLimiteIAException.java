package br.com.VixLegen.ProjetoVixLegen10.Exception;

public class TempoLimiteIAException extends RuntimeException {
    public TempoLimiteIAException(String mensagem) { super(mensagem); }
    public TempoLimiteIAException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
