package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.Colaborador;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ColaboradorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColaboradorService {

    private final ColaboradorRepository colaboradorRepository;
    private final TenantAccessService tenant;

    public ColaboradorService(ColaboradorRepository colaboradorRepository, TenantAccessService tenant) {
        this.colaboradorRepository = colaboradorRepository;
        this.tenant = tenant;
    }

    public Colaborador cadastrar(Colaborador colaborador) {
        colaborador.setEmpresaOrganizacao(tenant.empresaAtual());
        colaborador.setIdColaborador(null);
        return colaboradorRepository.save(colaborador);
    }

    public List<Colaborador> listarTodos() {
        return colaboradorRepository.findByEmpresaOrganizacaoIdEmpresa(tenant.empresaAtualId());
    }

    public Colaborador buscarPorId(Long id) {
        return colaboradorRepository.findByIdColaboradorAndEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Colaborador não encontrado"));
    }

    public Colaborador atualizar(Long id, Colaborador colaborador) {

        Colaborador existente = colaboradorRepository.findByIdColaboradorAndEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Colaborador não encontrado"));

        existente.setNomeCompleto(colaborador.getNomeCompleto());
        existente.setEndereco(colaborador.getEndereco());
        existente.setCidade(colaborador.getCidade());
        existente.setEstado(colaborador.getEstado());
        existente.setTelefone(colaborador.getTelefone());
        existente.setDataNascimento(colaborador.getDataNascimento());

        return colaboradorRepository.save(existente);
    }

    public void excluir(Long id) {

        Colaborador colaborador = colaboradorRepository.findByIdColaboradorAndEmpresaOrganizacaoIdEmpresa(id, tenant.empresaAtualId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Colaborador não encontrado"));

        colaboradorRepository.delete(colaborador);
    }
}