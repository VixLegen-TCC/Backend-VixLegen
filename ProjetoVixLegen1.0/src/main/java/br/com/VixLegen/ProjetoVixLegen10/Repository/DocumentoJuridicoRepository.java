package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Model.DocumentoJuridico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoJuridicoRepository extends JpaRepository<DocumentoJuridico, Long> {
    List<DocumentoJuridico> findByProcessoIdProcesso(Long idProcesso);
}
