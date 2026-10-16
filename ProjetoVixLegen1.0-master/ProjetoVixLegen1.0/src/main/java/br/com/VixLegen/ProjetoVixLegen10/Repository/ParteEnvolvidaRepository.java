package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Model.ParteEnvolvida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParteEnvolvidaRepository
    extends JpaRepository<ParteEnvolvida, Long>{
    java.util.List<ParteEnvolvida> findByProcessoClienteEmpresaOrganizacaoIdEmpresa(Long empresaId);
    java.util.Optional<ParteEnvolvida> findByIdParteAndProcessoClienteEmpresaOrganizacaoIdEmpresa(Long id, Long empresaId);}

