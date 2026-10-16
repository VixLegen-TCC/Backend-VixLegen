package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Model.MovimentacaoProcessual;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoProcessualRepository extends JpaRepository<MovimentacaoProcessual, Long> {
    java.util.List<MovimentacaoProcessual> findByProcessoClienteEmpresaOrganizacaoIdEmpresa(Long empresaId);
    java.util.Optional<MovimentacaoProcessual> findByIdMovimentacaoAndProcessoClienteEmpresaOrganizacaoIdEmpresa(Long id, Long empresaId);
}
