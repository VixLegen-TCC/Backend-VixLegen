package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.util.function.BiConsumer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatIAStreamingServiceTest {
    RagHttpClient rag; TenantAccessService tenant; ChatHistoricoStore store; ChatIAService service;
    final UUID id=UUID.randomUUID();
    final ChatHistoricoStore.Dono dono=new ChatHistoricoStore.Dono(7L,9L);
    final Map<String,Object> resposta=Map.of("conversa_id",id.toString(),"resposta","Olá","tipo","geral");
    final BiConsumer<String,Map<String,Object>> eventos=(event,data)->{};
    @BeforeEach void setup(){
        rag=mock(RagHttpClient.class);tenant=mock(TenantAccessService.class);store=mock(ChatHistoricoStore.class);
        when(tenant.usuarioAtualId()).thenReturn(7L);when(tenant.empresaAtualId()).thenReturn(9L);
        when(store.donoConversa(id,dono)).thenReturn(Map.of("conversa_id",id.toString()));
        when(store.memoria(id,dono)).thenReturn(List.of());
        service=new ChatIAService(rag,tenant,store,mock(ChatProcessoContextoService.class));
    }
    @Test void salvaSomenteDepoisDoResultado(){
        when(rag.stream(eq("/chat/historico/stream"),anyMap(),same(eventos))).thenAnswer(call->{
            verify(store,never()).salvar(any(),any(),any(),any());return resposta;
        });
        assertEquals(resposta,service.perguntarStream(id,"q",eventos));verify(store).salvar(id,dono,"q",resposta);
    }
    @Test void falhaSemResultadoNaoSalva(){
        when(rag.stream(anyString(),anyMap(),same(eventos))).thenThrow(new ServicoIAIndisponivelException("stream interrompido"));
        assertThrows(ServicoIAIndisponivelException.class,()->service.perguntarStream(id,"q",eventos));verify(store,never()).salvar(any(),any(),any(),any());
    }
    @Test void uuidAlheioNaoAbreStream(){
        when(store.donoConversa(id,dono)).thenThrow(new RagApiException(HttpStatus.NOT_FOUND,"IA_SESSAO_NAO_ENCONTRADA","Conversa não encontrada."));
        assertThrows(RagApiException.class,()->service.perguntarStream(id,"q",eventos));verifyNoInteractions(rag);
    }
    @Test void mudancaDeEmpresaNaoSalva(){
        when(tenant.empresaAtualId()).thenReturn(9L,10L);
        when(rag.stream(anyString(),anyMap(),same(eventos))).thenReturn(resposta);
        assertThrows(RagApiException.class,()->service.perguntarStream(id,"q",eventos));verify(store,never()).salvar(any(),any(),any(),any());
    }
}
