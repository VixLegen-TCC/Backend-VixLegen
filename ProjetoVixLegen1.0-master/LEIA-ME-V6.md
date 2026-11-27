# VixLegen — V6 (primeira integração HTTP)

Patch aditivo baseado na branch `main` do repositório `VixLegen-TCC/Backend-VixLegen`, árvore `4f7006e549f417847b826ab562eb20ed2bb4462d`. O pom declara Java 26 e Spring Boot 4.1.0. Nenhum arquivo existente foi substituído.

## Instalação

1. Faça backup/commit do backend e extraia o ZIP **na raiz do repositório**, acima de `ProjetoVixLegen1.0-master`. Os sete arquivos Java novos já estão na estrutura correta.
2. Mantenha a V5 em execução no PC (`127.0.0.1:8000`). Não rode outro processo RAG sobre o mesmo banco durante a integração. Não reindexe o PDF.
3. O Java usa a configuração padrão `rag.url=http://127.0.0.1:8000`, `rag.timeout-segundos=660`. É possível definir em `application.properties` se necessário; não substitua o arquivo original. Timeout de conexão: 3 segundos. Não há dependência Maven adicional prevista.
4. Para habilitar a autenticação entre serviços, defina `RAG_API_KEY` com o mesmo segredo aleatório forte no ambiente dos dois processos ANTES de iniciá-los. Não use o segredo JWT como chave da IA, não versionar chaves e não envie a chave ao navegador. A configuração permite chave vazia apenas para desenvolvimento compatível com a V5 atual. O FastAPI deve continuar restrito ao loopback. A chave compartilhada não autentica cada usuário do VixLegen; isso é feito pelo Spring.
5. Na pasta que contém `pom.xml`, com JDK 26 e Maven configurados, execute `mvn test` e `mvn spring-boot:run` (ou use seu IDE). Configure o MySQL e o JWT como já faz atualmente. O wrapper do repositório pode depender de arquivos locais não versionados; se `mvnw.cmd` falhar, use Maven instalado.

## Endpoints Spring Boot (porta 8080, se não alterada)

| Método | Caminho | Escopo atual do SecurityConfig |
|---|---|---|
| GET | /chat/ia/health | visualizar |
| POST | /chat/ia/sessoes | editar |
| POST | /chat/ia/sessoes/{id}/mensagens | editar |
| POST | /chat/ia/sessoes/{id}/reset | editar |
| POST | /chat/ia/sessoes/{id}/encerrar | editar |

Mantive as permissões atuais, sem editar o SecurityConfig. Se o produto permitir chat para perfis de somente leitura, será necessário decidir essa regra e adicionar matchers específicos antes dos POST genéricos. O encerramento atua só sobre conversa temporária, não sobre registros MySQL. O frontend envia `Authorization: Bearer <JWT do VixLegen>` e nunca o segredo do RAG. Use token de um usuário real com organização ativa e as permissões acima.

## Teste PowerShell

Com o Java e o Python em execução:

```powershell
$base = 'http://localhost:8080/chat/ia'
# Cole seu token localmente (não compartilhe em logs/chat).
$token = Read-Host 'JWT do VixLegen'
$h = @{ Authorization = "Bearer $token" }
Invoke-RestMethod "$base/health" -Headers $h
$s = Invoke-RestMethod "$base/sessoes" -Headers $h -Method Post
function Perguntar([string]$mensagem) {
    $json = @{ mensagem = $mensagem } | ConvertTo-Json
    $r = Invoke-WebRequest -UseBasicParsing -Method Post `
        -Uri "$base/sessoes/$($s.conversa_id)/mensagens" -Headers $h `
        -ContentType 'application/json; charset=utf-8' `
        -Body ([Text.Encoding]::UTF8.GetBytes($json))
    [Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray()) | ConvertFrom-Json
}
(Perguntar 'O que estabelece o artigo 5º da Constituição Federal?').resposta
(Perguntar 'E o inciso II?').resposta
(Perguntar 'Me dê um exemplo').resposta
```

Teste também `POST /chat/ia/sessoes/{id}/reset`, seguido de uma pergunta ambígua; e `POST /chat/ia/sessoes/{id}/encerrar` (204). Para verificar isolamento, use outro JWT, de outro usuário, e tente enviar mensagem usando o UUID anterior: espere 404. Faça o mesmo sem JWT: espere 401. Uma sessão já ocupada pode retornar 409, e a V5 ocupada 429. Não repita automaticamente uma pergunta após timeout: o processamento pode ter terminado no Python mesmo sem resposta ao Java.

## Escopo e limitações

- O UUID público gerado pelo Java é diferente do UUID do Python. O vínculo é conferido em todas as operações usando usuário e empresa do JWT/tenant, nunca do corpo da requisição. O registro Java fica em RAM, limitado a 100 sessões e uma hora de inatividade, sem persistência. Um restart de qualquer serviço pode exigir nova conversa; sessões órfãs no Python expiram pelo TTL. Se a V5 informar sessão inexistente, o Java descarta o vínculo e retorna 410. Não é uma implementação distribuída.
- O corpo completo da resposta V5 é preservado como mapa, substituindo `conversa_id` pelo UUID público. Assim não se perdem fontes, explicações, avisos, status e metadados. DTO de entrada limita a mensagem a 4000 caracteres. Não use `validacao` como prova de correção jurídica.
- Não altera `IAController`, `IAService`, `ChatContextoService`, MySQL, `pom.xml` ou segurança atual. Não usa dados privados de processos/clientes nem encaminha uploads ao `/ingest` global. O índice da V5 é compartilhado; para documentos privados é necessário isolamento de recuperação no Python, além da autorização Java.
- Este pacote é a primeira integração síncrona (JSON). SSE no Spring, persistência de chats e frontend ainda não foram implementados. O timeout de 660 segundos é uma escolha inicial; pode ser necessário ajustar de acordo com o tempo de geração observado. O RAG V5 e seu SSE existentes não foram modificados.
- O conector GitHub permitiu leitura, não escrita. Nenhuma branch/commit/PR foi criado. Não foi possível compilar o projeto Spring completo neste ambiente: somente JDK 21 disponível, Maven ausente e acesso de rede indisponível para baixar dependências. As dez verificações do registro Java puro foram executadas com javac/java 21; não representam testes HTTP, Spring Security ou integração com MySQL. Faça os testes reais acima antes de usar o módulo em produção.

## Verificação local independente

Na raiz do repositório após extrair, é possível compilar o registro sem Spring, com `javac`:

```powershell
$src = 'ProjetoVixLegen1.0-master/ProjetoVixLegen1.0/src/main/java/br/com/VixLegen/ProjetoVixLegen10/Service/RagSessaoRegistro.java'
javac -encoding UTF-8 -d verificacao/classes $src verificacao/RagSessaoRegistroSmoke.java
java -cp verificacao/classes br.com.VixLegen.ProjetoVixLegen10.Service.RagSessaoRegistroSmoke
```
