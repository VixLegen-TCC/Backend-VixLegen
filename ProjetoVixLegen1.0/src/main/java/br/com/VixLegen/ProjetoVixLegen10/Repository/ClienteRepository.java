package br.com.VixLegen.ProjetoVixLegen10.Repository;

import br.com.VixLegen.ProjetoVixLegen10.Model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByCnpj(String cnpj);

    boolean existsByCpfAndIdClienteNot(String cpf, Long idCliente);

    boolean existsByCnpjAndIdClienteNot(String cnpj, Long idCliente);

    List<Cliente> findByNomeCompletoContainingIgnoreCase(String nome);
}
