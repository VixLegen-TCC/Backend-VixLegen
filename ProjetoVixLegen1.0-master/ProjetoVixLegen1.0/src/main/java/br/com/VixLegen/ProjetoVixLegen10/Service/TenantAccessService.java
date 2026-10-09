package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException;
import br.com.VixLegen.ProjetoVixLegen10.Model.Cliente;
import br.com.VixLegen.ProjetoVixLegen10.Model.Empresa;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ClienteRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Escopo organizacional. IDs informados pelo frontend jamais definem a empresa atual.
 * Complementa a autenticação JWT para evitar acesso cruzado entre empresas.
 */
@Service
public class TenantAccessService {
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final ProcessoJuridicoRepository processos;

    public TenantAccessService(UsuarioRepository usuarios, ClienteRepository clientes,
                               ProcessoJuridicoRepository processos) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.processos = processos;
    }

    public Long usuarioAtualId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            throw new RegraNegocioException("Sessão autenticada obrigatória");
        }
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException erro) {
            throw new RegraNegocioException("Usuário autenticado inválido");
        }
    }

    public Empresa empresaAtual() {
        Usuario usuario = usuarios.findById(usuarioAtualId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        if (!usuario.isAtivo() || usuario.getEmpresaOrganizacao() == null) {
            throw new RegraNegocioException("Usuário sem organização ativa");
        }
        return usuario.getEmpresaOrganizacao();
    }

    public Long empresaAtualId() {
        return empresaAtual().getIdEmpresa();
    }

    public Usuario usuarioDaEmpresa(Long id) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        if (!usuario.isAtivo() || usuario.getEmpresaOrganizacao() == null
                || !empresaAtualId().equals(usuario.getEmpresaOrganizacao().getIdEmpresa())) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado nesta organização");
        }
        return usuario;
    }

    public Cliente cliente(Long id) {
        return clientes.findByIdClienteAndEmpresaOrganizacaoIdEmpresa(id, empresaAtualId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado nesta organização"));
    }

    public ProcessoJuridico processo(Long id) {
        return processos.findByIdProcessoAndClienteEmpresaOrganizacaoIdEmpresa(id, empresaAtualId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Processo não encontrado nesta organização"));
    }
}
