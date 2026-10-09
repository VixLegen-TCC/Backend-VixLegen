package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioIdUsuarioOrderByDataEnvioDesc(Long idUsuario);

    boolean existsByChaveAlerta(String chaveAlerta);
}
