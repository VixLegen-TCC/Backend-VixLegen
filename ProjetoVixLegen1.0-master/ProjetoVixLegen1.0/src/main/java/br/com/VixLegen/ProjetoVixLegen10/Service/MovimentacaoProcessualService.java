package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.MovimentacaoProcessual;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Repository.MovimentacaoProcessualRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimentacaoProcessualService {

    private final MovimentacaoProcessualRepository movimentacaoRepository;
    private final ProcessoJuridicoRepository processoRepository;
    private final TenantAccessService tenant;

    public MovimentacaoProcessualService(
            MovimentacaoProcessualRepository movimentacaoRepository,
            ProcessoJuridicoRepository processoRepository,
            TenantAccessService tenant) {

        this.movimentacaoRepository = movimentacaoRepository;
        this.processoRepository = processoRepository;
        this.tenant = tenant;
    }

    // CREATE
    public MovimentacaoProcessual cadastrar(
            MovimentacaoProcessual movimentacao) {

        ProcessoJuridico processo = tenant.processo(movimentacao.getProcesso().getIdProcesso());

        movimentacao.setProcesso(processo);
        movimentacao.setIdMovimentacao(null); // POST força novo registro.

        return movimentacaoRepository.save(movimentacao);
    }

    // READ
    public List<MovimentacaoProcessual> listarTodos() {
        return movimentacaoRepository.findByProcessoClienteEmpresaOrganizacaoIdEmpresa(tenant.empresaAtualId());
    }

    // READ por ID
    public MovimentacaoProcessual buscarPorId(Long id) {

        return movimentacaoRepository.findByIdMovimentacaoAndProcessoClienteEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Movimentação processual não encontrada"));
    }

    // UPDATE
    public MovimentacaoProcessual atualizar(
            Long id,
            MovimentacaoProcessual movimentacao) {

        MovimentacaoProcessual existente =
                movimentacaoRepository.findByIdMovimentacaoAndProcessoClienteEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Movimentação processual não encontrada"));

        ProcessoJuridico processo = tenant.processo(movimentacao.getProcesso().getIdProcesso());

        existente.setData(movimentacao.getData());
        existente.setDescricao(movimentacao.getDescricao());
        existente.setProcesso(processo);

        return movimentacaoRepository.save(existente);
    }

    // DELETE
    public void excluir(Long id) {

        MovimentacaoProcessual movimentacao =
                movimentacaoRepository.findByIdMovimentacaoAndProcessoClienteEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Movimentação processual não encontrada"));

        movimentacaoRepository.delete(movimentacao);
    }

    public MovimentacaoProcessual registrar(
            Long idProcesso,
            MovimentacaoProcessual movimentacao) {

        ProcessoJuridico processo = tenant.processo(idProcesso);

        movimentacao.setProcesso(processo);
        movimentacao.setIdMovimentacao(null); // POST força novo registro.

        return movimentacaoRepository.save(movimentacao);
    }
}