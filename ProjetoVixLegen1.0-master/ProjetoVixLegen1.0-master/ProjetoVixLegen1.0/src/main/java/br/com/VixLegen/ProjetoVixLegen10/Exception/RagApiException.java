package br.com.VixLegen.ProjetoVixLegen10.Exception;

import org.springframework.http.HttpStatus;

public final class RagApiException extends RuntimeException {
    private final HttpStatus status;
    private final String codigo;
    public RagApiException(HttpStatus status, String codigo, String mensagem) {
        super(mensagem); this.status = status; this.codigo = codigo;
    }
    public HttpStatus status() { return status; }
    public String codigo() { return codigo; }
}
