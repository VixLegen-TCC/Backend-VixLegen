package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.CadastroPublicoRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.UsuarioAtualizacaoRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.UsuarioRequest;
import br.com.VixLegen.ProjetoVixLegen10.DTOs.Response.UsuarioResponse;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.ConflitoRecursoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.Categoria;
import br.com.VixLegen.ProjetoVixLegen10.Model.Empresa;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.CategoriaRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.EmpresaRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String categoriaPublicaPadrao;
    private final TenantAccessService tenant;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder,
            TenantAccessService tenant,
            @Value("${cadastro.categoria-padrao:Advogado Júnior}") String categoriaPublicaPadrao) {

        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenant = tenant;
        this.categoriaPublicaPadrao = categoriaPublicaPadrao;
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest request) {

        validarDuplicidade(request.getEmail(), request.getCpf());

        Categoria categoria = buscarCategoria(
                request.getCodigoCategoria()
        );

        Usuario usuario = montarUsuario(
                request.getPrimeiroNome(),
                request.getUltimoNome(),
                request.getEmail(),
                request.getSenha(),
                request.getTelefone(),
                request.getCpf(),
                request.getRg(),
                request.getEmpresa(),
                request.getNumeroOAB(),
                request.getDataNascimento(),
                request.getEstado(),
                request.getCidade(),
                request.getCep(),
                categoria
        );

        // Cadastros administrativos integram o escritório atual.
        usuario.setEmpresaOrganizacao(tenant.empresaAtual());
        return converterParaResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cadastrarPublico(
            CadastroPublicoRequest request) {

        validarDuplicidade(request.getEmail(), request.getCpf());

        Categoria categoria = categoriaRepository
                .findByDescricaoIgnoreCase(categoriaPublicaPadrao)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Categoria padrão de cadastro não encontrada: "
                                        + categoriaPublicaPadrao
                        )
                );

        Usuario usuario = montarUsuario(
                request.getPrimeiroNome(),
                request.getUltimoNome(),
                request.getEmail(),
                request.getSenha(),
                request.getTelefone(),
                request.getCpf(),
                request.getRg(),
                request.getEmpresa(),
                request.getNumeroOAB(),
                request.getDataNascimento(),
                request.getEstado(),
                request.getCidade(),
                request.getCep(),
                categoria
        );

        return salvarUsuarioComOrganizacao(usuario);
    }

    /**
     * Cada novo cadastro inicia com sua própria organização.
     * Nomes iguais de empresas não comprovam permissão de compartilhar clientes.
     * O cadastro é transacional para evitar vínculos incompletos.
     */
    private UsuarioResponse salvarUsuarioComOrganizacao(Usuario usuario) {
        Usuario salvo = usuarioRepository.saveAndFlush(usuario);

        Empresa organizacao = new Empresa();
        String nome = salvo.getEmpresa() == null ? "" : salvo.getEmpresa().trim();
        if (nome.isBlank()) {
            nome = "Escritório do usuário " + salvo.getIdUsuario();
        }
        organizacao.setNome(nome.substring(0, Math.min(nome.length(), 160)));
        organizacao.setCriadorUsuarioId(salvo.getIdUsuario());
        organizacao = empresaRepository.save(organizacao);

        salvo.setEmpresaOrganizacao(organizacao);
        return converterParaResponse(usuarioRepository.save(salvo));
    }

    public List<UsuarioResponse> listarTodos() {

        return usuarioRepository
                .findByEmpresaOrganizacaoIdEmpresa(tenant.empresaAtualId())
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public UsuarioResponse buscarPorId(Long id) {

        return converterParaResponse(
                tenant.usuarioDaEmpresa(id)
        );
    }

    public UsuarioResponse atualizar(
            Long id,
            UsuarioAtualizacaoRequest request) {

        Usuario usuarioExistente =
                tenant.usuarioDaEmpresa(id);

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

        if (!usuarioExistente.getEmail().equalsIgnoreCase(request.getEmail())) {
            // O e-mail presente em sessões anteriores não deve permanecer vigente.
            invalidarSessoes(usuarioExistente);
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

        Usuario usuario = tenant.usuarioDaEmpresa(id);

        Categoria categoria =
                buscarCategoria(codigoCategoria);

        usuario.setCategoria(categoria);
        invalidarSessoes(usuario);

        return converterParaResponse(
                usuarioRepository.save(usuario)
        );
    }

    public UsuarioResponse ativar(Long id) {

        Usuario usuario = tenant.usuarioDaEmpresa(id);

        usuario.setAtivo(true);
        invalidarSessoes(usuario);

        return converterParaResponse(
                usuarioRepository.save(usuario)
        );
    }

    public UsuarioResponse desativar(Long id) {

        Usuario usuario = tenant.usuarioDaEmpresa(id);

        usuario.setAtivo(false);
        invalidarSessoes(usuario);

        return converterParaResponse(
                usuarioRepository.save(usuario)
        );
    }

    public void excluir(Long id) {

        Usuario usuario = tenant.usuarioDaEmpresa(id);

        if (usuario.getIdUsuario().equals(tenant.usuarioAtualId())) {
            throw new RegraNegocioException("Não é permitido excluir a própria conta por esta rota");
        }
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

        tenant.usuarioDaEmpresa(usuario.getIdUsuario());
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

        tenant.usuarioDaEmpresa(usuario.getIdUsuario());
        return converterParaResponse(usuario);
    }

    public List<UsuarioResponse> listarAtivos() {

        return usuarioRepository
                .findByAtivoTrueAndEmpresaOrganizacaoIdEmpresa(tenant.empresaAtualId())
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    private void validarDuplicidade(
            String email,
            String cpf) {

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoRecursoException("E-mail já cadastrado", "email");
        }

        if (usuarioRepository.existsByCpf(cpf)) {
            throw new ConflitoRecursoException("CPF já cadastrado", "cpf");
        }
    }

    private Usuario montarUsuario(
            String primeiroNome,
            String ultimoNome,
            String email,
            String senha,
            String telefone,
            String cpf,
            String rg,
            String empresa,
            String numeroOAB,
            java.time.LocalDate dataNascimento,
            String estado,
            String cidade,
            String cep,
            Categoria categoria) {

        Usuario usuario = new Usuario();

        usuario.setPrimeiroNome(primeiroNome);
        usuario.setUltimoNome(ultimoNome);
        usuario.setEmail(email.trim());
        usuario.setSenhaHash(
                passwordEncoder.encode(senha)
        );
        usuario.setTelefone(telefone);
        usuario.setCpf(cpf);
        usuario.setRg(rg);
        usuario.setEmpresa(empresa);
        usuario.setNumeroOAB(numeroOAB);
        usuario.setDataNascimento(dataNascimento);
        usuario.setEstado(estado);
        usuario.setCidade(cidade);
        usuario.setCep(cep);
        usuario.setAtivo(true);
        usuario.setCategoria(categoria);

        return usuario;
    }

    private void invalidarSessoes(Usuario usuario) {
        Long versao = usuario.getAuthVersion();
        usuario.setAuthVersion((versao == null ? 0L : versao) + 1L);
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

        Usuario usuario = tenant.usuarioDaEmpresa(id);

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
