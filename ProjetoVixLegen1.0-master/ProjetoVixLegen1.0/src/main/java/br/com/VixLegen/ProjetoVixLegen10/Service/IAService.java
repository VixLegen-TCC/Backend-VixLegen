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

    public IAService(
            IARepository analiseRepository,
            ProcessoJuridicoRepository processoRepository) {

        this.analiseRepository = analiseRepository;
        this.processoRepository = processoRepository;
    }

    // CREATE
    public IA cadastrar(IA analise) {

        ProcessoJuridico processo = processoRepository.findById(
                analise.getProcesso().getIdProcesso()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Processo jurídico não encontrado"));

        analise.setProcesso(processo);

        return analiseRepository.save(analise);
    }

    // READ
    public List<IA> listarTodos() {
        return analiseRepository.findAll();
    }

    // READ por ID
    public IA buscarPorId(Long id) {

        return analiseRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Análise de IA não encontrada"));
    }

    // UPDATE
    public IA atualizar(
            Long id,
            IA analise) {

        IA existente = analiseRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Análise de IA não encontrada"));

        ProcessoJuridico processo = processoRepository.findById(
                analise.getProcesso().getIdProcesso()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Processo jurídico não encontrado"));

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

        IA analise = analiseRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Análise de IA não encontrada"));

        analiseRepository.delete(analise);
    }
}

