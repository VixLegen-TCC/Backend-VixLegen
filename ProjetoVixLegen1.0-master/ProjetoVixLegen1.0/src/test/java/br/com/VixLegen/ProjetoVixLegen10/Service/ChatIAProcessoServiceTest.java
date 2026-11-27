package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatIAProcessoServiceTest {
    RagHttpClient rag; TenantAccessService tenant; ChatHistoricoStore store; ChatProcessoContextoService context; ChatIAService service;
    final UUID id=UUID.randomUUID(); final ChatHistoricoStore.Dono dono=new ChatHistoricoStore.Dono(7L,9L);
    final Map<String,Object> meta=Map.of("processo_id","42","numero","00042");
    final Map<String,Object> ctx=Map.of("processo_id",42L,"numero","00042");
    @BeforeEach void setup(){
        rag=mock(RagHttpClient.class);tenant=mock(TenantAccessService.class);store=mock(ChatHistoricoStore.class);context=mock(ChatProcessoContextoService.class);
        when(tenant.usuarioAtualId()).thenReturn(7L);when(tenant.empresaAtualId()).thenReturn(9L);
        when(store.donoConversa(id,dono)).thenReturn(Map.of("conversa_id",id.toString()));
        when(context.metadados(42L)).thenReturn(meta);when(context.contexto(42L)).thenReturn(ctx);
        service=new ChatIAService(rag,tenant,store,context);
    }
    @Test void vinculaSomenteProcessoAutorizado(){assertEquals(meta,service.vincularProcesso(id,42L));verify(store).vincularProcesso(id,dono,42L);}
    @Test void processoAlheioNaoVincula(){when(context.metadados(42L)).thenThrow(new RecursoNaoEncontradoException("ausente"));assertThrows(RecursoNaoEncontradoException.class,()->service.vincularProcesso(id,42L));verify(store,never()).vincularProcesso(any(),any(),anyLong());verifyNoInteractions(rag);}
    @Test void perguntaEncaminhaDadosAtuais(){when(store.processoId(id,dono)).thenReturn(42L);when(store.memoriaProcesso(id,dono,42L)).thenReturn(List.of());var r=Map.<String,Object>of("tipo","processo","resposta","r","conversa_id",id.toString(),"processo_id",42L);when(rag.perguntarProcesso(id,"q",ctx,List.of())).thenReturn(r);assertEquals(r,service.perguntar(id,"q"));verify(store).salvar(id,dono,"q",r);verify(rag,never()).perguntarHistorico(any(),any(),any());verify(rag,never()).perguntarAnexo(any(),any(),any(),any(),any());}
    @Test void processoRevogadoNaoMostraHistorico(){when(store.processoId(id,dono)).thenReturn(42L);when(context.metadados(42L)).thenThrow(new RecursoNaoEncontradoException("ausente"));assertThrows(RecursoNaoEncontradoException.class,()->service.detalhes(id));verify(store,never()).detalhes(any(),any());}
    @Test void processoRevogadoDuranteGeracaoNaoSalva(){when(store.processoId(id,dono)).thenReturn(42L);when(store.memoriaProcesso(id,dono,42L)).thenReturn(List.of());when(rag.perguntarProcesso(id,"q",ctx,List.of())).thenReturn(Map.of("tipo","processo","resposta","r","conversa_id",id.toString(),"processo_id",42L));when(context.metadados(42L)).thenThrow(new RecursoNaoEncontradoException("ausente"));assertThrows(RecursoNaoEncontradoException.class,()->service.perguntar(id,"q"));verify(store,never()).salvar(any(),any(),any(),any());}
    @Test void anexoBloqueadoEmConversaDeProcesso(){when(store.processoId(id,dono)).thenReturn(42L);assertThrows(RagApiException.class,()->service.anexar(id,"teste.txt","YQ=="));verifyNoInteractions(rag);}
}
