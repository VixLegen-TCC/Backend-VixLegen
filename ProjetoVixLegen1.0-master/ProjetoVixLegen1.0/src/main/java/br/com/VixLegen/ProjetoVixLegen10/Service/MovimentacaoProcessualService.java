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

    public MovimentacaoProcessualService(
            MovimentacaoProcessualRepository movimentacaoRepository,
            ProcessoJuridicoRepository processoRepository) {

        this.movimentacaoRepository = movimentacaoRepository;
        this.processoRepository = processoRepository;
    }

    // CREATE
    public MovimentacaoProcessual cadastrar(
            MovimentacaoProcessual movimentacao) {

        ProcessoJuridico processo = processoRepository.findById(
                movimentacao.getProcesso().getIdProcesso()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Processo jurídico não encontrado"));

        movimentacao.setProcesso(processo);

        return movimentacaoRepository.save(movimentacao);
    }

    // READ
    public List<MovimentacaoProcessual> listarTodos() {
        return movimentacaoRepository.findAll();
    }

    // READ por ID
    public MovimentacaoProcessual buscarPorId(Long id) {

        return movimentacaoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Movimentação processual não encontrada"));
    }

    // UPDATE
    public MovimentacaoProcessual atualizar(
            Long id,
            MovimentacaoProcessual movimentacao) {

        MovimentacaoProcessual existente =
                movimentacaoRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Movimentação processual não encontrada"));

        ProcessoJuridico processo = processoRepository.findById(
                movimentacao.getProcesso().getIdProcesso()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Processo jurídico não encontrado"));

        existente.setData(movimentacao.getData());
        existente.setDescricao(movimentacao.getDescricao());
        existente.setProcesso(processo);

        return movimentacaoRepository.save(existente);
    }

    // DELETE
    public void excluir(Long id) {

        MovimentacaoProcessual movimentacao =
                movimentacaoRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Movimentação processual não encontrada"));

        movimentacaoRepository.delete(movimentacao);
    }

    public MovimentacaoProcessual registrar(
            Long idProcesso,
            MovimentacaoProcessual movimentacao) {

        ProcessoJuridico processo = processoRepository.findById(idProcesso)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Processo jurídico não encontrado"));

        movimentacao.setProcesso(processo);

        return movimentacaoRepository.save(movimentacao);
    }
}