package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessoJuridicoRepository extends JpaRepository<ProcessoJuridico, Long> {

    List<ProcessoJuridico> findByClienteIdCliente(Long idCliente);

    long countByClienteUsuarioResponsavelIdUsuario(Long idUsuario);
}
