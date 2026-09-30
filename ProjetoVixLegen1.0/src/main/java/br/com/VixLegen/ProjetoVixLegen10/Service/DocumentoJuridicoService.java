package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException;
import br.com.VixLegen.ProjetoVixLegen10.Model.CategoriaDocumento;
import br.com.VixLegen.ProjetoVixLegen10.Model.DocumentoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Repository.CategoriaDocumentoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.DocumentoJuridicoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentoJuridicoService {

    private final DocumentoJuridicoRepository documentoRepository;
    private final ProcessoJuridicoRepository processoRepository;
    private final CategoriaDocumentoRepository categoriaRepository;

    public DocumentoJuridicoService(
            DocumentoJuridicoRepository documentoRepository,
            ProcessoJuridicoRepository processoRepository,
            CategoriaDocumentoRepository categoriaRepository) {

        this.documentoRepository = documentoRepository;
        this.processoRepository = processoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // CREATE
    public DocumentoJuridico cadastrar(DocumentoJuridico documento) {

        ProcessoJuridico processo = processoRepository.findById(
                documento.getProcesso().getIdProcesso()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Processo jurídico não encontrado"));

        CategoriaDocumento categoria = categoriaRepository.findById(
                documento.getCategoriaDocumento()
                        .getCodigoCategoriaDocumento()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException(
                        "Categoria de documento não encontrada"));

        documento.setProcesso(processo);
        documento.setCategoriaDocumento(categoria);

        return documentoRepository.save(documento);
    }

    // READ
    public List<DocumentoJuridico> listarTodos() {
        return documentoRepository.findAll();
    }

    // READ por ID
    public DocumentoJuridico buscarPorId(Long id) {

        return documentoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Documento não encontrado"));
    }

    // UPDATE
    public DocumentoJuridico atualizar(
            Long id,
            DocumentoJuridico documento) {

        DocumentoJuridico existente =
                documentoRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Documento não encontrado"));

        ProcessoJuridico processo = processoRepository.findById(
                documento.getProcesso().getIdProcesso()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException("Processo jurídico não encontrado"));

        CategoriaDocumento categoria = categoriaRepository.findById(
                documento.getCategoriaDocumento()
                        .getCodigoCategoriaDocumento()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException(
                        "Categoria de documento não encontrada"));

        existente.setNome(documento.getNome());
        existente.setDataCadastro(documento.getDataCadastro());
        existente.setArquivo(documento.getArquivo());
        existente.setProcesso(processo);
        existente.setCategoriaDocumento(categoria);

        return documentoRepository.save(existente);
    }

    // DELETE
    public void excluir(Long id) {

        DocumentoJuridico documento =
                documentoRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Documento não encontrado"));

        documentoRepository.delete(documento);
    }

    public List<DocumentoJuridico> listarPorProcesso(Long idProcesso) {

        if (!processoRepository.existsById(idProcesso)) {
            throw new RecursoNaoEncontradoException("Processo jurídico não encontrado");
        }

        return documentoRepository.findByProcessoIdProcesso(idProcesso);
    }

    public DocumentoJuridico anexar(Long id, String arquivo) {

        DocumentoJuridico documento = documentoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Documento não encontrado"));

        if (arquivo == null || arquivo.isBlank()) {
            throw new RegraNegocioException(
                    "O arquivo é obrigatório");
        }

        documento.setArquivo(arquivo);

        return documentoRepository.save(documento);
    }
    public DocumentoJuridico remover(Long id) {

        DocumentoJuridico documento = documentoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Documento não encontrado"));

        documento.setArquivo(null);

        return documentoRepository.save(documento);
    }
}