package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusNotificacao;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException;
import br.com.VixLegen.ProjetoVixLegen10.Model.Notificacao;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.NotificacaoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            UsuarioRepository usuarioRepository) {

        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // CREATE
    public Notificacao cadastrar(Notificacao notificacao) {

        Usuario usuario = usuarioRepository.findById(
                notificacao.getUsuario().getIdUsuario()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Usuário não encontrado"));

        notificacao.setUsuario(usuario);

        return notificacaoRepository.save(notificacao);
    }

    // READ
    public List<Notificacao> listarTodos() {
        return notificacaoRepository.findAll();
    }

    // READ por ID
    public Notificacao buscarPorId(Long id) {

        return notificacaoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Notificação não encontrada"));
    }

    // UPDATE
    public Notificacao atualizar(
            Long id,
            Notificacao notificacao) {

        Notificacao existente = notificacaoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Notificação não encontrada"));

        Usuario usuario = usuarioRepository.findById(
                notificacao.getUsuario().getIdUsuario()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Usuário não encontrado"));

        existente.setMensagem(notificacao.getMensagem());
        existente.setDataEnvio(notificacao.getDataEnvio());
        existente.setCanal(notificacao.getCanal());
        existente.setStatus(notificacao.getStatus());
        existente.setUsuario(usuario);

        return notificacaoRepository.save(existente);
    }

    // DELETE
    public void excluir(Long id) {

        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Notificação não encontrada"));

        notificacaoRepository.delete(notificacao);
    }

    public Notificacao enviar(Long id) {

        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Notificação não encontrada"));

        if (notificacao.getStatus() == StatusNotificacao.ENVIADA) {
            throw new RegraNegocioException(
                    "A notificação já foi enviada");
        }

        if (notificacao.getStatus() == StatusNotificacao.CANCELADA) {
            throw new RegraNegocioException(
                    "Não é possível enviar uma notificação cancelada");
        }

        notificacao.setStatus(StatusNotificacao.ENVIADA);
        notificacao.setDataEnvio(LocalDateTime.now());

        return notificacaoRepository.save(notificacao);
    }


    public Notificacao cancelar(Long id) {

        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Notificação não encontrada"));

        if (notificacao.getStatus() == StatusNotificacao.ENVIADA) {
            throw new RegraNegocioException(
                    "Não é possível cancelar uma notificação já enviada");
        }

        if (notificacao.getStatus() == StatusNotificacao.CANCELADA) {
            throw new RegraNegocioException(
                    "A notificação já está cancelada");
        }

        notificacao.setStatus(StatusNotificacao.CANCELADA);

        return notificacaoRepository.save(notificacao);
    }
}
