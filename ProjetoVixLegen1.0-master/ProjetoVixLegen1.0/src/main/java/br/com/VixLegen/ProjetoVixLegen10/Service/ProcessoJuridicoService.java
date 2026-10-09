package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.ProcessoJuridicoRequest;
import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusProcesso;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException;
import br.com.VixLegen.ProjetoVixLegen10.Model.Cliente;
import br.com.VixLegen.ProjetoVixLegen10.Model.ClassificacaoProcesso;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ClassificacaoProcessoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ClienteRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProcessoJuridicoService {

    private final ProcessoJuridicoRepository processoRepository;
    private final ClienteRepository clienteRepository;
    private final ClassificacaoProcessoRepository classificacaoRepository;
    private final TenantAccessService tenant;

    public ProcessoJuridicoService(
            ProcessoJuridicoRepository processoRepository,
            ClienteRepository clienteRepository,
            ClassificacaoProcessoRepository classificacaoRepository,
            TenantAccessService tenant) {

        this.processoRepository = processoRepository;
        this.clienteRepository = clienteRepository;
        this.classificacaoRepository = classificacaoRepository;
        this.tenant = tenant;
    }

    public ProcessoJuridico cadastrar(
            ProcessoJuridicoRequest request) {

        Cliente cliente = buscarCliente(
                request.getClienteId()
        );

        validarLimiteProcessos(cliente);

        ProcessoJuridico processo =
                montarProcesso(request);

        processo.setCliente(cliente);

        return processoRepository.save(processo);
    }

    public List<ProcessoJuridico> listarTodos() {
        return processoRepository.findByClienteEmpresaOrganizacaoIdEmpresa(tenant.empresaAtualId());
    }

    public ProcessoJuridico buscarPorId(Long id) {
        return tenant.processo(id);
    }

    public ProcessoJuridico atualizar(
            Long id,
            ProcessoJuridicoRequest request) {

        ProcessoJuridico processoExistente =
                buscarPorId(id);

        classificacaoRepository
                .findByProcessoIdProcesso(id)
                .filter(classificacao ->
                        classificacao.getStatus()
                                == StatusProcesso.ENCERRADO
                )
                .ifPresent(classificacao -> {
                    throw new RegraNegocioException(
                            "Processo encerrado não pode ser alterado"
                    );
                });

        Cliente cliente = buscarCliente(
                request.getClienteId()
        );

        if (!processoExistente
                .getCliente()
                .getIdCliente()
                .equals(cliente.getIdCliente())) {

            validarLimiteProcessos(cliente);
        }

        processoExistente.setNumeroProcesso(
                request.getNumeroProcesso()
        );
        processoExistente.setVara(
                request.getVara()
        );
        processoExistente.setComarca(
                request.getComarca()
        );
        processoExistente.setTribunal(
                request.getTribunal()
        );
        processoExistente.setInstancia(
                request.getInstancia()
        );
        processoExistente.setSegredoJustica(
                request.isSegredoJustica()
        );
        processoExistente.setDataAbertura(
                request.getDataAbertura()
        );
        processoExistente.setDataEncerramento(
                request.getDataEncerramento()
        );
        processoExistente.setCliente(cliente);

        return processoRepository.save(
                processoExistente
        );
    }

    @Transactional
    public void excluir(Long id) {
        ProcessoJuridico processo =
                buscarPorId(id);

        processoRepository.delete(processo);
    }

    public List<ProcessoJuridico> listarPorStatus(
            StatusProcesso status) {

        return classificacaoRepository
                .findByStatus(status)
                .stream()
                .filter(c -> c.getProcesso().getCliente().getEmpresaOrganizacao() != null && c.getProcesso().getCliente().getEmpresaOrganizacao().getIdEmpresa().equals(tenant.empresaAtualId()))
                .map(ClassificacaoProcesso::getProcesso)
                .toList();
    }

    public StatusProcesso consultarSituacao(Long id) {

        tenant.processo(id);

        return classificacaoRepository
                .findByProcessoIdProcesso(id)
                .map(ClassificacaoProcesso::getStatus)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Classificação do processo não encontrada"
                        ));
    }

    private ProcessoJuridico montarProcesso(
            ProcessoJuridicoRequest request) {

        ProcessoJuridico processo =
                new ProcessoJuridico();

        processo.setNumeroProcesso(
                request.getNumeroProcesso()
        );
        processo.setVara(
                request.getVara()
        );
        processo.setComarca(
                request.getComarca()
        );
        processo.setTribunal(
                request.getTribunal()
        );
        processo.setInstancia(
                request.getInstancia()
        );
        processo.setSegredoJustica(
                request.isSegredoJustica()
        );
        processo.setDataAbertura(
                request.getDataAbertura()
        );
        processo.setDataEncerramento(
                request.getDataEncerramento()
        );

        return processo;
    }

    private Cliente buscarCliente(Long idCliente) {

        if (idCliente == null) {
            throw new RegraNegocioException(
                    "O cliente do processo é obrigatório"
            );
        }

        return tenant.cliente(idCliente);
    }

    private void validarLimiteProcessos(
            Cliente cliente) {

        Usuario responsavel =
                cliente.getUsuarioResponsavel();

        if (responsavel == null
                || responsavel.getCategoria() == null) {

            throw new RegraNegocioException(
                    "Cliente sem usuário responsável ou categoria definida"
            );
        }

        Integer limite =
                responsavel
                        .getCategoria()
                        .getLimiteProcessosSimultaneos();

        if (limite == null || limite <= 0) {
            return;
        }

        long quantidadeAtual =
                processoRepository
                        .countByClienteUsuarioResponsavelIdUsuario(
                                responsavel.getIdUsuario()
                        );

        if (quantidadeAtual >= limite) {
            throw new RegraNegocioException(
                    "O usuário responsável atingiu o limite de processos simultâneos"
            );
        }
    }
}
