package br.com.VixLegen.ProjetoVixLegen10.Exception;

/** Conflito conhecido, com campo opcional para destacar no formulário. */
public class ConflitoRecursoException extends RuntimeException {
    private final String campo;

    public ConflitoRecursoException(String mensagem) {
        super(mensagem);
        this.campo = null;
    }

    public ConflitoRecursoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
        this.campo = null;
    }

    public ConflitoRecursoException(String mensagem, String campo) {
        super(mensagem);
        if (campo == null || !campo.matches("cpf|cnpj|email")) {
            throw new IllegalArgumentException("Campo de conflito inválido");
        }
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
