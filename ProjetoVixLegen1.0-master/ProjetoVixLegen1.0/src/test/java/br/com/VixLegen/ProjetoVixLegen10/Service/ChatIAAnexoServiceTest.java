package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RagApiException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.ServicoIAIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatIAAnexoServiceTest {
    RagHttpClient rag; TenantAccessService tenant; ChatHistoricoStore store; ChatIAService service;
    final UUID id=UUID.randomUUID(); final String aid=UUID.randomUUID().toString();
    final ChatHistoricoStore.Dono dono=new ChatHistoricoStore.Dono(7L,9L);
    final Map<String,Object> doc=Map.of("arquivo","contrato.txt","partes",List.of(Map.of("trecho","Contrato de teste.")),"caracteres",18);
    @BeforeEach void setup(){
        rag=mock(RagHttpClient.class);tenant=mock(TenantAccessService.class);store=mock(ChatHistoricoStore.class);
        when(tenant.usuarioAtualId()).thenReturn(7L);when(tenant.empresaAtualId()).thenReturn(9L);
        when(store.donoConversa(id,dono)).thenReturn(Map.of("conversa_id",id.toString()));
        service=new ChatIAService(rag,tenant,store,mock(ChatProcessoContextoService.class));
    }
    @Test void uploadAutorizadoSalvaSomenteTextoExtraido(){
        when(rag.extrairAnexo("contrato.txt","YQ==")).thenReturn(doc);
        var meta=Map.<String,Object>of("id",aid,"arquivo","contrato.txt");when(store.salvarAnexo(id,dono,doc)).thenReturn(meta);
        assertEquals(meta,service.anexar(id,"contrato.txt","YQ=="));verify(store).salvarAnexo(id,dono,doc);
    }
    @Test void uuidAlheioNaoChegaAoRag(){
        when(store.donoConversa(id,dono)).thenThrow(new RagApiException(HttpStatus.NOT_FOUND,"IA_SESSAO_NAO_ENCONTRADA","Conversa não encontrada."));
        assertThrows(RagApiException.class,()->service.anexar(id,"contrato.txt","YQ=="));
        assertThrows(RagApiException.class,()->service.perguntar(id,"Resuma"));assertThrows(RagApiException.class,()->service.removerAnexo(id));verifyNoInteractions(rag);
    }
    @Test void anexoAtivoNaoConsultaBaseGlobal(){
        when(store.anexo(id,dono)).thenReturn(Map.of("id",aid,"documento",doc));
        when(store.memoriaAnexo(id,dono,aid)).thenReturn(List.of());
        var r=Map.<String,Object>of("conversa_id",id.toString(),"anexo_id",aid,"tipo","documento","resposta","Resposta privada");
        when(rag.perguntarAnexo(id,"Resuma",aid,doc,List.of())).thenReturn(r);
        assertEquals(r,service.perguntar(id,"Resuma"));verify(store).salvar(id,dono,"Resuma",r);verify(rag,never()).perguntarHistorico(any(),any(),any());
    }
    @Test void anexoErradoNaoSalva(){
        when(store.anexo(id,dono)).thenReturn(Map.of("id",aid,"documento",doc));when(store.memoriaAnexo(id,dono,aid)).thenReturn(List.of());
        when(rag.perguntarAnexo(id,"Resuma",aid,doc,List.of())).thenReturn(Map.of("conversa_id",id.toString(),"anexo_id",UUID.randomUUID().toString(),"tipo","documento","resposta","r"));
        assertThrows(ServicoIAIndisponivelException.class,()->service.perguntar(id,"Resuma"));verify(store,never()).salvar(any(),any(),any(),any());
    }
    @Test void naoSobrescreveAnexoExistente(){
        when(store.metadadosAnexo(id,dono)).thenReturn(Map.of("id",aid));assertThrows(RagApiException.class,()->service.anexar(id,"contrato.txt","YQ=="));verifyNoInteractions(rag);
    }
}
