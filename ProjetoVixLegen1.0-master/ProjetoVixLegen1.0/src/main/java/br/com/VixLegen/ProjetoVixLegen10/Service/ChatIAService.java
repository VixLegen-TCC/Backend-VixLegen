package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RagApiException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.ServicoIAIndisponivelException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.function.BiConsumer;

@Service
public class ChatIAService {
    private final RagHttpClient rag;
    private final TenantAccessService tenant;
    private final ChatHistoricoStore store;
    private final ChatProcessoContextoService processos;
    // Um único Spring Boot, conforme arquitetura local. Limita memória e evita corridas por conversa.
    private final ReentrantLock[] locks=new ReentrantLock[256];
    public ChatIAService(RagHttpClient rag,TenantAccessService tenant,ChatHistoricoStore store,ChatProcessoContextoService processos) {
        this.rag=rag;this.tenant=tenant;this.store=store;this.processos=processos;
        Arrays.setAll(locks,i->new ReentrantLock());
    }
    private ChatHistoricoStore.Dono dono(){return new ChatHistoricoStore.Dono(tenant.usuarioAtualId(),tenant.empresaAtualId());}
    public Map<String,Object> health(){dono();return rag.health();}
    public synchronized Map<String,Object> criar(){return store.criar(dono());}
    public List<Map<String,Object>> listar(){
        var d=dono();var out=new ArrayList<Map<String,Object>>();
        for(var c:store.listar(d)){
            var id=UUID.fromString((String)c.get("conversa_id"));Long pid=store.processoId(id,d);
            try{if(pid!=null)processos.metadados(pid);out.add(c);}
            catch(br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException ex){/* Processo removido, transferido ou sigiloso: não mostra título/histórico. */}
        }return out;
    }
    public Map<String,Object> detalhes(UUID id){var d=dono();Long pid=store.processoId(id,d);var meta=pid==null?null:processos.metadados(pid);var r=new LinkedHashMap<>(store.detalhes(id,d));r.put("processo",meta);return r;}
    public List<Map<String,Object>> processosDisponiveis(){dono();return processos.listar();}
    public Map<String,Object> vincularProcesso(UUID id,long pid){var d=dono();return operar(id,d,()->{var meta=processos.metadados(pid);store.vincularProcesso(id,d,pid);return meta;});}

    private <T> T operar(UUID id,ChatHistoricoStore.Dono d,Supplier<T> action){
        store.donoConversa(id,d); // antes de revelar ocupação; UUID alheio sempre 404
        ReentrantLock lock=locks[Math.floorMod(id.hashCode(),locks.length)];
        if(!lock.tryLock())throw new RagApiException(HttpStatus.CONFLICT,"IA_CONVERSA_OCUPADA","Conversa ocupada. Tente novamente.");
        try{store.donoConversa(id,d);return action.get();}finally{lock.unlock();}
    }
    public Map<String,Object> perguntar(UUID id,String mensagem){
        return perguntarInterno(id,mensagem,null);
    }
    public Map<String,Object> perguntarStream(UUID id,String mensagem,BiConsumer<String,Map<String,Object>> eventos){
        return perguntarInterno(id,mensagem,Objects.requireNonNull(eventos));
    }
    private Map<String,Object> perguntarInterno(UUID id,String mensagem,BiConsumer<String,Map<String,Object>> eventos){
        var d=dono();String q=mensagem.strip();
        return operar(id,d,()->{
            store.conferirLimite(id,d);
            Long pid=store.processoId(id,d);
            var anexo=pid==null?store.anexo(id,d):null;
            Map<String,Object> r;
            if(pid!=null){
                if(q.length()>1800)throw new RagApiException(HttpStatus.BAD_REQUEST,"IA_PROCESSO_PERGUNTA_LONGA","Pergunta sobre processo: até 1.800 caracteres.");
                var contexto=processos.contexto(pid);var memoria=store.memoriaProcesso(id,d,pid);
                r=eventos==null?rag.perguntarProcesso(id,q,contexto,memoria):rag.stream("/chat/processo/stream",Map.of("conversa_id",id.toString(),"mensagem",q,"processo",contexto,"historico",memoria),eventos);
                if(!Long.toString(pid).equals(String.valueOf(r.get("processo_id"))) || !"processo".equals(r.get("tipo")))throw new ServicoIAIndisponivelException("Contrato de processo inválido");
            }else if(anexo==null){
                var memoria=store.memoria(id,d);
                r=eventos==null?rag.perguntarHistorico(id,q,memoria):rag.stream("/chat/historico/stream",Map.of("conversa_id",id.toString(),"mensagem",q,"historico",memoria),eventos);
            }
            else {
                if(q.length()>1800)throw new RagApiException(HttpStatus.BAD_REQUEST,"IA_ANEXO_PERGUNTA_LONGA","Pergunta sobre anexo: até 1.800 caracteres.");
                String anexoId=(String)anexo.get("id");
                var memoria=store.memoriaAnexo(id,d,anexoId);
                r=eventos==null?rag.perguntarAnexo(id,q,anexoId,anexo.get("documento"),memoria):rag.stream("/chat/anexo/stream",Map.of("conversa_id",id.toString(),"mensagem",q,"anexo_id",anexoId,"documento",anexo.get("documento"),"historico",memoria),eventos);
                if(!anexoId.equals(r.get("anexo_id")) || !"documento".equals(r.get("tipo")))throw new ServicoIAIndisponivelException("Contrato de anexo inválido");
            }
            if(!id.toString().equals(String.valueOf(r.get("conversa_id"))) || !(r.get("resposta") instanceof String) || !(r.get("tipo") instanceof String))
                throw new ServicoIAIndisponivelException("Contrato de resposta inválido");
            // Confirma a organização novamente antes de persistir após uma geração longa.
            if(!d.equals(dono()))throw new RagApiException(HttpStatus.FORBIDDEN,"IA_ACESSO_ALTERADO","Acesso à conversa alterado.");
            if(pid!=null)processos.metadados(pid);
            store.salvar(id,d,q,r);return r;
        });
    }
    public Map<String,Object> renomear(UUID id,String titulo){var d=dono();return operar(id,d,()->{Long pid=store.processoId(id,d);if(pid!=null)processos.metadados(pid);return store.renomear(id,d,titulo.strip());});}
    public Map<String,Object> resetar(UUID id){var d=dono();return operar(id,d,()->store.resetar(id,d));}
    public void encerrar(UUID id){var d=dono();operar(id,d,()->{store.excluir(id,d);return true;});}
    public Map<String,Object> anexar(UUID id,String nome,String base64){
        var d=dono();return operar(id,d,()->{
            if(store.processoId(id,d)!=null)throw new RagApiException(HttpStatus.CONFLICT,"IA_CONTEXTO_EXISTENTE","Use outra conversa para anexar documentos.");
            if(store.metadadosAnexo(id,d)!=null)throw new RagApiException(HttpStatus.CONFLICT,"IA_ANEXO_EXISTENTE","Remova o anexo atual antes de adicionar outro.");
            if(nome.contains("/")||nome.contains("\\")||!nome.toLowerCase(Locale.ROOT).matches(".*\\.(pdf|docx|txt)$"))throw new RagApiException(HttpStatus.BAD_REQUEST,"IA_ANEXO_INVALIDO","Use PDF pesquisável, DOCX ou TXT.");
            var doc=rag.extrairAnexo(nome,base64);
            if(!nome.equals(doc.get("arquivo")))throw new ServicoIAIndisponivelException("Contrato de extração inválido");
            if(!d.equals(dono()))throw new RagApiException(HttpStatus.FORBIDDEN,"IA_ACESSO_ALTERADO","Acesso à conversa alterado.");
            return store.salvarAnexo(id,d,doc);
        });
    }
    public void removerAnexo(UUID id){var d=dono();operar(id,d,()->{store.removerAnexo(id,d);return true;});}
}
