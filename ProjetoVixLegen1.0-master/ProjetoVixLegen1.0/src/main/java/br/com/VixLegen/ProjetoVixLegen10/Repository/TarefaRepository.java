package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    java.util.List<Tarefa> findByUsuarioResponsavelIdUsuario(Long idUsuario);
    java.util.List<Tarefa> findByProcessoClienteEmpresaOrganizacaoIdEmpresa(Long empresaId);
    java.util.Optional<Tarefa> findByIdTarefaAndProcessoClienteEmpresaOrganizacaoIdEmpresa(Long id, Long empresaId);
}
