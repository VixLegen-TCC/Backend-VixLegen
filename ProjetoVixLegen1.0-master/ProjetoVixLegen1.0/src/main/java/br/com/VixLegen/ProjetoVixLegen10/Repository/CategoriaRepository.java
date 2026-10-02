package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByDescricaoIgnoreCase(String descricao);
}
