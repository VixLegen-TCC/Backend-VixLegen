package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Model.Tarefa;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.TarefaRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/** Exclusão é autorizada somente após resolver o processo no escopo da organização. */
class TarefaExclusaoServiceTest {
    private final TarefaRepository tarefas = mock(TarefaRepository.class);
    private final ProcessoJuridicoRepository processos = mock(ProcessoJuridicoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final TenantAccessService tenant = mock(TenantAccessService.class);
    private final TarefaService service = new TarefaService(tarefas, processos, usuarios, tenant);

    @Test
    void excluiTarefaDaEmpresaAutorizada() {
        ProcessoJuridico processo = new ProcessoJuridico();
        processo.setIdProcesso(20L);
        Tarefa tarefa = new Tarefa();
        tarefa.setIdTarefa(10L);
        tarefa.setProcesso(processo);
        when(tarefas.findById(10L)).thenReturn(Optional.of(tarefa));
        when(tenant.processo(20L)).thenReturn(processo);

        service.excluir(10L);

        verify(tenant).processo(20L);
        verify(tarefas).delete(tarefa);
    }

    @Test
    void naoExcluiTarefaDeOutraEmpresa() {
        ProcessoJuridico processo = new ProcessoJuridico();
        processo.setIdProcesso(99L);
        Tarefa tarefa = new Tarefa();
        tarefa.setProcesso(processo);
        when(tarefas.findById(10L)).thenReturn(Optional.of(tarefa));
        when(tenant.processo(99L)).thenThrow(new RecursoNaoEncontradoException("Processo não encontrado nesta organização"));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.excluir(10L));

        verify(tarefas, never()).delete(any(Tarefa.class));
    }
}
