package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.*;
import br.com.VixLegen.ProjetoVixLegen10.Repository.*;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Regressão de IDOR: nenhum ID de outra empresa pode ser consultado ou excluído.
 * Mock de repositório garante que as consultas tragam explicitamente empresaId.
 */
class IsolamentoOrganizacaoTest {
    private final TenantAccessService tenant = mock(TenantAccessService.class);
    private final Long empresa = 20L;

    @Test
    void tarefasConsultamApenasEmpresaAtual() {
        TarefaRepository repo = mock(TarefaRepository.class);
        TarefaService svc = new TarefaService(repo, mock(ProcessoJuridicoRepository.class),
                mock(UsuarioRepository.class), tenant);
        when(tenant.empresaAtualId()).thenReturn(empresa);
        when(repo.findByProcessoClienteEmpresaOrganizacaoIdEmpresa(empresa)).thenReturn(List.of());
        assertTrue(svc.listarTodos().isEmpty());
        assertThrows(RecursoNaoEncontradoException.class, () -> svc.buscarPorId(900L));
        assertThrows(RecursoNaoEncontradoException.class, () -> svc.excluir(900L));
        verify(repo).findByIdTarefaAndProcessoClienteEmpresaOrganizacaoIdEmpresa(900L, empresa);
        verify(repo, never()).findById(anyLong());
        verify(repo, never()).delete(any());
    }

    @Test
    void documentosNaoPodemSerLidosNemExcluidosPorIdExterno() {
        DocumentoJuridicoRepository repo = mock(DocumentoJuridicoRepository.class);
        DocumentoJuridicoService svc = new DocumentoJuridicoService(repo,
                mock(ProcessoJuridicoRepository.class), mock(CategoriaDocumentoRepository.class), tenant);
        when(tenant.empresaAtualId()).thenReturn(empresa);
        assertThrows(RecursoNaoEncontradoException.class, () -> svc.buscarPorId(901L));
        assertThrows(RecursoNaoEncontradoException.class, () -> svc.excluir(901L));
        verify(repo).findByIdDocumentoAndProcessoClienteEmpresaOrganizacaoIdEmpresa(901L, empresa);
        verify(repo, never()).findById(anyLong());
        verify(repo, never()).delete(any());
    }

    @Test
    void iaNaoPodeSerModificadaEmOutraEmpresa() {
        IARepository repo = mock(IARepository.class);
        IAService svc = new IAService(repo, mock(ProcessoJuridicoRepository.class), tenant);
        when(tenant.empresaAtualId()).thenReturn(empresa);
        assertThrows(RecursoNaoEncontradoException.class, () -> svc.buscarPorId(902L));
        assertThrows(RecursoNaoEncontradoException.class, () -> svc.excluir(902L));
        verify(repo, atLeastOnce()).findByIdAnaliseAndProcessoClienteEmpresaOrganizacaoIdEmpresa(902L, empresa);
        verify(repo, never()).findById(anyLong());
        verify(repo, never()).delete(any());
    }

    @Test
    void partesMovimentacoesEClassificacoesNaoAcessamOutroEscritorio() {
        when(tenant.empresaAtualId()).thenReturn(empresa);
        ParteEnvolvidaRepository partes = mock(ParteEnvolvidaRepository.class);
        MovimentacaoProcessualRepository movimentos = mock(MovimentacaoProcessualRepository.class);
        ClassificacaoProcessoRepository classificacoes = mock(ClassificacaoProcessoRepository.class);

        ParteEnvolvidaService svcParte = new ParteEnvolvidaService(partes,
                mock(ProcessoJuridicoRepository.class), tenant);
        MovimentacaoProcessualService svcMov = new MovimentacaoProcessualService(movimentos,
                mock(ProcessoJuridicoRepository.class), tenant);
        ClassificacaoProcessoService svcClass = new ClassificacaoProcessoService(classificacoes,
                mock(ProcessoJuridicoRepository.class), tenant);

        assertThrows(RecursoNaoEncontradoException.class, () -> svcParte.buscarPorId(903L));
        assertThrows(RecursoNaoEncontradoException.class, () -> svcMov.buscarPorId(904L));
        assertThrows(RecursoNaoEncontradoException.class, () -> svcClass.buscarPorId(905L));
        assertThrows(RecursoNaoEncontradoException.class, () -> svcParte.excluir(903L));
        assertThrows(RecursoNaoEncontradoException.class, () -> svcMov.excluir(904L));
        assertThrows(RecursoNaoEncontradoException.class, () -> svcClass.excluir(905L));

        verify(partes, never()).findById(anyLong());
        verify(movimentos, never()).findById(anyLong());
        verify(classificacoes, never()).findById(anyLong());
        verify(partes, never()).delete(any());
        verify(movimentos, never()).delete(any());
        verify(classificacoes, never()).delete(any());
    }

    @Test
    void colaboradorSemprePertenceAoEscritorioAtual() {
        ColaboradorRepository repo = mock(ColaboradorRepository.class);
        ColaboradorService svc = new ColaboradorService(repo, tenant);
        when(tenant.empresaAtualId()).thenReturn(empresa);
        Empresa empresaAtual = new Empresa();
        empresaAtual.setIdEmpresa(empresa);
        when(tenant.empresaAtual()).thenReturn(empresaAtual);

        Colaborador criado = new Colaborador();
        svc.cadastrar(criado);
        assertSame(empresaAtual, criado.getEmpresaOrganizacao());
        assertThrows(RecursoNaoEncontradoException.class, () -> svc.buscarPorId(999L));
        verify(repo).findByIdColaboradorAndEmpresaOrganizacaoIdEmpresa(999L, empresa);
    }

    @Test
    void consultaAdministrativaDeUsuariosEhEscopada() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        UsuarioService svc = new UsuarioService(repo, mock(CategoriaRepository.class),
                mock(EmpresaRepository.class), mock(org.springframework.security.crypto.password.PasswordEncoder.class),
                tenant, "Advogado Júnior");
        when(tenant.empresaAtualId()).thenReturn(empresa);
        assertTrue(svc.listarTodos().isEmpty());
        assertThrows(RecursoNaoEncontradoException.class, () -> svc.buscarPorId(900L));
        verify(repo).findByEmpresaOrganizacaoIdEmpresa(empresa);
        verify(tenant).usuarioDaEmpresa(900L);
        verify(repo, never()).findAll();
    }
}
