# Chat contextual e observabilidade (preparação)

## Endpoints autenticados (JWT)

- `GET /chat/contexto/processo/{id}` consulta dados cadastrais do processo da empresa atual.
- `GET /chat/contexto/notificacao/{id}` consulta somente notificações pertencentes ao usuário.
- `GET /chat/contexto/documento/{id}` lê o conteúdo do documento vinculado a um processo da empresa atual.
- `POST /chat/contexto/arquivo` com campo multipart `arquivo` extrai texto de PDF pesquisável ou TXT sem necessidade de salvar o upload.

Todos exigem escopo de visualização. Nunca aceitar empresa/usuário informados pelo cliente como autoridade: o `TenantAccessService` usa o JWT. O upload tem limite de 10 MB, PDFs têm limite de 100 páginas, a saída textual é truncada em 100 mil caracteres. A extração PDF não oferece OCR. Não adicionar DOCX, OCR ou arquivos binários ao RAG sem uma validação específica.

### Exemplos

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/chat/contexto/notificacao/1
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/chat/contexto/processo/1
curl -H "Authorization: Bearer $TOKEN" -F "arquivo=@teste.pdf" http://localhost:8080/chat/contexto/arquivo
```

### Próxima integração com RAG
O frontend deve permitir selecionar uma fonte interna (tipo/id) ou anexar um arquivo. O serviço RAG deverá consumir o texto extraído **internamente pelo servidor**, após validar novamente o acesso, sem exigir download. Não retornar o texto integral para clientes sem necessidade. A API criada por enquanto expõe texto ao frontend e **não gera resposta de IA**, não faz vetorização e não persiste arquivos. Para documentos sigilosos, complementar autorização por profissional responsável/participante, e revisar a política de retenção e envio de dados ao fornecedor de IA antes de integrar.

## Erros e logs
O `GlobalExceptionHandler` responde JSON com `timestamp`, `status`, `codigo`, `mensagem`, `erroId`, `rota` e, em erro de validação, `campos`. Faça pesquisa do `erroId` nos logs do Spring. Evite registrar parâmetros, corpos de requisição, tokens e conteúdo jurídico. Autenticação JWT inválida pode ser rejeitada pelo filtro Spring Security antes do ControllerAdvice e requer handlers próprios para padronização total.

## Pendências de validação
Executar compilação e testes na máquina/CI com Java 26, Spring Boot 4.1 e MySQL. Testar multipart acima do limite, conteúdo PDF inválido, PDF sem texto, notificações de outro usuário e processos/documentos de outra empresa. Garantir alinhamento da versão transitive PDFBox ao openhtmltopdf atual. Não considerar esta preparação como chatbot RAG implementado.
