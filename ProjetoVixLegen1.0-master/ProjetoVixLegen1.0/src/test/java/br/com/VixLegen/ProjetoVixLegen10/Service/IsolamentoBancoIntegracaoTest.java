package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusTarefa;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.*;
import br.com.VixLegen.ProjetoVixLegen10.Repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integração real com MySQL da CI: verifica consultas JPA multiempresa.
 */
@SpringBootTest
class IsolamentoBancoIntegracaoTest {
    @Autowired private EmpresaRepository empresas;
    @Autowired private CategoriaRepository categorias;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ClienteRepository clientes;
    @Autowired private ProcessoJuridicoRepository processos;
    @Autowired private TarefaRepository tarefas;
    @Autowired private TarefaService tarefaService;
    @Autowired private UsuarioService usuarioService;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Transactional
    void idDaOutraEmpresaNaoEhLegivelNemExcluivel() {
        String marcador = UUID.randomUUID().toString().replace("-", "");
        Categoria categoria = new Categoria();
        categoria.setDescricao("TST-" + marcador);
        categoria.setNivelAcesso(1);
        categoria.setLimiteProcessosSimultaneos(0);
        categoria.setPermissaoVisualizar(true);
        categoria.setPermissaoEditar(true);
        categoria = categorias.saveAndFlush(categoria);

        Empresa a = new Empresa();
        a.setNome("Escritorio A - " + marcador);
        a = empresas.saveAndFlush(a);
        Empresa b = new Empresa();
        b.setNome("Escritorio B - " + marcador);
        b = empresas.saveAndFlush(b);

        Usuario ua = criarUsuario("a", marcador, a, categoria);
        Usuario ub = criarUsuario("b", marcador, b, categoria);
        ProcessoJuridico pa = criarProcesso("a", marcador, a, ua);
        ProcessoJuridico pb = criarProcesso("b", marcador, b, ub);
        Tarefa ta = criarTarefa("a", pa, ua);
        Tarefa tb = criarTarefa("b", pb, ub);

        Jwt jwt = Jwt.withTokenValue("jwt-teste")
                .header("alg", "HS256")
                .subject(String.valueOf(ua.getIdUsuario()))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        List<Tarefa> visiveis = tarefaService.listarTodos();
        assertTrue(visiveis.stream().anyMatch(t -> t.getIdTarefa().equals(ta.getIdTarefa())));
        assertFalse(visiveis.stream().anyMatch(t -> t.getIdTarefa().equals(tb.getIdTarefa())));
        assertThrows(RecursoNaoEncontradoException.class, () -> tarefaService.buscarPorId(tb.getIdTarefa()));
        assertThrows(RecursoNaoEncontradoException.class, () -> tarefaService.excluir(tb.getIdTarefa()));
        assertTrue(usuarios.existsById(ub.getIdUsuario()));

        assertThrows(RecursoNaoEncontradoException.class, () -> usuarioService.buscarPorId(ub.getIdUsuario()));
        assertTrue(usuarioService.listarTodos().stream()
                .noneMatch(u -> u.getIdUsuario().equals(ub.getIdUsuario())));
    }

    private Usuario criarUsuario(String sigla, String marcador, Empresa empresa, Categoria categoria) {
        Usuario u = new Usuario();
        u.setPrimeiroNome("Teste");
        u.setUltimoNome("Organizacao");
        u.setEmail(sigla + marcador + "@exemplo.test");
        u.setSenhaHash("hash-falso-para-teste");
        u.setTelefone("11999999999");
        u.setCpf(sigla + marcador);
        u.setRg("RG-" + sigla + marcador);
        u.setEmpresa(empresa.getNome());
        u.setNumeroOAB("OAB-TST");
        u.setDataNascimento(LocalDate.of(1990, 1, 1));
        u.setEstado("SP");
        u.setCidade("Sao Paulo");
        u.setCep("01000000");
        u.setAtivo(true);
        u.setEmpresaOrganizacao(empresa);
        u.setCategoria(categoria);
        return usuarios.saveAndFlush(u);
    }

    private ProcessoJuridico criarProcesso(String sigla, String marcador, Empresa empresa, Usuario usuario) {
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("Cliente " + sigla);
        cliente.setEmail(sigla + marcador + "@cliente.test");
        cliente.setTelefone("11999999999");
        cliente.setCpf("DOC-" + sigla + marcador);
        cliente.setUsuarioResponsavel(usuario);
        cliente.setEmpresaOrganizacao(empresa);
        clientes.saveAndFlush(cliente);

        ProcessoJuridico p = new ProcessoJuridico();
        p.setNumeroProcesso(sigla + marcador);
        p.setVara("Vara 1");
        p.setComarca("Sao Paulo");
        p.setTribunal("TJSP");
        p.setInstancia("1");
        p.setDataAbertura(LocalDate.now());
        p.setCliente(cliente);
        return processos.saveAndFlush(p);
    }

    private Tarefa criarTarefa(String sigla, ProcessoJuridico processo, Usuario usuario) {
        Tarefa t = new Tarefa();
        t.setDataAtribuicao(LocalDateTime.now());
        t.setPrazo(LocalDateTime.now().plusDays(3));
        t.setTipoTarefa("Tarefa-" + sigla);
        t.setStatus(StatusTarefa.PENDENTE);
        t.setProcesso(processo);
        t.setUsuarioResponsavel(usuario);
        return tarefas.saveAndFlush(t);
    }
}
