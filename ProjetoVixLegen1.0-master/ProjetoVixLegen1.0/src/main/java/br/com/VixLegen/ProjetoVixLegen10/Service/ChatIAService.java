package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RagApiException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.ServicoIAIndisponivelException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class ChatIAService {
    private final RagHttpClient rag;
    private final TenantAccessService tenant;
    private final RagSessaoRegistro registro = new RagSessaoRegistro(System::nanoTime,
            TimeUnit.HOURS.toNanos(1), 100);
    public ChatIAService(RagHttpClient rag, TenantAccessService tenant) {
        this.rag = rag; this.tenant = tenant;
    }
    private RagSessaoRegistro.Dono dono() {
        return new RagSessaoRegistro.Dono(tenant.usuarioAtualId(), tenant.empresaAtualId());
    }
    public Map<String, Object> health() { dono(); return rag.health(); }
    public synchronized Map<String, Object> criar() {
        var dono = dono();
        try { registro.conferirCapacidade(); } catch (RagSessaoRegistro.Erro ex) { throw traduzir(ex); }
        var resposta = rag.criar();
        UUID interna;
        try { interna = UUID.fromString(String.valueOf(resposta.get("conversa_id"))); }
        catch (IllegalArgumentException ex) { throw new ServicoIAIndisponivelException("Contrato de sessão inválido"); }
        UUID externa = registro.registrar(dono, interna);
        return publicar(resposta, externa);
    }
    public Map<String, Object> perguntar(UUID id, String mensagem) {
        return operar(id, interna -> publicar(rag.perguntar(interna, mensagem.strip()), id), false);
    }
    public Map<String, Object> resetar(UUID id) {
        return operar(id, interna -> publicar(rag.resetar(interna), id), false);
    }
    public void encerrar(UUID id) {
        operar(id, interna -> { rag.encerrar(interna); return Boolean.TRUE; }, true);
    }
    private <T> T operar(UUID externa, Function<UUID, T> chamada, boolean excluir) {
        UUID interna;
        try { interna = registro.reservar(externa, dono()); }
        catch (RagSessaoRegistro.Erro ex) { throw traduzir(ex); }
        boolean remover = false;
        try {
            T resposta = chamada.apply(interna); remover = excluir; return resposta;
        } catch (RagApiException ex) {
            remover = ex.status() == HttpStatus.GONE;
            throw ex;
        } finally { registro.liberar(externa, remover); }
    }
    private static Map<String, Object> publicar(Map<String, Object> resposta, UUID externa) {
        var copia = new LinkedHashMap<>(resposta);
        copia.put("conversa_id", externa.toString());
        return copia;
    }
    private static RagApiException traduzir(RagSessaoRegistro.Erro ex) {
        return switch (ex.falha) {
            case AUSENTE -> new RagApiException(HttpStatus.NOT_FOUND, "IA_SESSAO_NAO_ENCONTRADA", "Conversa não encontrada ou expirada.");
            case OCUPADA -> new RagApiException(HttpStatus.CONFLICT, "IA_CONVERSA_OCUPADA", "Conversa ocupada.");
            case LIMITE -> new RagApiException(HttpStatus.TOO_MANY_REQUESTS, "IA_LIMITE_SESSOES", "Limite de sessões atingido.");
        };
    }
}
