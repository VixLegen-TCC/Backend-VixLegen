package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.CategoriaDocumento;
import br.com.VixLegen.ProjetoVixLegen10.Repository.CategoriaDocumentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaDocumentoService {

    private final CategoriaDocumentoRepository categoriaRepository;

    public CategoriaDocumentoService(
            CategoriaDocumentoRepository categoriaRepository) {

        this.categoriaRepository = categoriaRepository;
    }

    public CategoriaDocumento cadastrar(CategoriaDocumento categoria) {
        return categoriaRepository.save(categoria);
    }

    public List<CategoriaDocumento> listarTodos() {
        return categoriaRepository.findAll();
    }

    public CategoriaDocumento buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Categoria de documento não encontrada"));
    }

    public CategoriaDocumento atualizar(
            Long id,
            CategoriaDocumento categoria) {

        CategoriaDocumento existente = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Categoria de documento não encontrada"));

        existente.setDescricao(categoria.getDescricao());
        existente.setPrazoMaximoUtilizacao(
                categoria.getPrazoMaximoUtilizacao());
        existente.setValorTaxaDiariaMulta(
                categoria.getValorTaxaDiariaMulta());

        return categoriaRepository.save(existente);
    }

    public void excluir(Long id) {

        CategoriaDocumento categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Categoria de documento não encontrada"));

        categoriaRepository.delete(categoria);
    }
}