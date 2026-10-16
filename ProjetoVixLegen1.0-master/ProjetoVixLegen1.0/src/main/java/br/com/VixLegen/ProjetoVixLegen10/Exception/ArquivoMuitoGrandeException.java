package br.com.VixLegen.ProjetoVixLegen10.Exception;

public class ArquivoMuitoGrandeException extends RuntimeException {
    public ArquivoMuitoGrandeException(String mensagem) { super(mensagem); }
    public ArquivoMuitoGrandeException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
