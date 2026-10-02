package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.UsuarioAtualizacaoRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.UsuarioRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Response.UsuarioResponse;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException;
import br.com.VixLegen.ProjetoVixLegen10.Model.Categoria;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.CategoriaRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponse cadastrar(UsuarioRequest request) {

        if (usuarioRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new RegraNegocioException("E-mail já cadastrado");
        }

        if (usuarioRepository.existsByCpf(request.getCpf())) {
            throw new RegraNegocioException("CPF já cadastrado");
        }

        Categoria categoria = buscarCategoria(
                request.getCodigoCategoria()
        );

        Usuario usuario = new Usuario();

        usuario.setPrimeiroNome(request.getPrimeiroNome());
        usuario.setUltimoNome(request.getUltimoNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenhaHash(
                passwordEncoder.encode(request.getSenha())
        );
        usuario.setTelefone(request.getTelefone());
        usuario.setCpf(request.getCpf());
        usuario.setRg(request.getRg());
        usuario.setEmpresa(request.getEmpresa());
        usuario.setNumeroOAB(request.getNumeroOAB());
        usuario.setDataNascimento(request.getDataNascimento());
        usuario.setEstado(request.getEstado());
        usuario.setCidade(request.getCidade());
        usuario.setCep(request.getCep());
        usuario.setAtivo(true);
        usuario.setCategoria(categoria);

        Usuario usuarioSalvo =
                usuarioRepository.save(usuario);

        return converterParaResponse(usuarioSalvo);
    }

    public List<UsuarioResponse> listarTodos() {

        return usuarioRepository
                .findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public UsuarioResponse buscarPorId(Long id) {

        return converterParaResponse(
                buscarEntidadePorId(id)
        );
    }

    public UsuarioResponse atualizar(
            Long id,
            UsuarioAtualizacaoRequest request) {

        Usuario usuarioExistente =
                buscarEntidadePorId(id);

        if (!usuarioExistente
                .getEmail()
                .equalsIgnoreCase(request.getEmail())
                && usuarioRepository
                .existsByEmailIgnoreCase(
                        request.getEmail()
                )) {

            throw new RegraNegocioException(
                    "E-mail já cadastrado"
            );
        }

        if (!usuarioExistente
                .getCpf()
                .equals(request.getCpf())
                && usuarioRepository
                .existsByCpf(request.getCpf())) {

            throw new RegraNegocioException(
                    "CPF já cadastrado"
            );
        }

        usuarioExistente.setPrimeiroNome(
                request.getPrimeiroNome()
        );
        usuarioExistente.setUltimoNome(
                request.getUltimoNome()
        );
        usuarioExistente.setEmail(
                request.getEmail()
        );
        usuarioExistente.setTelefone(
                request.getTelefone()
        );
        usuarioExistente.setCpf(
                request.getCpf()
        );
        usuarioExistente.setRg(
                request.getRg()
        );
        usuarioExistente.setEmpresa(
                request.getEmpresa()
        );
        usuarioExistente.setNumeroOAB(
                request.getNumeroOAB()
        );
        usuarioExistente.setDataNascimento(
                request.getDataNascimento()
        );
        usuarioExistente.setEstado(
                request.getEstado()
        );
        usuarioExistente.setCidade(
                request.getCidade()
        );
        usuarioExistente.setCep(
                request.getCep()
        );

        return converterParaResponse(
                usuarioRepository.save(
                        usuarioExistente
                )
        );
    }

    public UsuarioResponse alterarCategoria(
            Long id,
            Long codigoCategoria) {

        Usuario usuario =
                buscarEntidadePorId(id);

        Categoria categoria =
                buscarCategoria(codigoCategoria);

        usuario.setCategoria(categoria);

        return converterParaResponse(
                usuarioRepository.save(usuario)
        );
    }

    public UsuarioResponse ativar(Long id) {

        Usuario usuario =
                buscarEntidadePorId(id);

        usuario.setAtivo(true);

        return converterParaResponse(
                usuarioRepository.save(usuario)
        );
    }

    public UsuarioResponse desativar(Long id) {

        Usuario usuario =
                buscarEntidadePorId(id);

        usuario.setAtivo(false);

        return converterParaResponse(
                usuarioRepository.save(usuario)
        );
    }

    public void excluir(Long id) {

        Usuario usuario =
                buscarEntidadePorId(id);

        usuarioRepository.delete(usuario);
    }

    public UsuarioResponse buscarPorEmail(
            String email) {

        Usuario usuario =
                usuarioRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Usuário não encontrado"
                                )
                        );

        return converterParaResponse(usuario);
    }

    public UsuarioResponse buscarPorCpf(
            String cpf) {

        Usuario usuario =
                usuarioRepository
                        .findByCpf(cpf)
                        .orElseThrow(() ->
                                new RecursoNaoEncontradoException(
                                        "Usuário não encontrado"
                                )
                        );

        return converterParaResponse(usuario);
    }

    public List<UsuarioResponse> listarAtivos() {

        return usuarioRepository
                .findByAtivoTrue()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    private Usuario buscarEntidadePorId(Long id) {

        return usuarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Usuário não encontrado"
                        )
                );
    }

    private Categoria buscarCategoria(
            Long codigoCategoria) {

        return categoriaRepository
                .findById(codigoCategoria)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Categoria não encontrada"
                        )
                );
    }

    public Usuario buscarUsuarioAtivo(Long id) {

        Usuario usuario =
                buscarEntidadePorId(id);

        if (!usuario.isAtivo()) {
            throw new RecursoNaoEncontradoException(
                    "Usuário está inativo"
            );
        }

        return usuario;
    }

    public Usuario buscarEntidadePorEmail(
            String email) {

        return usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Usuário não encontrado"
                        )
                );
    }

    private UsuarioResponse converterParaResponse(
            Usuario usuario) {

        UsuarioResponse response =
                new UsuarioResponse();

        response.setIdUsuario(
                usuario.getIdUsuario()
        );
        response.setPrimeiroNome(
                usuario.getPrimeiroNome()
        );
        response.setUltimoNome(
                usuario.getUltimoNome()
        );
        response.setEmail(
                usuario.getEmail()
        );
        response.setTelefone(
                usuario.getTelefone()
        );
        response.setCpf(
                usuario.getCpf()
        );
        response.setRg(
                usuario.getRg()
        );
        response.setEmpresa(
                usuario.getEmpresa()
        );
        response.setNumeroOAB(
                usuario.getNumeroOAB()
        );
        response.setDataNascimento(
                usuario.getDataNascimento()
        );
        response.setEstado(
                usuario.getEstado()
        );
        response.setCidade(
                usuario.getCidade()
        );
        response.setCep(
                usuario.getCep()
        );
        response.setAtivo(
                usuario.isAtivo()
        );

        if (usuario.getCategoria() != null) {
            response.setCodigoCategoria(
                    usuario
                            .getCategoria()
                            .getCodigoCategoria()
            );
        }

        return response;
    }
}
