package br.com.VixLegen.ProjetoVixLegen10.Exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final ApiErroFactory erros;

    public GlobalExceptionHandler(ApiErroFactory erros) {
        this.erros = erros;
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String codigo,
            String mensagem, HttpServletRequest request, Exception ex, Map<String, String> campos) {
        return ResponseEntity.status(status)
                .body(erros.criar(status, codigo, mensagem, request, ex, campos));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> autenticacao(AuthenticationException ex, HttpServletRequest req) {
        return resposta(HttpStatus.UNAUTHORIZED, "AUTENTICACAO_NECESSARIA",
                "Faça login novamente para continuar", req, ex, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String campo = ex.getName() == null ? "parametro" : ex.getName();
        if (!campo.matches("[A-Za-z][A-Za-z0-9_]{0,60}")) campo = "parametro";
        return resposta(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", "Parâmetro com tipo inválido",
                req, ex, Map.of(campo, "Formato inválido"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> restricoes(ConstraintViolationException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "VALIDACAO",
                "Parâmetros fora das regras permitidas", req, ex, Map.of());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> validacaoMetodo(HandlerMethodValidationException ex, HttpServletRequest req) {
        HttpStatus status = ex.isForReturnValue() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
        return resposta(status, "VALIDACAO", status.is5xxServerError()
                ? "Erro interno. Informe o erroId ao suporte" : "Parâmetros inválidos", req, ex, Map.of());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Map<String, Object>> credenciais(CredenciaisInvalidasException ex, HttpServletRequest req) {
        return resposta(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", "Credenciais inválidas", req, ex, Map.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "RECURSO_NAO_ENCONTRADO", ex.getMessage(), req, ex, Map.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> negocio(RegraNegocioException ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "REGRA_NEGOCIO", ex.getMessage(), req, ex, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> conflito(DataIntegrityViolationException ex, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "CONFLITO_INTEGRIDADE",
                "Valor duplicado ou registro vinculado; verifique os dados", req, ex, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                campos.putIfAbsent(e.getField(), e.getDefaultMessage() == null ? "Valor inválido" : e.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "VALIDACAO", "Há campos inválidos", req, ex, campos);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, Object>> entrada(Exception ex, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Corpo ou parâmetros inválidos", req, ex, Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> proibido(AccessDeniedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", "Você não tem permissão", req, ex, Map.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> uploadGrande(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return resposta(HttpStatus.PAYLOAD_TOO_LARGE, "ARQUIVO_GRANDE", "Arquivo acima do limite permitido", req, ex, Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> metodo(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "METODO_INVALIDO", "Método HTTP não permitido", req, ex, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception ex, HttpServletRequest req) {
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Erro interno. Informe o erroId ao suporte", req, ex, Map.of());
    }
}
