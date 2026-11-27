package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RagApiException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.ServicoIAIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatIAHistoricoServiceTest {
    RagHttpClient rag; TenantAccessService tenant; ChatHistoricoStore store; ChatIAService service;
    final UUID id=UUID.randomUUID();
    final ChatHistoricoStore.Dono dono=new ChatHistoricoStore.Dono(7L, 9L);
    @BeforeEach void setup(){
        rag=mock(RagHttpClient.class);tenant=mock(TenantAccessService.class);store=mock(ChatHistoricoStore.class);
        when(tenant.usuarioAtualId()).thenReturn(7L);when(tenant.empresaAtualId()).thenReturn(9L);
        when(store.donoConversa(id,dono)).thenReturn(Map.of("conversa_id",id.toString()));
        service=new ChatIAService(rag,tenant,store);
    }
    @Test void restauraSomenteHistoricoDoDonoESalva(){
        List<Map<String,Object>> h=List.of(Map.of("pergunta","Art. 5º","resposta","texto"));
        var resposta=Map.<String,Object>of("conversa_id",id.toString(),"resposta","inciso II","tipo","juridico");
        when(store.memoria(id,dono)).thenReturn(h);when(rag.perguntarHistorico(id,"E o inciso II?",h)).thenReturn(resposta);
        assertEquals(resposta,service.perguntar(id," E o inciso II? "));
        verify(store).salvar(id,dono,"E o inciso II?",resposta);
    }
    @Test void uuidAlheioNaoChegaAoPython(){
        when(store.donoConversa(id,dono)).thenThrow(new RagApiException(HttpStatus.NOT_FOUND,"IA_SESSAO_NAO_ENCONTRADA","Conversa não encontrada."));
        assertEquals(HttpStatus.NOT_FOUND,assertThrows(RagApiException.class,()->service.perguntar(id,"pergunta")).status());
        verifyNoInteractions(rag);verify(store,never()).salvar(any(),any(),any(),any());
    }
    @Test void respostaInvalidaNaoSalva(){
        when(store.memoria(id,dono)).thenReturn(List.of());when(rag.perguntarHistorico(id,"q",List.of())).thenReturn(Map.of("resposta","sem id"));
        assertThrows(ServicoIAIndisponivelException.class,()->service.perguntar(id,"q"));verify(store,never()).salvar(any(),any(),any(),any());
    }
    @Test void mudancaDeEmpresaDuranteGeracaoNaoSalva(){
        when(tenant.empresaAtualId()).thenReturn(9L,10L);when(store.memoria(id,dono)).thenReturn(List.of());
        when(rag.perguntarHistorico(id,"q",List.of())).thenReturn(Map.of("conversa_id",id.toString(),"resposta","r","tipo","geral"));
        assertEquals(HttpStatus.FORBIDDEN,assertThrows(RagApiException.class,()->service.perguntar(id,"q")).status());verify(store,never()).salvar(any(),any(),any(),any());
    }
    @Test void excluiSomenteConversaDoDono(){service.encerrar(id);verify(store).excluir(id,dono);verifyNoInteractions(rag);}
}
