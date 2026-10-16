package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Model.IA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IARepository  extends JpaRepository<IA, Long> {
    java.util.List<IA> findByProcessoClienteEmpresaOrganizacaoIdEmpresa(Long empresaId);
    java.util.Optional<IA> findByIdAnaliseAndProcessoClienteEmpresaOrganizacaoIdEmpresa(Long id, Long empresaId);
}
