package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.ParteEnvolvida;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ParteEnvolvidaRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParteEnvolvidaService {

    private final ParteEnvolvidaRepository parteRepository;
    private final ProcessoJuridicoRepository processoRepository;
    private final TenantAccessService tenant;

    public ParteEnvolvidaService(
            ParteEnvolvidaRepository parteRepository,
            ProcessoJuridicoRepository processoRepository,
            TenantAccessService tenant) {

        this.parteRepository = parteRepository;
        this.processoRepository = processoRepository;
        this.tenant = tenant;
    }

    // CREATE
    public ParteEnvolvida cadastrar(ParteEnvolvida parte) {

        ProcessoJuridico processo = tenant.processo(parte.getProcesso().getIdProcesso());

        parte.setProcesso(processo);
        parte.setIdParte(0L); // ID gerado é inicializado a zero.

        return parteRepository.save(parte);
    }

    // READ - todos
    public List<ParteEnvolvida> listarTodos() {
        return parteRepository.findByProcessoClienteEmpresaOrganizacaoIdEmpresa(tenant.empresaAtualId());
    }

    // READ - por ID
    public ParteEnvolvida buscarPorId(Long id) {

        return parteRepository.findByIdParteAndProcessoClienteEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Parte envolvida não encontrada"));
    }

    // UPDATE
    public ParteEnvolvida atualizar(
            Long id,
            ParteEnvolvida parte) {

        ParteEnvolvida existente =
                parteRepository.findByIdParteAndProcessoClienteEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Parte envolvida não encontrada"));

        ProcessoJuridico processo = tenant.processo(parte.getProcesso().getIdProcesso());

        existente.setPosicaoCliente(parte.getPosicaoCliente());
        existente.setParteContraria(parte.getParteContraria());
        existente.setAdvogadoContrario(parte.getAdvogadoContrario());
        existente.setJuizResponsavel(parte.getJuizResponsavel());
        existente.setProcesso(processo);

        return parteRepository.save(existente);
    }

    // DELETE
    public void excluir(Long id) {

        ParteEnvolvida parte =
                parteRepository.findByIdParteAndProcessoClienteEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Parte envolvida não encontrada"));

        parteRepository.delete(parte);
    }
}