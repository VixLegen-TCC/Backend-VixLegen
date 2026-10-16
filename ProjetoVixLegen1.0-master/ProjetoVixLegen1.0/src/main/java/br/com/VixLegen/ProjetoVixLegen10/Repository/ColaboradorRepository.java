package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {
    java.util.List<Colaborador> findByEmpresaOrganizacaoIdEmpresa(Long empresaId);
    java.util.Optional<Colaborador> findByIdColaboradorAndEmpresaOrganizacaoIdEmpresa(Long id, Long empresaId);
}