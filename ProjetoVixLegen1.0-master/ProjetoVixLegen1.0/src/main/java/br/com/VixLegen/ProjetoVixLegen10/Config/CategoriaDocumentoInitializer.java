package br.com.VixLegen.ProjetoVixLegen10.Config;

import br.com.VixLegen.ProjetoVixLegen10.Model.CategoriaDocumento;
import br.com.VixLegen.ProjetoVixLegen10.Repository.CategoriaDocumentoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CategoriaDocumentoInitializer implements CommandLineRunner {

    private final CategoriaDocumentoRepository repository;

    public CategoriaDocumentoInitializer(
            CategoriaDocumentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {

        if (repository.count() > 0) {
            return;
        }

        repository.saveAll(List.of(
                categoria("Petição Inicial"),
                categoria("Contestação"),
                categoria("Procuração"),
                categoria("Contrato"),
                categoria("Recurso"),
                categoria("Outros")
        ));
    }

    private CategoriaDocumento categoria(String descricao) {

        CategoriaDocumento categoria =
                new CategoriaDocumento();

        categoria.setDescricao(descricao);
        categoria.setPrazoMaximoUtilizacao(30);
        categoria.setValorTaxaDiariaMulta(
                BigDecimal.ZERO
        );

        return categoria;
    }
}
