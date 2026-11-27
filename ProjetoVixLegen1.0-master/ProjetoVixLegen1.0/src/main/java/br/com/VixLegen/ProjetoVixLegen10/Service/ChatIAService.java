package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RagApiException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.ServicoIAIndisponivelException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
public class ChatIAService {
    private final RagHttpClient rag;
    private final TenantAccessService tenant;
    private final ChatHistoricoStore store;
    // Um único Spring Boot, conforme arquitetura local. Limita memória e evita corridas por conversa.
    private final ReentrantLock[] locks=new ReentrantLock[256];
    public ChatIAService(RagHttpClient rag,TenantAccessService tenant,ChatHistoricoStore store) {
        this.rag=rag;this.tenant=tenant;this.store=store;
        Arrays.setAll(locks,i->new ReentrantLock());
    }
    private ChatHistoricoStore.Dono dono(){return new ChatHistoricoStore.Dono(tenant.usuarioAtualId(),tenant.empresaAtualId());}
    public Map<String,Object> health(){dono();return rag.health();}
    public synchronized Map<String,Object> criar(){return store.criar(dono());}
    public List<Map<String,Object>> listar(){return store.listar(dono());}
    public Map<String,Object> detalhes(UUID id){return store.detalhes(id,dono());}
    private <T> T operar(UUID id,ChatHistoricoStore.Dono d,Supplier<T> action){
        store.donoConversa(id,d); // antes de revelar ocupação; UUID alheio sempre 404
        ReentrantLock lock=locks[Math.floorMod(id.hashCode(),locks.length)];
        if(!lock.tryLock())throw new RagApiException(HttpStatus.CONFLICT,"IA_CONVERSA_OCUPADA","Conversa ocupada. Tente novamente.");
        try{store.donoConversa(id,d);return action.get();}finally{lock.unlock();}
    }
    public Map<String,Object> perguntar(UUID id,String mensagem){
        var d=dono();String q=mensagem.strip();
        return operar(id,d,()->{
            store.conferirLimite(id,d);
            var r=rag.perguntarHistorico(id,q,store.memoria(id,d));
            if(!id.toString().equals(String.valueOf(r.get("conversa_id"))) || !(r.get("resposta") instanceof String) || !(r.get("tipo") instanceof String))
                throw new ServicoIAIndisponivelException("Contrato de resposta inválido");
            // Confirma a organização novamente antes de persistir após uma geração longa.
            if(!d.equals(dono()))throw new RagApiException(HttpStatus.FORBIDDEN,"IA_ACESSO_ALTERADO","Acesso à conversa alterado.");
            store.salvar(id,d,q,r);return r;
        });
    }
    public Map<String,Object> renomear(UUID id,String titulo){var d=dono();return operar(id,d,()->store.renomear(id,d,titulo.strip()));}
    public Map<String,Object> resetar(UUID id){var d=dono();return operar(id,d,()->store.resetar(id,d));}
    public void encerrar(UUID id){var d=dono();operar(id,d,()->{store.excluir(id,d);return true;});}
}
