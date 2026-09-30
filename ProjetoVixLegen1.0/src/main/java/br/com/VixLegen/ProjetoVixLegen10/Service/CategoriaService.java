package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.Categoria;
import br.com.VixLegen.ProjetoVixLegen10.Repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Categoria cadastrar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarTodos() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
    }

    public Categoria atualizar(Long id, Categoria categoria) {

        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));

        categoriaExistente.setDescricao(categoria.getDescricao());
        categoriaExistente.setNivelAcesso(categoria.getNivelAcesso());
        categoriaExistente.setLimiteProcessosSimultaneos(
                categoria.getLimiteProcessosSimultaneos()
        );
        categoriaExistente.setPermissaoVisualizar(
                categoria.isPermissaoVisualizar()
        );
        categoriaExistente.setPermissaoEditar(
                categoria.isPermissaoEditar()
        );
        categoriaExistente.setPermissaoExcluir(
                categoria.isPermissaoExcluir()
        );

        return categoriaRepository.save(categoriaExistente);
    }

    public void excluir(Long id) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));

        categoriaRepository.delete(categoria);
    }
}