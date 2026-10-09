package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusTarefa;
import br.com.VixLegen.ProjetoVixLegen10.Model.*;
import br.com.VixLegen.ProjetoVixLegen10.Repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes isolados: não dependem do MySQL local.
 * Relógio fixo para tornar os marcos de vencimento determinísticos.
 */
class AlertaPrazoServiceTest {
    @Test
    void registraPrazoDeTarefaEProcessoSemDuplicar() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        TarefaRepository tarefas = mock(TarefaRepository.class);
        ProcessoJuridicoRepository processos = mock(ProcessoJuridicoRepository.class);
        NotificacaoRepository notificacoes = mock(NotificacaoRepository.class);
        Clock relogio = Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

        Empresa empresa = new Empresa();
        empresa.setIdEmpresa(2L);
        Usuario responsavel = new Usuario();
        responsavel.setIdUsuario(10L);
        responsavel.setAtivo(true);
        responsavel.setEmpresaOrganizacao(empresa);

        Cliente cliente = new Cliente();
        cliente.setEmpresaOrganizacao(empresa);
        cliente.setUsuarioResponsavel(responsavel);

        ProcessoJuridico processo = new ProcessoJuridico();
        processo.setIdProcesso(99L);
        processo.setCliente(cliente);
        processo.setNumeroProcesso("0000000-00.2026.8.26.0000");
        processo.setPrazoProcessual(LocalDate.of(2026, 10, 12)); // 3 dias

        Tarefa tarefa = new Tarefa();
        tarefa.setIdTarefa(70L);
        tarefa.setProcesso(processo);
        tarefa.setUsuarioResponsavel(responsavel);
        tarefa.setTipoTarefa("Preparar contestação");
        tarefa.setPrazo(LocalDateTime.of(2026, 10, 10, 18, 0)); // 1 dia
        tarefa.setStatus(StatusTarefa.PENDENTE);

        when(usuarios.findById(10L)).thenReturn(Optional.of(responsavel));
        when(tarefas.findByUsuarioResponsavelIdUsuario(10L)).thenReturn(List.of(tarefa));
        when(processos.findByClienteUsuarioResponsavelIdUsuario(10L)).thenReturn(List.of(processo));

        Set<String> chaves = new HashSet<>();
        when(notificacoes.existsByChaveAlerta(any())).thenAnswer(a -> chaves.contains(a.getArgument(0)));
        when(notificacoes.saveAndFlush(any(Notificacao.class))).thenAnswer(a -> {
            Notificacao n = a.getArgument(0);
            chaves.add(n.getChaveAlerta());
            return n;
        });

        AlertaPrazoService servico = new AlertaPrazoService(usuarios, tarefas, processos, notificacoes, relogio);
        servico.gerarParaUsuario(10L);
        servico.gerarParaUsuario(10L); // consultar central duas vezes não recria avisos

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacoes, times(2)).saveAndFlush(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(n ->
                "TAREFA".equals(n.getTipoReferencia()) && "1_DIA".equals(n.getEtapaAlerta())));
        assertTrue(captor.getAllValues().stream().anyMatch(n ->
                "PROCESSO".equals(n.getTipoReferencia()) && "3_DIAS".equals(n.getEtapaAlerta())));
    }

    @Test
    void naoEnviaAvisoDeTarefaDeOutraEmpresa() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        TarefaRepository tarefas = mock(TarefaRepository.class);
        ProcessoJuridicoRepository processos = mock(ProcessoJuridicoRepository.class);
        NotificacaoRepository notificacoes = mock(NotificacaoRepository.class);
        Empresa empresa = new Empresa();
        empresa.setIdEmpresa(1L);
        Empresa outra = new Empresa();
        outra.setIdEmpresa(2L);
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setAtivo(true);
        usuario.setEmpresaOrganizacao(empresa);
        Cliente cliente = new Cliente();
        cliente.setEmpresaOrganizacao(outra);
        ProcessoJuridico processo = new ProcessoJuridico();
        processo.setCliente(cliente);
        Tarefa tarefa = new Tarefa();
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setPrazo(LocalDateTime.of(2026, 10, 10, 12, 0));
        tarefa.setProcesso(processo);
        when(usuarios.findById(1L)).thenReturn(Optional.of(usuario));
        when(tarefas.findByUsuarioResponsavelIdUsuario(1L)).thenReturn(List.of(tarefa));
        when(processos.findByClienteUsuarioResponsavelIdUsuario(1L)).thenReturn(List.of());
        new AlertaPrazoService(usuarios, tarefas, processos, notificacoes,
                Clock.fixed(Instant.parse("2026-10-09T12:00:00Z"), ZoneId.of("America/Sao_Paulo")))
                .gerarParaUsuario(1L);
        verify(notificacoes, never()).saveAndFlush(any(Notificacao.class));
    }
}
