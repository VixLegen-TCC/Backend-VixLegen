package br.com.VixLegen.ProjetoVixLegen10.Service;

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

import java.util.List;

@Service
public class ProcessoJuridicoService {

    private final ProcessoJuridicoRepository processoRepository;
    private final ClienteRepository clienteRepository;
    private final ClassificacaoProcessoRepository classificacaoRepository;

    public ProcessoJuridicoService(
            ProcessoJuridicoRepository processoRepository,
            ClienteRepository clienteRepository,
            ClassificacaoProcessoRepository classificacaoRepository) {

        this.processoRepository = processoRepository;
        this.clienteRepository = clienteRepository;
        this.classificacaoRepository = classificacaoRepository;
    }

    public ProcessoJuridico cadastrar(ProcessoJuridico processo) {

        Cliente cliente = clienteRepository.findById(
                processo.getCliente().getIdCliente()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Cliente não encontrado"));

        validarLimiteProcessos(cliente);

        processo.setCliente(cliente);

        return processoRepository.save(processo);
    }

    public List<ProcessoJuridico> listarTodos() {
        return processoRepository.findAll();
    }

    public ProcessoJuridico buscarPorId(Long id) {
        return processoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Processo jurídico não encontrado"));
    }

    public ProcessoJuridico atualizar(
            Long id,
            ProcessoJuridico processo) {

        ProcessoJuridico processoExistente = buscarPorId(id);

        classificacaoRepository.findByProcessoIdProcesso(id)
                .filter(classificacao ->
                        classificacao.getStatus() == StatusProcesso.ENCERRADO)
                .ifPresent(classificacao -> {
                    throw new RegraNegocioException(
                            "Processo encerrado não pode ser alterado");
                });

        Cliente cliente = clienteRepository.findById(
                processo.getCliente().getIdCliente()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Cliente não encontrado"));

        if (!processoExistente.getCliente().getIdCliente()
                .equals(cliente.getIdCliente())) {
            validarLimiteProcessos(cliente);
        }

        processoExistente.setNumeroProcesso(processo.getNumeroProcesso());
        processoExistente.setVara(processo.getVara());
        processoExistente.setComarca(processo.getComarca());
        processoExistente.setTribunal(processo.getTribunal());
        processoExistente.setInstancia(processo.getInstancia());
        processoExistente.setSegredoJustica(processo.isSegredoJustica());
        processoExistente.setDataAbertura(processo.getDataAbertura());
        processoExistente.setDataEncerramento(processo.getDataEncerramento());
        processoExistente.setCliente(cliente);

        return processoRepository.save(processoExistente);
    }

    public void excluir(Long id) {
        ProcessoJuridico processo = buscarPorId(id);
        processoRepository.delete(processo);
    }

    public List<ProcessoJuridico> listarPorStatus(StatusProcesso status) {
        return classificacaoRepository.findByStatus(status)
                .stream()
                .map(ClassificacaoProcesso::getProcesso)
                .toList();
    }

    public StatusProcesso consultarSituacao(Long id) {
        if (!processoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException(
                    "Processo jurídico não encontrado");
        }

        return classificacaoRepository.findByProcessoIdProcesso(id)
                .map(ClassificacaoProcesso::getStatus)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Classificação do processo não encontrada"));
    }

    private void validarLimiteProcessos(Cliente cliente) {

        Usuario responsavel = cliente.getUsuarioResponsavel();

        if (responsavel == null || responsavel.getCategoria() == null) {
            throw new RegraNegocioException(
                    "Cliente sem usuário responsável ou categoria definida");
        }

        Integer limite = responsavel.getCategoria()
                .getLimiteProcessosSimultaneos();

        if (limite == null || limite <= 0) {
            return;
        }

        long quantidadeAtual =
                processoRepository.countByClienteUsuarioResponsavelIdUsuario(
                        responsavel.getIdUsuario());

        if (quantidadeAtual >= limite) {
            throw new RegraNegocioException(
                    "O usuário responsável atingiu o limite de processos simultâneos");
        }
    }
}
