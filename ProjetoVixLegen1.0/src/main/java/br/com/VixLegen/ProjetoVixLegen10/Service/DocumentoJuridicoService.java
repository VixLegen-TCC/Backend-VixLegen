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

    public DocumentoJuridico cadastrar(DocumentoJuridico documento) {

        ProcessoJuridico processo = buscarProcesso(documento);
        CategoriaDocumento categoria = buscarCategoria(documento);

        documento.setProcesso(processo);
        documento.setCategoriaDocumento(categoria);

        if (documento.getDataCadastro() == null) {
            documento.setDataCadastro(LocalDateTime.now());
        }

        return documentoRepository.save(documento);
    }

    public List<DocumentoJuridico> listarTodos() {
        return documentoRepository.findAll();
    }

    public DocumentoJuridico buscarPorId(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Documento não encontrado"));
    }

    public DocumentoJuridico atualizar(
            Long id,
            DocumentoJuridico documento) {

        DocumentoJuridico existente = buscarPorId(id);
        ProcessoJuridico processo = buscarProcesso(documento);
        CategoriaDocumento categoria = buscarCategoria(documento);

        existente.setNome(documento.getNome());
        existente.setConteudo(documento.getConteudo());
        existente.setArquivo(documento.getArquivo());
        existente.setTipoArquivo(documento.getTipoArquivo());
        existente.setTamanhoArquivo(documento.getTamanhoArquivo());
        existente.setProcesso(processo);
        existente.setCategoriaDocumento(categoria);

        return documentoRepository.save(existente);
    }

    public void excluir(Long id) {
        documentoRepository.delete(buscarPorId(id));
    }

    public List<DocumentoJuridico> listarPorProcesso(Long idProcesso) {

        if (!processoRepository.existsById(idProcesso)) {
            throw new RecursoNaoEncontradoException(
                    "Processo jurídico não encontrado"
            );
        }

        return documentoRepository.findByProcessoIdProcesso(idProcesso);
    }

    public DocumentoJuridico anexar(Long id, String arquivo) {

        DocumentoJuridico documento = buscarPorId(id);

        if (arquivo == null || arquivo.isBlank()) {
            throw new RegraNegocioException("O arquivo é obrigatório");
        }

        documento.setArquivo(arquivo);

        return documentoRepository.save(documento);
    }

    public DocumentoJuridico remover(Long id) {

        DocumentoJuridico documento = buscarPorId(id);
        documento.setArquivo(null);

        return documentoRepository.save(documento);
    }

    private ProcessoJuridico buscarProcesso(
            DocumentoJuridico documento) {

        if (documento.getProcesso() == null
                || documento.getProcesso().getIdProcesso() == null) {
            throw new RegraNegocioException(
                    "O processo do documento é obrigatório"
            );
        }

        return processoRepository.findById(
                documento.getProcesso().getIdProcesso()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException(
                        "Processo jurídico não encontrado"
                ));
    }

    private CategoriaDocumento buscarCategoria(
            DocumentoJuridico documento) {

        if (documento.getCategoriaDocumento() == null
                || documento.getCategoriaDocumento()
                .getCodigoCategoriaDocumento() == null) {
            throw new RegraNegocioException(
                    "A categoria do documento é obrigatória"
            );
        }

        return categoriaRepository.findById(
                documento.getCategoriaDocumento()
                        .getCodigoCategoriaDocumento()
        ).orElseThrow(() ->
                new RecursoNaoEncontradoException(
                        "Categoria de documento não encontrada"
                ));
    }
}
