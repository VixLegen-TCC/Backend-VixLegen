package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.ClassificacaoProcessoRequest;
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

        this.classificacaoRepository =
                classificacaoRepository;

        this.processoRepository =
                processoRepository;
    }

    public ClassificacaoProcesso cadastrar(
            ClassificacaoProcessoRequest request) {

        ProcessoJuridico processo =
                buscarProcesso(
                        request.getProcessoId()
                );

        ClassificacaoProcesso classificacao =
                montarClassificacao(request);

        classificacao.setProcesso(processo);

        return classificacaoRepository
                .save(classificacao);
    }

    public List<ClassificacaoProcesso>
    listarTodos() {

        return classificacaoRepository.findAll();
    }

    public ClassificacaoProcesso buscarPorId(
            Long id) {

        return classificacaoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Classificação não encontrada"
                        ));
    }

    public ClassificacaoProcesso atualizar(
            Long id,
            ClassificacaoProcessoRequest request) {

        ClassificacaoProcesso existente =
                buscarPorId(id);

        ProcessoJuridico processo =
                buscarProcesso(
                        request.getProcessoId()
                );

        existente.setStatus(
                request.getStatus()
        );
        existente.setAreaDireito(
                request.getAreaDireito()
        );
        existente.setTipoAcao(
                request.getTipoAcao()
        );
        existente.setFaseProcessual(
                request.getFaseProcessual()
        );
        existente.setDescricaoObjeto(
                request.getDescricaoObjeto()
        );
        existente.setProcesso(processo);

        return classificacaoRepository
                .save(existente);
    }

    public void excluir(Long id) {

        ClassificacaoProcesso classificacao =
                buscarPorId(id);

        classificacaoRepository.delete(
                classificacao
        );
    }

    public ClassificacaoProcesso alterarStatus(
            Long id,
            StatusProcesso novoStatus) {

        ClassificacaoProcesso classificacao =
                buscarPorId(id);

        classificacao.setStatus(novoStatus);

        return classificacaoRepository
                .save(classificacao);
    }

    private ClassificacaoProcesso
    montarClassificacao(
            ClassificacaoProcessoRequest request) {

        ClassificacaoProcesso classificacao =
                new ClassificacaoProcesso();

        classificacao.setStatus(
                request.getStatus()
        );
        classificacao.setAreaDireito(
                request.getAreaDireito()
        );
        classificacao.setTipoAcao(
                request.getTipoAcao()
        );
        classificacao.setFaseProcessual(
                request.getFaseProcessual()
        );
        classificacao.setDescricaoObjeto(
                request.getDescricaoObjeto()
        );

        return classificacao;
    }

    private ProcessoJuridico buscarProcesso(
            Long idProcesso) {

        return processoRepository
                .findById(idProcesso)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Processo jurídico não encontrado"
                        ));
    }
}
