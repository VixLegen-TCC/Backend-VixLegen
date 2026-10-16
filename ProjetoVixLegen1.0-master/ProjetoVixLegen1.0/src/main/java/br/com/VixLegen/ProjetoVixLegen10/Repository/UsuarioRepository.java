package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByCpf(String cpf);

    List<Usuario> findByAtivoTrue();
    List<Usuario> findByEmpresaOrganizacaoIdEmpresa(Long empresaId);
    List<Usuario> findByAtivoTrueAndEmpresaOrganizacaoIdEmpresa(Long empresaId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Usuario u set u.authVersion = COALESCE(u.authVersion, 0) + 1 where u.idUsuario = :id")
    int revogarSessoes(@Param("id") Long id);
}
