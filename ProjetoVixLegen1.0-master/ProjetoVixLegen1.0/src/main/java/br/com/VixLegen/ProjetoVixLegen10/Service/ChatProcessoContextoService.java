package br.com.VixLegen.ProjetoVixLegen10.Service;

import br.com.VixLegen.ProjetoVixLegen10.Exception.RecursoNaoEncontradoException;
import br.com.VixLegen.ProjetoVixLegen10.Model.*;
import br.com.VixLegen.ProjetoVixLegen10.Repository.ProcessoJuridicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

/** Somente leitura, escopo organizacional do JWT. Sem autos, CPF, arquivos ou dados de login. */
@Service
public class ChatProcessoContextoService {
    private final TenantAccessService tenant;
    private final ProcessoJuridicoRepository processos;
    private static final int MAX_GRUPO=10;
    public ChatProcessoContextoService(TenantAccessService tenant, ProcessoJuridicoRepository processos){this.tenant=tenant;this.processos=processos;}
    private static String campo(Object o){
        if(o==null)return "não informado";
        String s=o.toString().replaceAll("[\\p{Cntrl}\\s]+"," ").strip();
        if(s.isEmpty())return "não informado";
        int n=Math.min(140,s.length());if(n<s.length()&&Character.isHighSurrogate(s.charAt(n-1)))n--;
        return n<s.length()?s.substring(0,n)+" [recortado]":s;
    }
    private ProcessoJuridico autorizado(long id){
        var p=tenant.processo(id);
        if(p.isSegredoJustica())throw new RecursoNaoEncontradoException("Processo não disponível para consulta pela IA.");
        return p;
    }
    @Transactional(readOnly=true)
    public Map<String,Object> metadados(long id){var p=autorizado(id);return Map.of("processo_id",p.getIdProcesso().toString(),"numero",campo(p.getNumeroProcesso()));}
    @Transactional(readOnly=true)
    public List<Map<String,Object>> listar(){
        return processos.findByClienteEmpresaOrganizacaoIdEmpresa(tenant.empresaAtualId()).stream()
            .filter(p->!p.isSegredoJustica()).sorted(Comparator.comparing(ProcessoJuridico::getIdProcesso).reversed())
            .limit(200).map(p->Map.<String,Object>of("processo_id",p.getIdProcesso().toString(),"numero",campo(p.getNumeroProcesso()))).toList();
    }
    private static void add(List<Map<String,String>> rows,String group,String s){
        if(s.length()>760){int n=740;if(Character.isHighSurrogate(s.charAt(n-1)))n--;s=s.substring(0,n)+" [recortado]";}
        rows.add(Map.of("secao",group,"trecho",s));
    }
    private static <T> void group(List<Map<String,String>> rows,List<String> avisos,String group,List<T> values,Function<T,String> format){
        add(rows,group,"Quantidade cadastrada: "+values.size()+". "+(values.isEmpty()?"Nenhum registro cadastrado nesta categoria.":"Registros apresentados abaixo."));
        if(values.size()>MAX_GRUPO)avisos.add(group+": exibidos até "+MAX_GRUPO+" de "+values.size()+" registros (ordem definida pelo cadastro).");
        values.stream().limit(MAX_GRUPO).forEach(v->add(rows,group,format.apply(v)));
    }
    @Transactional(readOnly=true)
    public Map<String,Object> contexto(long id){
        var p=autorizado(id);var rows=new ArrayList<Map<String,String>>();var avisos=new ArrayList<String>();
        add(rows,"Cadastro","Processo: "+campo(p.getNumeroProcesso())+"; vara: "+campo(p.getVara())+"; comarca: "+campo(p.getComarca())+"; tribunal: "+campo(p.getTribunal())+"; instância: "+campo(p.getInstancia()));
        add(rows,"Cadastro","Data de abertura: "+campo(p.getDataAbertura())+"; encerramento registrado: "+campo(p.getDataEncerramento())+"; prazo processual registrado: "+campo(p.getPrazoProcessual())+".");
        var c=p.getClassificacao();
        add(rows,"Classificação",c==null?"Classificação não cadastrada.":"Status cadastrado: "+campo(c.getStatus())+"; fase: "+campo(c.getFaseProcessual())+"; área: "+campo(c.getAreaDireito())+"; tipo de ação: "+campo(c.getTipoAcao()));
        if(c!=null)add(rows,"Classificação","Objeto cadastrado: "+campo(c.getDescricaoObjeto()));
        group(rows,avisos,"Prazos e tarefas",p.getTarefas().stream().sorted(Comparator.comparing(Tarefa::getPrazo,Comparator.nullsLast(Comparator.naturalOrder()))).toList(),t->"Tarefa #"+t.getIdTarefa()+": "+campo(t.getTipoTarefa())+"; prazo registrado: "+campo(t.getPrazo())+"; status: "+campo(t.getStatus())+"; prioridade: "+campo(t.getPrioridade()));
        group(rows,avisos,"Partes",p.getPartesEnvolvidas().stream().sorted(Comparator.comparingLong(ParteEnvolvida::getIdParte)).toList(),t->"Parte #"+t.getIdParte()+": posição do cliente: "+campo(t.getPosicaoCliente())+"; parte contrária: "+campo(t.getParteContraria())+"; advogado contrário: "+campo(t.getAdvogadoContrario())+"; juiz responsável: "+campo(t.getJuizResponsavel()));
        group(rows,avisos,"Documentos",p.getDocumentos().stream().sorted(Comparator.comparing(DocumentoJuridico::getIdDocumento).reversed()).toList(),t->"Documento #"+t.getIdDocumento()+": "+campo(t.getNome())+"; cadastro: "+campo(t.getDataCadastro())+"; tipo: "+campo(t.getTipoArquivo())+". Somente metadados; conteúdo não consultado.");
        group(rows,avisos,"Movimentações",p.getMovimentacoes().stream().sorted(Comparator.comparing(MovimentacaoProcessual::getData,Comparator.nullsLast(Comparator.reverseOrder()))).toList(),t->"Movimentação #"+t.getIdMovimentacao()+": "+campo(t.getData())+"; descrição: "+campo(t.getDescricao()));
        return Map.of("processo_id",p.getIdProcesso(),"numero",campo(p.getNumeroProcesso()),"consultado_em",Instant.now().toString(),"registros",rows,"avisos",avisos);
    }
}
