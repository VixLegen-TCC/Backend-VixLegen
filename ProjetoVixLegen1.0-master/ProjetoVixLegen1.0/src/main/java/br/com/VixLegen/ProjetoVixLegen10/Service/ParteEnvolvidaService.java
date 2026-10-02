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

    public ParteEnvolvidaService(
            ParteEnvolvidaRepository parteRepository,
            ProcessoJuridicoRepository processoRepository) {

        this.parteRepository = parteRepository;
        this.processoRepository = processoRepository;
    }

    // CREATE
    public ParteEnvolvida cadastrar(ParteEnvolvida parte) {

        ProcessoJuridico processo = processoRepository.findById(
                parte.getProcesso().getIdProcesso()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Processo jurídico não encontrado"));

        parte.setProcesso(processo);

        return parteRepository.save(parte);
    }

    // READ - todos
    public List<ParteEnvolvida> listarTodos() {
        return parteRepository.findAll();
    }

    // READ - por ID
    public ParteEnvolvida buscarPorId(Long id) {

        return parteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Parte envolvida não encontrada"));
    }

    // UPDATE
    public ParteEnvolvida atualizar(
            Long id,
            ParteEnvolvida parte) {

        ParteEnvolvida existente =
                parteRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Parte envolvida não encontrada"));

        ProcessoJuridico processo = processoRepository.findById(
                parte.getProcesso().getIdProcesso()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Processo jurídico não encontrado"));

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
                parteRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Parte envolvida não encontrada"));

        parteRepository.delete(parte);
    }
}