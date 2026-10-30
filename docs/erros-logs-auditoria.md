# VixLegen — exceptions, erros 401/403, logs e auditoria

## Respostas de erro

Os erros da API retornam JSON com `timestamp`, `status`, `codigo`, `mensagem`, `erroId`, `rota`, e `campos` (somente quando houver validação por campo). O campo `rota` é um **padrão** (ex.: `/tarefas/{id}`) e não contém valores de CPF/e-mail/IDs informados na URL.

| HTTP | Código | Significado |
|---|---|---|
| 400 | `PARAMETRO_INVALIDO` / `VALIDACAO` / `REQUISICAO_INVALIDA` | ID, body ou parâmetro incorreto |
| 401 | `AUTENTICACAO_NECESSARIA` / `CREDENCIAIS_INVALIDAS` | Sem JWT, JWT inválido ou credenciais erradas |
| 403 | `ACESSO_NEGADO` | Autenticado sem permissão |
| 404 | `RECURSO_NAO_ENCONTRADO` / `ROTA_NAO_ENCONTRADA` | Objeto ou rota ausente |
| 405 | `METODO_INVALIDO` | Método HTTP errado |
| 406 | `FORMATO_RESPOSTA_INVALIDO` | Accept não suportado |
| 409 | `CONFLITO_INTEGRIDADE` / `CONFLITO_RECURSO` | Duplicidade, vínculo ou conflito de negócio |
| 413 | `ARQUIVO_GRANDE` | Multipart superior a 10 MB |
| 415 | `TIPO_MIDIA_INVALIDO` | Content-Type não permitido |
| 422 | `ARQUIVO_INVALIDO` | Arquivo não suportado, corrompido ou sem texto |
| 500 | `ERRO_INTERNO` | Falha inesperada |
| 503 | `IA_INDISPONIVEL` | Exception preparada para integração futura |
| 504 | `IA_TEMPO_ESGOTADO` | Exception preparada para integração futura |

**Atenção:** as exceptions 503/504 estão definidas, mas não há cliente RAG ativo. A mudança não implementa conexão com IA.

### Investigação de falhas

1. Copie o `erroId` da resposta (não precisa expor token, CPF ou payload).
2. Procure-o no console ou no arquivo `logs/vixlegen.log`, relativo ao diretório de execução.
3. O registro identifica status, código, rota sem parâmetros, usuário interno (quando autenticado) e classe do erro. Stack traces de falhas 5xx ficam **somente nos logs do servidor**.
4. Log de operação concluída: `auditoria=OPERACAO_CONCLUIDA usuarioId=... recurso=tarefas acao=EXCLUIR recursoId=... status=204 duracaoMs=...`. Só são auditados POST/PUT/PATCH/DELETE com resposta 2xx nas rotas MVC de clientes, processos, tarefas e documentos.
5. Requisições MVC com 2 segundos ou mais geram `requisicao_lenta`. O limite pode ser configurado por `app.logs.slow-ms`.

Os arquivos de log giram a 10 MB, têm histórico de 14 dias e limite total de 300 MB. É possível escolher o arquivo com `LOG_FILE`. Garanta permissões restritas no diretório: esses logs não devem ser publicados ou expostos por endpoints. `.gitignore` impede adicionar arquivos .log por acidente. Os logs de auditoria são **operacionais em arquivo**: não são uma trilha inviolável persistida em banco.

### Testes manuais

```bash
# Sem token: 401 com JSON e erroId
curl -i http://localhost:8080/tarefas

# ID não numérico com JWT válido: 400
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8080/tarefas/invalido

# TXT pesquisável: 200, sem salvar cópia
curl -i -H "Authorization: Bearer $TOKEN" -F "arquivo=@caso.txt" http://localhost:8080/chat/contexto/arquivo

# Deve responder 415: a rota espera multipart/form-data
curl -i -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{}' http://localhost:8080/chat/contexto/arquivo
```

Teste também: token inválido (401), perfil sem escopo (403), upload superior a 10 MB (413), PDF inválido (422), exclusão válida (204), e isolamento entre empresas (404).

## Compilação

Na raiz Maven (`ProjetoVixLegen1.0-master/ProjetoVixLegen1.0`) com **JDK 26** configurado:

```bash
./mvnw test
```

No Windows: `mvnw.cmd test`.

O PR #19 foi integrado e a suíte do GitHub Actions passou com JDK 26 e MySQL temporário em 30/10/2026. O código atual usa `spring.jpa.show-sql=false` e `spring.jpa.hibernate.ddl-auto=none`. Na base local, continue coletando o `erroId` para diagnosticar possíveis incompatibilidades legadas de schema. Testes manuais no navegador e no banco real da equipe ainda são necessários.

A autenticação/escopos e as permissões anteriores de exclusão permanecem intactos. Segundo a equipe, a IA foi desenvolvida separadamente por Raphael (PH); sua **integração** ao backend/frontend do VixLegen continua pendente. Veja [estado atual do projeto](estado-projeto-2026-10-30.md).
