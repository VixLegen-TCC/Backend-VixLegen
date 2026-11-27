package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RagApiException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;

@Repository
public class ChatHistoricoStore {
    private final JdbcTemplate db;
    private final JsonMapper json = JsonMapper.builder().build();
    private static final TypeReference<Map<String,Object>> MAP = new TypeReference<>() {};
    public record Dono(long usuario, long empresa) {}
    public ChatHistoricoStore(JdbcTemplate db) { this.db = db; }
    private static RagApiException ausente() {
        return new RagApiException(HttpStatus.NOT_FOUND,"IA_SESSAO_NAO_ENCONTRADA","Conversa não encontrada.");
    }
    public Map<String,Object> donoConversa(UUID id,Dono d) {
        var rows=db.query("SELECT id,titulo,atualizada_em FROM chat_ia_conversa WHERE id=? AND usuario_id=? AND empresa_id=?",
            (rs,n)->Map.<String,Object>of("conversa_id",rs.getString(1),"titulo",rs.getString(2),"atualizada_em",rs.getTimestamp(3).toInstant().toString()),id.toString(),d.usuario(),d.empresa());
        if(rows.isEmpty()) throw ausente(); return rows.getFirst();
    }
    public List<Map<String,Object>> listar(Dono d) {
        return db.query("SELECT id,titulo,atualizada_em FROM chat_ia_conversa WHERE usuario_id=? AND empresa_id=? ORDER BY atualizada_em DESC,id LIMIT 100",
            (rs,n)->Map.<String,Object>of("conversa_id",rs.getString(1),"titulo",rs.getString(2),"atualizada_em",rs.getTimestamp(3).toInstant().toString()),d.usuario(),d.empresa());
    }
    @Transactional
    public Map<String,Object> criar(Dono d) {
        var count=db.queryForObject("SELECT COUNT(*) FROM chat_ia_conversa WHERE usuario_id=? AND empresa_id=?",Long.class,d.usuario(),d.empresa());
        if(count!=null && count>=100)throw new RagApiException(HttpStatus.TOO_MANY_REQUESTS,"IA_LIMITE_SESSOES","Limite de 100 conversas. Exclua uma conversa.");
        UUID id=UUID.randomUUID();db.update("INSERT INTO chat_ia_conversa(id,usuario_id,empresa_id) VALUES(?,?,?)",id.toString(),d.usuario(),d.empresa());return donoConversa(id,d);
    }
    private List<Map<String,Object>> turnos(UUID id,Dono d,boolean memoria) {
        donoConversa(id,d);
        String sql="SELECT t.pergunta,t.resposta_json FROM chat_ia_turno t JOIN chat_ia_conversa c ON c.id=t.conversa_id WHERE c.id=? AND c.usuario_id=? AND c.empresa_id=?";
        sql += memoria ? " AND t.em_memoria=TRUE ORDER BY t.id DESC LIMIT 6" : " ORDER BY t.id ASC LIMIT 200";
        var rows=db.query(sql,(rs,n)->Map.<String,Object>of("pergunta",rs.getString(1),"resultado",json.readValue(rs.getString(2),MAP)),id.toString(),d.usuario(),d.empresa());
        if(memoria) Collections.reverse(rows);return rows;
    }
    public Map<String,Object> detalhes(UUID id,Dono d) {
        var result=new LinkedHashMap<>(donoConversa(id,d));result.put("turnos",turnos(id,d,false));result.put("anexo",metadadosAnexo(id,d));return result;
    }
    public Long processoId(UUID id,Dono d) {
        donoConversa(id,d);
        var rows=db.query("SELECT p.processo_id FROM chat_ia_processo p JOIN chat_ia_conversa c ON c.id=p.conversa_id WHERE c.id=? AND c.usuario_id=? AND c.empresa_id=?",(rs,n)->rs.getLong(1),id.toString(),d.usuario(),d.empresa());
        return rows.isEmpty()?null:rows.getFirst();
    }
    @Transactional
    public void vincularProcesso(UUID id,Dono d,long processoId) {
        donoConversa(id,d);
        if(processoId(id,d)!=null || metadadosAnexo(id,d)!=null)throw new RagApiException(HttpStatus.CONFLICT,"IA_CONTEXTO_EXISTENTE","Use uma nova conversa para selecionar o processo.");
        Long count=db.queryForObject("SELECT COUNT(*) FROM chat_ia_turno t JOIN chat_ia_conversa c ON c.id=t.conversa_id WHERE c.id=? AND c.usuario_id=? AND c.empresa_id=?",Long.class,id.toString(),d.usuario(),d.empresa());
        if(count==null || count!=0)throw new RagApiException(HttpStatus.CONFLICT,"IA_CONTEXTO_EXISTENTE","Use uma conversa sem mensagens.");
        if(db.update("INSERT INTO chat_ia_processo(conversa_id,processo_id) SELECT id,? FROM chat_ia_conversa WHERE id=? AND usuario_id=? AND empresa_id=?",processoId,id.toString(),d.usuario(),d.empresa())!=1)throw ausente();
    }
    public List<Map<String,Object>> memoriaProcesso(UUID id,Dono d,long processoId) {
        var out=new ArrayList<Map<String,Object>>();
        for(var t:turnos(id,d,true)) {
            @SuppressWarnings("unchecked") var r=(Map<String,Object>)t.get("resultado");
            if(!"processo".equals(r.get("tipo")) || !Long.toString(processoId).equals(String.valueOf(r.get("processo_id"))))continue;
            var turn=new LinkedHashMap<String,Object>();turn.put("pergunta",t.get("pergunta"));
            for(String k:List.of("resposta","tipo","consulta","referencia","pendencia"))turn.put(k,r.get(k));
            String a=String.valueOf(turn.get("resposta"));if(a.length()>50000)turn.put("resposta",a.substring(0,50000));out.add(turn);
        }return out;
    }
    public List<Map<String,Object>> memoria(UUID id,Dono d) {return memoriaAnexo(id,d,null);}
    public List<Map<String,Object>> memoriaAnexo(UUID id,Dono d,String anexoId) {
        var out=new ArrayList<Map<String,Object>>();
        for(var t:turnos(id,d,true)) {
            @SuppressWarnings("unchecked") var r=(Map<String,Object>)t.get("resultado");
            if(anexoId==null ? ("documento".equals(r.get("tipo")) || "processo".equals(r.get("tipo"))) : !anexoId.equals(r.get("anexo_id"))) continue;
            var turn=new LinkedHashMap<String,Object>();turn.put("pergunta",t.get("pergunta"));
            for(String k:List.of("resposta","tipo","consulta","referencia","pendencia"))turn.put(k,r.get(k));
            // O motor usa somente um recorte de respostas anteriores; preserva os metadados integrais.
            String a=String.valueOf(turn.get("resposta"));if(a.length()>50000)turn.put("resposta",a.substring(0,50000));
            out.add(turn);
        }return out;
    }
    public void conferirLimite(UUID id,Dono d) {
        donoConversa(id,d);
        Long count=db.queryForObject("SELECT COUNT(*) FROM chat_ia_turno t JOIN chat_ia_conversa c ON c.id=t.conversa_id WHERE c.id=? AND c.usuario_id=? AND c.empresa_id=?",Long.class,id.toString(),d.usuario(),d.empresa());
        if(count!=null&&count>=200)throw new RagApiException(HttpStatus.CONFLICT,"IA_LIMITE_TURNOS","Limite de 200 turnos. Crie uma nova conversa.");
    }
    @Transactional
    public void salvar(UUID id,Dono d,String pergunta,Map<String,Object> resultado) {
        conferirLimite(id,d);
        int n=db.update("INSERT INTO chat_ia_turno(conversa_id,pergunta,resposta_json,em_memoria) SELECT id,?,?,? FROM chat_ia_conversa WHERE id=? AND usuario_id=? AND empresa_id=?",pergunta,json.writeValueAsString(resultado),!"interrompida".equals(resultado.get("status_resposta")),id.toString(),d.usuario(),d.empresa());
        if(n!=1)throw ausente();
        String titulo=pergunta.replaceAll("\\s+"," ");titulo=titulo.substring(0,Math.min(55,titulo.length()));
        db.update("UPDATE chat_ia_conversa SET titulo=CASE WHEN titulo='Nova conversa' THEN ? ELSE titulo END, atualizada_em=CURRENT_TIMESTAMP(6) WHERE id=? AND usuario_id=? AND empresa_id=?",titulo,id.toString(),d.usuario(),d.empresa());
    }
    @Transactional
    public Map<String,Object> renomear(UUID id,Dono d,String titulo) {
        donoConversa(id,d);db.update("UPDATE chat_ia_conversa SET titulo=?,atualizada_em=CURRENT_TIMESTAMP(6) WHERE id=? AND usuario_id=? AND empresa_id=?",titulo,id.toString(),d.usuario(),d.empresa());return donoConversa(id,d);
    }
    @Transactional
    public Map<String,Object> resetar(UUID id,Dono d) {
        donoConversa(id,d);removerAnexo(id,d);db.update("DELETE t FROM chat_ia_turno t JOIN chat_ia_conversa c ON c.id=t.conversa_id WHERE c.id=? AND c.usuario_id=? AND c.empresa_id=?",id.toString(),d.usuario(),d.empresa());return renomear(id,d,"Nova conversa");
    }
    @Transactional
    public void excluir(UUID id,Dono d) {
        if(db.update("DELETE FROM chat_ia_conversa WHERE id=? AND usuario_id=? AND empresa_id=?",id.toString(),d.usuario(),d.empresa())!=1)throw ausente();
    }
    public Map<String,Object> metadadosAnexo(UUID id,Dono d) {
        donoConversa(id,d);
        var rows=db.query("SELECT a.id,a.arquivo,a.caracteres,a.trechos FROM chat_ia_anexo a JOIN chat_ia_conversa c ON c.id=a.conversa_id WHERE c.id=? AND c.usuario_id=? AND c.empresa_id=?",
            (rs,n)->Map.<String,Object>of("id",rs.getString(1),"arquivo",rs.getString(2),"caracteres",rs.getInt(3),"trechos",rs.getInt(4)),id.toString(),d.usuario(),d.empresa());
        return rows.isEmpty()?null:rows.getFirst();
    }
    public Map<String,Object> anexo(UUID id,Dono d) {
        donoConversa(id,d);
        var rows=db.query("SELECT a.id,a.documento_json FROM chat_ia_anexo a JOIN chat_ia_conversa c ON c.id=a.conversa_id WHERE c.id=? AND c.usuario_id=? AND c.empresa_id=?",
            (rs,n)->Map.<String,Object>of("id",rs.getString(1),"documento",json.readValue(rs.getString(2),MAP)),id.toString(),d.usuario(),d.empresa());
        return rows.isEmpty()?null:rows.getFirst();
    }
    @Transactional
    public Map<String,Object> salvarAnexo(UUID id,Dono d,Map<String,Object> doc) {
        donoConversa(id,d);
        if(metadadosAnexo(id,d)!=null)throw new RagApiException(HttpStatus.CONFLICT,"IA_ANEXO_EXISTENTE","Remova o anexo atual antes de adicionar outro.");
        if(!(doc.get("arquivo") instanceof String arquivo) || !(doc.get("caracteres") instanceof Number chars) || !(doc.get("partes") instanceof List<?> partes) || partes.isEmpty() || partes.size()>200 || chars.intValue()<1 || chars.intValue()>100000)
            throw new br.com.VixLegen.ProjetoVixLegen10.Exception.ServicoIAIndisponivelException("Contrato de extração inválido");
        int n=db.update("INSERT INTO chat_ia_anexo(conversa_id,id,arquivo,documento_json,caracteres,trechos) SELECT id,?,?,?,?,? FROM chat_ia_conversa WHERE id=? AND usuario_id=? AND empresa_id=?",UUID.randomUUID().toString(),arquivo,json.writeValueAsString(doc),chars.intValue(),partes.size(),id.toString(),d.usuario(),d.empresa());
        if(n!=1)throw ausente();return metadadosAnexo(id,d);
    }
    @Transactional
    public void removerAnexo(UUID id,Dono d) {
        donoConversa(id,d);db.update("DELETE a FROM chat_ia_anexo a JOIN chat_ia_conversa c ON c.id=a.conversa_id WHERE c.id=? AND c.usuario_id=? AND c.empresa_id=?",id.toString(),d.usuario(),d.empresa());
    }
}
