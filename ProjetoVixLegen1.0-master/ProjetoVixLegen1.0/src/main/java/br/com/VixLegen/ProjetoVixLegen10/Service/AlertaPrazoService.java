package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusNotificacao;
import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusProcesso;
import br.com.VixLegen.ProjetoVixLegen10.Enums.StatusTarefa;
import br.com.VixLegen.ProjetoVixLegen10.Model.Empresa;
import br.com.VixLegen.ProjetoVixLegen10.Model.Notificacao;
import br.com.VixLegen.ProjetoVixLegen10.Model.ProcessoJuridico;
import br.com.VixLegen.ProjetoVixLegen10.Model.Tarefa;
import br.com.VixLegen.ProjetoVixLegen10.Model.Usuario;
import br.com.VixLegen.ProjetoVixLegen10.Repository.NotificacaoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.TarefaRepository;
import br.com.VixLegen.ProjetoVixLegen10.Repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Alertas sobre datas explicitamente cadastradas, sem cálculos jurídicos automáticos.
 * Janelas: 7, 3 e 1 dia, no dia e uma vez quando vencido.
 */
@Service
public class AlertaPrazoService {
    private static final Logger LOG = LoggerFactory.getLogger(AlertaPrazoService.class);
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final UsuarioRepository usuarios;
    private final TarefaRepository tarefas;
    private final ProcessoJuridicoRepository processos;
    private final NotificacaoRepository notificacoes;
    private final Clock clock;

    public AlertaPrazoService(UsuarioRepository usuarios, TarefaRepository tarefas,
            ProcessoJuridicoRepository processos, NotificacaoRepository notificacoes, Clock clock) {
        this.usuarios = usuarios;
        this.tarefas = tarefas;
        this.processos = processos;
        this.notificacoes = notificacoes;
        this.clock = clock;
    }

    // Roda sem navegador aberto, repetindo a cada 15 minutos por padrão.
    @Transactional
    @Scheduled(initialDelayString = "${app.alertas-prazo.atraso-inicial-ms:10000}",
            fixedDelayString = "${app.alertas-prazo.intervalo-ms:900000}")
    public void verificarPrazos() {
        for (Usuario usuario : usuarios.findByAtivoTrue()) {
            try {
                gerarParaUsuario(usuario.getIdUsuario());
            } catch (RuntimeException erro) {
                LOG.error("Falha ao verificar prazos do usuário {}", usuario.getIdUsuario(), erro);
            }
        }
    }

    @Transactional
    public void gerarParaUsuario(Long usuarioId) {
        Usuario usuario = usuarios.findById(usuarioId).orElse(null);
        if (usuario == null || !usuario.isAtivo() || usuario.getEmpresaOrganizacao() == null) return;

        // Avisos de tarefas vão exclusivamente para o usuário responsável.
        for (Tarefa tarefa : tarefas.findByUsuarioResponsavelIdUsuario(usuarioId)) {
            if (tarefa.getStatus() == StatusTarefa.CONCLUIDA || tarefa.getPrazo() == null) continue;
            ProcessoJuridico processo = tarefa.getProcesso();
            if (!mesmaEmpresa(usuario, processo) || inativo(processo)) continue;
            LocalDate prazo = tarefa.getPrazo().toLocalDate();
            String etapa = etapa(prazo);
            if (etapa == null) continue;
            String assunto = "A tarefa \"" + tarefa.getTipoTarefa() + "\" do processo "
                    + processo.getNumeroProcesso();
            registrar(usuario, "TAREFA", tarefa.getIdTarefa(), prazo,
                    tarefa.getPrazo().toString(), etapa, assunto);
        }

        // Prazo processual: advogado responsável pelo cliente, na mesma empresa.
        for (ProcessoJuridico processo : processos.findByClienteUsuarioResponsavelIdUsuario(usuarioId)) {
            if (!mesmaEmpresa(usuario, processo) || processo.getPrazoProcessual() == null || inativo(processo)) continue;
            LocalDate prazo = processo.getPrazoProcessual();
            String etapa = etapa(prazo);
            if (etapa == null) continue;
            registrar(usuario, "PROCESSO", processo.getIdProcesso(), prazo,
                    prazo.toString(), etapa, "O processo " + processo.getNumeroProcesso());
        }
    }

    private boolean mesmaEmpresa(Usuario usuario, ProcessoJuridico processo) {
        if (processo == null || processo.getCliente() == null) return false;
        Empresa empresaUsuario = usuario.getEmpresaOrganizacao();
        Empresa empresaProcesso = processo.getCliente().getEmpresaOrganizacao();
        return empresaUsuario != null && empresaProcesso != null
                && empresaUsuario.getIdEmpresa() != null
                && empresaUsuario.getIdEmpresa().equals(empresaProcesso.getIdEmpresa());
    }

    private boolean inativo(ProcessoJuridico processo) {
        if (processo.getDataEncerramento() != null) return true;
        if (processo.getClassificacao() == null) return false;
        StatusProcesso status = processo.getClassificacao().getStatus();
        return status == StatusProcesso.ENCERRADO || status == StatusProcesso.SUSPENSO;
    }

    private String etapa(LocalDate prazo) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(clock), prazo);
        if (dias < 0) return "ATRASADO";
        if (dias == 0) return "HOJE";
        if (dias == 1) return "1_DIA";
        if (dias == 3) return "3_DIAS";
        if (dias == 7) return "7_DIAS";
        return null;
    }

    private void registrar(Usuario usuario, String tipo, Long recursoId, LocalDate prazo,
            String identidadePrazo, String etapa, String assunto) {
        // Unicidade por usuário, recurso, data de vencimento e janela de alerta.
        String chave = tipo + ":" + recursoId + ":" + usuario.getIdUsuario()
                + ":" + identidadePrazo + ":" + etapa;
        if (notificacoes.existsByChaveAlerta(chave)) return;

        String descricao = switch (etapa) {
            case "7_DIAS" -> " vence em 7 dias";
            case "3_DIAS" -> " vence em 3 dias";
            case "1_DIA" -> " vence amanhã";
            case "HOJE" -> " vence hoje";
            default -> " está com prazo vencido";
        };

        Notificacao aviso = new Notificacao();
        aviso.setChaveAlerta(chave);
        aviso.setTipoReferencia(tipo);
        aviso.setReferenciaId(recursoId);
        aviso.setEtapaAlerta(etapa);
        aviso.setCanal(tipo.equals("TAREFA") ? "Prazo da tarefa" : "Prazo do processo");
        aviso.setMensagem(assunto + descricao + " (" + prazo.format(DATA_BR)
                + "). Confira a data e as providências necessárias.");
        aviso.setUsuario(usuario);
        aviso.setDataEnvio(LocalDateTime.now(clock));
        aviso.setStatus(StatusNotificacao.ENVIADA);
        aviso.setLida(false);
        try {
            notificacoes.saveAndFlush(aviso);
        } catch (DataIntegrityViolationException erro) {
            // Proteção contra duas varreduras gerando o mesmo alerta ao mesmo tempo.
            if (!notificacoes.existsByChaveAlerta(chave)) throw erro;
        }
    }
}
