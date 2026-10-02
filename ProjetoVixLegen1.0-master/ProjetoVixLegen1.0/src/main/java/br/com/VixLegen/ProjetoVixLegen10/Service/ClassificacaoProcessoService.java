package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusProcesso;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.ClassificacaoProcesso;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ClassificacaoProcessoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassificacaoProcessoService {

    private final ClassificacaoProcessoRepository classificacaoRepository;
    private final ProcessoJuridicoRepository processoRepository;

    public ClassificacaoProcessoService(
            ClassificacaoProcessoRepository classificacaoRepository,
            ProcessoJuridicoRepository processoRepository) {

        this.classificacaoRepository = classificacaoRepository;
        this.processoRepository = processoRepository;
    }

    // CREATE
    public ClassificacaoProcesso cadastrar(
            ClassificacaoProcesso classificacao) {

        ProcessoJuridico processo = processoRepository.findById(
                classificacao.getProcesso().getIdProcesso()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Processo jurídico não encontrado"));

        classificacao.setProcesso(processo);

        return classificacaoRepository.save(classificacao);
    }

    // READ - todos
    public List<ClassificacaoProcesso> listarTodos() {
        return classificacaoRepository.findAll();
    }

    // READ - por ID
    public ClassificacaoProcesso buscarPorId(Long id) {

        return classificacaoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Classificação não encontrada"));
    }

    // UPDATE
    public ClassificacaoProcesso atualizar(
            Long id,
            ClassificacaoProcesso classificacao) {

        ClassificacaoProcesso existente =
                classificacaoRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Classificação não encontrada"));

        ProcessoJuridico processo = processoRepository.findById(
                classificacao.getProcesso().getIdProcesso()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Processo jurídico não encontrado"));

        existente.setStatus(classificacao.getStatus());
        existente.setAreaDireito(classificacao.getAreaDireito());
        existente.setTipoAcao(classificacao.getTipoAcao());
        existente.setFaseProcessual(classificacao.getFaseProcessual());
        existente.setDescricaoObjeto(classificacao.getDescricaoObjeto());
        existente.setProcesso(processo);

        return classificacaoRepository.save(existente);
    }

    // DELETE
    public void excluir(Long id) {

        ClassificacaoProcesso classificacao =
                classificacaoRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Classificação não encontrada"));

        classificacaoRepository.delete(classificacao);
    }

    public ClassificacaoProcesso alterarStatus(
            Long id,
            StatusProcesso novoStatus) {

        ClassificacaoProcesso classificacao =
                classificacaoRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Classificação não encontrada"));

        classificacao.setStatus(novoStatus);

        return classificacaoRepository.save(classificacao);
    }
}