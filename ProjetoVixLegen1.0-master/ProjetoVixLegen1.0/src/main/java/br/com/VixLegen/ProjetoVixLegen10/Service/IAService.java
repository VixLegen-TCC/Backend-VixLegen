package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.IA;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Repository.IARepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IAService {

    private final IARepository analiseRepository;
    private final ProcessoJuridicoRepository processoRepository;
    private final TenantAccessService tenant;

    public IAService(
            IARepository analiseRepository,
            ProcessoJuridicoRepository processoRepository,
            TenantAccessService tenant) {

        this.analiseRepository = analiseRepository;
        this.processoRepository = processoRepository;
        this.tenant = tenant;
    }

    // CREATE
    public IA cadastrar(IA analise) {

        ProcessoJuridico processo = tenant.processo(analise.getProcesso().getIdProcesso());

        analise.setProcesso(processo);

        return analiseRepository.save(analise);
    }

    // READ
    public List<IA> listarTodos() {
        return analiseRepository.findByProcessoClienteEmpresaOrganizacaoIdEmpresa(tenant.empresaAtualId());
    }

    // READ por ID
    public IA buscarPorId(Long id) {

        return analiseRepository.findByIdAnaliseAndProcessoClienteEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Análise de IA não encontrada"));
    }

    // UPDATE
    public IA atualizar(
            Long id,
            IA analise) {

        IA existente = analiseRepository.findByIdAnaliseAndProcessoClienteEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Análise de IA não encontrada"));

        ProcessoJuridico processo = tenant.processo(analise.getProcesso().getIdProcesso());

        existente.setPrevisaoTempoJulgamento(
                analise.getPrevisaoTempoJulgamento()
        );

        existente.setProbabilidadeSucesso(
                analise.getProbabilidadeSucesso()
        );

        existente.setNivelConfianca(
                analise.getNivelConfianca()
        );

        existente.setSugestoes(
                analise.getSugestoes()
        );

        existente.setPadroesEncontrados(
                analise.getPadroesEncontrados()
        );

        existente.setProcesso(processo);

        return analiseRepository.save(existente);
    }

    // DELETE
    public void excluir(Long id) {

        IA analise = analiseRepository.findByIdAnaliseAndProcessoClienteEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Análise de IA não encontrada"));

        analiseRepository.delete(analise);
    }
}

