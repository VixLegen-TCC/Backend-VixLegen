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
    private final AlertaPrazoService alertaPrazoService;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            UsuarioRepository usuarioRepository,
            AlertaPrazoService alertaPrazoService) {

        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.alertaPrazoService = alertaPrazoService;
    }

    public Notificacao cadastrar(Notificacao notificacao) {

        Usuario usuario = usuarioRepository.findById(
                notificacao.getUsuario().getIdUsuario()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Usuário não encontrado"));

        notificacao.setUsuario(usuario);

        if (notificacao.getDataEnvio() == null) {
            notificacao.setDataEnvio(LocalDateTime.now());
        }

        return notificacaoRepository.save(notificacao);
    }

    public List<Notificacao> listarTodos() {
        return notificacaoRepository.findAll();
    }

    public List<Notificacao> listarPorUsuario(Long idUsuario) {

        alertaPrazoService.gerarParaUsuario(idUsuario);
        garantirNotificacoesIniciais(idUsuario);

        return notificacaoRepository
                .findByUsuarioIdUsuarioOrderByDataEnvioDesc(idUsuario);
    }

    private void garantirNotificacoesIniciais(Long idUsuario) {

        List<Notificacao> existentes =
                notificacaoRepository
                        .findByUsuarioIdUsuarioOrderByDataEnvioDesc(idUsuario);

        if (!existentes.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Usuário não encontrado"
                        )
                );

        notificacaoRepository.saveAll(List.of(
                criarGenerica(
                        "Sistema",
                        "Bem-vindo ao VixLegen. Sua central de notificações está pronta para acompanhar avisos do escritório.",
                        usuario
                ),
                criarGenerica(
                        "Tarefa",
                        "Revise suas tarefas e prazos pendentes para manter o quadro jurídico atualizado.",
                        usuario
                ),
                criarGenerica(
                        "Documento",
                        "Lembrete: mantenha as minutas vinculadas ao processo correto antes de exportar ou compartilhar.",
                        usuario
                )
        ));
    }

    private Notificacao criarGenerica(
            String canal,
            String mensagem,
            Usuario usuario) {

        Notificacao notificacao =
                new Notificacao();

        notificacao.setMensagem(mensagem);
        notificacao.setDataEnvio(LocalDateTime.now());
        notificacao.setCanal(canal);
        notificacao.setStatus(
                StatusNotificacao.ENVIADA
        );
        notificacao.setLida(false);
        notificacao.setUsuario(usuario);

        return notificacao;
    }

    public Notificacao buscarPorId(Long id) {

        return notificacaoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Notificação não encontrada"));
    }

    public Notificacao atualizar(
            Long id,
            Notificacao notificacao) {

        Notificacao existente = buscarPorId(id);

        Usuario usuario = usuarioRepository.findById(
                notificacao.getUsuario().getIdUsuario()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Usuário não encontrado"));

        existente.setMensagem(notificacao.getMensagem());
        existente.setDataEnvio(notificacao.getDataEnvio());
        existente.setCanal(notificacao.getCanal());
        existente.setStatus(notificacao.getStatus());
        existente.setLida(notificacao.isLida());
        existente.setUsuario(usuario);

        return notificacaoRepository.save(existente);
    }

    public void excluir(Long id) {
        notificacaoRepository.delete(buscarPorId(id));
    }

    public Notificacao marcarComoLida(Long id, Long idUsuario) {

        Notificacao notificacao = buscarPorId(id);

        if (!notificacao.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new RegraNegocioException(
                    "A notificação não pertence ao usuário autenticado"
            );
        }

        notificacao.setLida(true);

        return notificacaoRepository.save(notificacao);
    }

    public void marcarTodasComoLidas(Long idUsuario) {

        List<Notificacao> notificacoes = listarPorUsuario(idUsuario);

        notificacoes.forEach(notificacao ->
                notificacao.setLida(true)
        );

        notificacaoRepository.saveAll(notificacoes);
    }

    public Notificacao enviar(Long id) {

        Notificacao notificacao = buscarPorId(id);

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

        Notificacao notificacao = buscarPorId(id);

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
