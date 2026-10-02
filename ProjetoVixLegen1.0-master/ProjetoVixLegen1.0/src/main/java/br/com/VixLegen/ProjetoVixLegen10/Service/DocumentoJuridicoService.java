package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.DocumentoJuridicoRequest;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException;
import br.com.VixLegen.ProjetoVixLegen10.Model.CategoriaDocumento;
import br.com.VixLegen.ProjetoVixLegen10.Model.DocumentoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Repository.CategoriaDocumentoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.DocumentoJuridicoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public DocumentoJuridico cadastrar(
            DocumentoJuridicoRequest request) {

        DocumentoJuridico documento =
                new DocumentoJuridico();

        aplicarDados(documento, request);
        documento.setDataCadastro(LocalDateTime.now());

        return documentoRepository.save(documento);
    }

    public List<DocumentoJuridico> listarTodos() {
        return documentoRepository.findAll();
    }

    public DocumentoJuridico buscarPorId(Long id) {

        return documentoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Documento não encontrado"
                        )
                );
    }

    public DocumentoJuridico atualizar(
            Long id,
            DocumentoJuridicoRequest request) {

        DocumentoJuridico existente =
                buscarPorId(id);

        aplicarDados(existente, request);

        return documentoRepository.save(existente);
    }

    public void excluir(Long id) {

        documentoRepository.delete(
                buscarPorId(id)
        );
    }

    public List<DocumentoJuridico> listarPorProcesso(
            Long idProcesso) {

        if (!processoRepository.existsById(idProcesso)) {
            throw new RecursoNaoEncontradoException(
                    "Processo jurídico não encontrado"
            );
        }

        return documentoRepository
                .findByProcessoIdProcesso(idProcesso);
    }

    public DocumentoJuridico anexar(
            Long id,
            String arquivo) {

        DocumentoJuridico documento =
                buscarPorId(id);

        if (arquivo == null || arquivo.isBlank()) {
            throw new RegraNegocioException(
                    "O arquivo é obrigatório"
            );
        }

        documento.setArquivo(arquivo);

        return documentoRepository.save(documento);
    }

    public DocumentoJuridico remover(Long id) {

        DocumentoJuridico documento =
                buscarPorId(id);

        documento.setArquivo(null);

        return documentoRepository.save(documento);
    }

    private void aplicarDados(
            DocumentoJuridico documento,
            DocumentoJuridicoRequest request) {

        ProcessoJuridico processo =
                processoRepository.findById(
                        request.getProcessoId()
                ).orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Processo jurídico não encontrado"
                        )
                );

        CategoriaDocumento categoria =
                categoriaRepository.findById(
                        request.getCategoriaDocumentoId()
                ).orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Categoria de documento não encontrada"
                        )
                );

        documento.setNome(request.getNome());
        documento.setConteudo(request.getConteudo());
        documento.setTipoArquivo(
                request.getTipoArquivo() == null
                        ? "text/html"
                        : request.getTipoArquivo()
        );
        documento.setTamanhoArquivo(
                request.getTamanhoArquivo()
        );
        documento.setProcesso(processo);
        documento.setCategoriaDocumento(categoria);
    }
}
