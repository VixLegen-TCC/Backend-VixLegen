package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.TarefaRequest;
import br.com.VixLegen.ProjetoVixLegen10.Enums.PrioridadeTarefa;
import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusTarefa;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Model.Tarefa;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.TarefaRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final ProcessoJuridicoRepository processoRepository;
    private final UsuarioRepository usuarioRepository;

    public TarefaService(
            TarefaRepository tarefaRepository,
            ProcessoJuridicoRepository processoRepository,
            UsuarioRepository usuarioRepository) {

        this.tarefaRepository = tarefaRepository;
        this.processoRepository = processoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Tarefa cadastrar(TarefaRequest request) {

        ProcessoJuridico processo =
                buscarProcesso(request.getProcessoId());

        Usuario usuario =
                buscarUsuario(request.getUsuarioResponsavelId());

        LocalDateTime dataAtribuicao =
                LocalDateTime.now();

        validarPrazo(
                request.getPrazo(),
                dataAtribuicao
        );

        Tarefa tarefa = new Tarefa();

        tarefa.setDataAtribuicao(dataAtribuicao);
        tarefa.setPrazo(request.getPrazo());
        tarefa.setTipoTarefa(request.getTipoTarefa());
        tarefa.setDescricao(request.getDescricao());
        tarefa.setStatus(request.getStatus());
        tarefa.setPrioridade(
                request.getPrioridade() != null
                        ? request.getPrioridade()
                        : PrioridadeTarefa.MEDIA
        );
        tarefa.setProcesso(processo);
        tarefa.setUsuarioResponsavel(usuario);

        return tarefaRepository.save(tarefa);
    }

    public List<Tarefa> listarTodos() {
        return tarefaRepository.findAll();
    }

    public Tarefa buscarPorId(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Tarefa não encontrada"
                        ));
    }

    public Tarefa atualizar(
            Long id,
            TarefaRequest request) {

        Tarefa existente = buscarPorId(id);

        ProcessoJuridico processo =
                buscarProcesso(request.getProcessoId());

        Usuario usuario =
                buscarUsuario(
                        request.getUsuarioResponsavelId()
                );

        validarPrazo(
                request.getPrazo(),
                existente.getDataAtribuicao()
        );

        existente.setPrazo(request.getPrazo());
        existente.setTipoTarefa(
                request.getTipoTarefa()
        );
        existente.setDescricao(
                request.getDescricao()
        );
        existente.setStatus(
                request.getStatus()
        );
        existente.setPrioridade(
                request.getPrioridade() != null
                        ? request.getPrioridade()
                        : PrioridadeTarefa.MEDIA
        );
        existente.setProcesso(processo);
        existente.setUsuarioResponsavel(usuario);

        return tarefaRepository.save(existente);
    }

    public void excluir(Long id) {
        tarefaRepository.delete(
                buscarPorId(id)
        );
    }

    public Tarefa alterarStatus(
            Long id,
            StatusTarefa status) {

        Tarefa tarefa = buscarPorId(id);
        tarefa.setStatus(status);

        return tarefaRepository.save(tarefa);
    }

    public Tarefa concluir(Long id) {

        return alterarStatus(
                id,
                StatusTarefa.CONCLUIDA
        );
    }

    public Tarefa atribuir(
            Long idTarefa,
            Long idUsuario,
            Long idProcesso) {

        Tarefa tarefa = buscarPorId(idTarefa);

        tarefa.setUsuarioResponsavel(
                buscarUsuario(idUsuario)
        );
        tarefa.setProcesso(
                buscarProcesso(idProcesso)
        );
        tarefa.setStatus(
                StatusTarefa.PENDENTE
        );

        return tarefaRepository.save(tarefa);
    }

    public Tarefa alterarPrazo(
            Long id,
            LocalDateTime novoPrazo) {

        Tarefa tarefa = buscarPorId(id);

        if (tarefa.getStatus()
                == StatusTarefa.CONCLUIDA) {

            throw new RegraNegocioException(
                    "Não é possível alterar o prazo de uma tarefa concluída"
            );
        }

        validarPrazo(
                novoPrazo,
                tarefa.getDataAtribuicao()
        );

        tarefa.setPrazo(novoPrazo);

        return tarefaRepository.save(tarefa);
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

    private Usuario buscarUsuario(
            Long idUsuario) {

        return usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Usuário responsável não encontrado"
                        ));
    }

    private void validarPrazo(
            LocalDateTime prazo,
            LocalDateTime dataAtribuicao) {

        if (prazo.isBefore(dataAtribuicao)) {
            throw new RegraNegocioException(
                    "O prazo não pode ser anterior à data de atribuição"
            );
        }
    }
}
