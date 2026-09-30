package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusProcesso;
import br.com.VixLegen.ProjetoVixLegen10.Model.ClassificacaoProcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassificacaoProcessoRepository
        extends JpaRepository<ClassificacaoProcesso, Long> {

    Optional<ClassificacaoProcesso> findByProcessoIdProcesso(Long idProcesso);

    List<ClassificacaoProcesso> findByStatus(StatusProcesso status);
}
