package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.DTOs.Request.ClienteRequest;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Exception.RegraNegocioException;
import br.com.VixLegen.ProjetoVixLegen10.Model.Cliente;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ClienteRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ProcessoJuridicoRepository processoRepository;
    private final UsuarioRepository usuarioRepository;

    public ClienteService(
            ClienteRepository clienteRepository,
            ProcessoJuridicoRepository processoRepository,
            UsuarioRepository usuarioRepository) {

        this.clienteRepository = clienteRepository;
        this.processoRepository = processoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Cliente cadastrar(ClienteRequest request) {

        Cliente cliente = montarCliente(request);
        validarDocumentoDuplicado(cliente, null);

        cliente.setUsuarioResponsavel(
                buscarUsuarioResponsavel(
                        request.getUsuarioResponsavelId()
                )
        );

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Cliente não encontrado"
                        ));
    }

    public List<ProcessoJuridico> listarProcessos(Long idCliente) {
        buscarPorId(idCliente);
        return processoRepository.findByClienteIdCliente(idCliente);
    }

    public Cliente atualizar(
            Long id,
            ClienteRequest request) {

        Cliente clienteExistente = buscarPorId(id);
        Cliente dadosNovos = montarCliente(request);

        validarDocumentoDuplicado(
                dadosNovos,
                id
        );

        clienteExistente.setNomeCompleto(
                dadosNovos.getNomeCompleto()
        );
        clienteExistente.setEmail(
                dadosNovos.getEmail()
        );
        clienteExistente.setTelefone(
                dadosNovos.getTelefone()
        );
        clienteExistente.setCpf(
                dadosNovos.getCpf()
        );
        clienteExistente.setCnpj(
                dadosNovos.getCnpj()
        );
        clienteExistente.setUsuarioResponsavel(
                buscarUsuarioResponsavel(
                        request.getUsuarioResponsavelId()
                )
        );

        return clienteRepository.save(clienteExistente);
    }

    public void excluir(Long id) {

        Cliente cliente = buscarPorId(id);

        if (!processoRepository
                .findByClienteIdCliente(id)
                .isEmpty()) {

            throw new RegraNegocioException(
                    "Não é possível excluir um cliente com processos vinculados"
            );
        }

        clienteRepository.delete(cliente);
    }

    public List<ProcessoJuridico> consultarHistorico(Long idCliente) {
        buscarPorId(idCliente);
        return processoRepository.findByClienteIdCliente(idCliente);
    }

    private Cliente montarCliente(
            ClienteRequest request) {

        Cliente cliente = new Cliente();

        cliente.setNomeCompleto(
                request.getNomeCompleto()
        );
        cliente.setEmail(
                request.getEmail()
        );
        cliente.setTelefone(
                request.getTelefone()
        );

        String cpf = normalizarOpcional(
                request.getCpf()
        );
        String cnpj = normalizarOpcional(
                request.getCnpj()
        );

        cliente.setCpf(cpf);
        cliente.setCnpj(cnpj);

        if ((cpf == null && cnpj == null)
                || (cpf != null && cnpj != null)) {

            throw new RegraNegocioException(
                    "Informe CPF ou CNPJ, mas não os dois"
            );
        }

        return cliente;
    }

    private Usuario buscarUsuarioResponsavel(
            Long idUsuario) {

        if (idUsuario == null) {
            throw new RegraNegocioException(
                    "O usuário responsável pelo cliente é obrigatório"
            );
        }

        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Usuário responsável não encontrado"
                        ));
    }

    private String normalizarOpcional(String valor) {

        if (valor == null) {
            return null;
        }

        String normalizado = valor.trim();

        return normalizado.isEmpty()
                ? null
                : normalizado;
    }

    private void validarDocumentoDuplicado(
            Cliente cliente,
            Long idClienteAtual) {

        if (cliente.getCpf() != null) {

            boolean cpfDuplicado =
                    idClienteAtual == null
                            ? clienteRepository
                            .existsByCpf(cliente.getCpf())
                            : clienteRepository
                            .existsByCpfAndIdClienteNot(
                                    cliente.getCpf(),
                                    idClienteAtual
                            );

            if (cpfDuplicado) {
                throw new RegraNegocioException(
                        "CPF já cadastrado"
                );
            }
        }

        if (cliente.getCnpj() != null) {

            boolean cnpjDuplicado =
                    idClienteAtual == null
                            ? clienteRepository
                            .existsByCnpj(cliente.getCnpj())
                            : clienteRepository
                            .existsByCnpjAndIdClienteNot(
                                    cliente.getCnpj(),
                                    idClienteAtual
                            );

            if (cnpjDuplicado) {
                throw new RegraNegocioException(
                        "CNPJ já cadastrado"
                );
            }
        }
    }
}
