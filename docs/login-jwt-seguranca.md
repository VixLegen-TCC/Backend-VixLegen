# Segurança JWT e login — VixLegen

## Mudanças de autenticação

- JWT HS256 assinado pela variável **obrigatória** `JWT_SECRET`; sem fallback versionado.
- Validação em cada chamada autenticada: assinatura, issuer, validade, audience, conta ativa, organização e categoria atuais, permissão e perfil atuais, e `authVersion` do usuário.
- Tokens antigos (sem `aud`/`authVersion`) **deixam de funcionar**: usuários precisam entrar novamente após o deploy.
- `POST /auth/logout` revoga **todas** as sessões da conta, de qualquer aparelho; a implementação é via versão de sessão no banco e não lista individual de tokens.
- Alterações de categoria, atividade e e-mail invalidam tokens emitidos anteriormente.
- Login limitado em memória: cinco falhas por conta ou trinta por IP, dentro de 15 minutos. Depois retorna HTTP 429 `LOGIN_BLOQUEADO` e `Retry-After: 900`. Para múltiplas réplicas, migrar o limitador para Redis/Bucket4j.
- Respostas 401 e 403 continuam com `erroId`.
- Os scopes continuam `visualizar`, `editar`, `excluir`, incluindo a regra de exclusão de tarefas do PR #14.
- JWT não carrega e-mail nem nome; estas informações continuam na resposta inicial do login.

## Configuração obrigatória ANTES de subir o backend

No Windows PowerShell, exemplo de ambiente local (não registre o segredo em arquivos versionados):

```powershell
$env:DB_USERNAME = "seu_usuario_mysql"
$env:DB_PASSWORD = "sua_senha_mysql"
$env:JWT_SECRET = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
$env:JWT_AUDIENCE = "vixlegen-web"
# Na pasta que contém pom.xml e com JDK 26 ativo:
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Use o **mesmo** `JWT_SECRET` em todos os reinícios do servidor, e guarde-o em gerenciador de segredos; regenerar invalida todas as sessões. No Linux/macOS, use exportações equivalentes. Alterar a chave antiga é necessário porque a fallback era pública no histórico Git. Não use a antiga novamente. Utilize HTTPS em rede externa.

O campo `auth_version` foi adicionado ao modelo `usuarios` como coluna `Long` anulável, preservando cadastros anteriores: usuários antigos com NULL são tratados como versão 0. Migrations devem ser preparadas antes de substituir o `ddl-auto=update` atual.

## Rotas

```http
POST /auth/login
Content-Type: application/json

{"email":"advogado@exemplo.com","senha":"senha-exemplo"}
```

```http
POST /auth/logout
Authorization: Bearer TOKEN_VALIDO
```

O logout responde 204, e tokens anteriores retornam 401, inclusive em outros dispositivos. Não requer `SCOPE_editar`: qualquer conta autenticada pode encerrar suas sessões.

## Testes para validar após o merge

1. Senha errada: 401; após cinco falhas seguidas: 429.
2. Token ausente ou assinatura incorreta: 401 com `erroId`.
3. Token expirado: 401; token novo: acesso permitido.
4. Usuário desativado no banco: token anterior rejeitado imediatamente; ao reativar, exige novo login.
5. Categoria/permissões reduzidas: token antigo rejeitado; login novo recebe os scopes atualizados.
6. Após `POST /auth/logout`: o token anterior falha com 401 em `GET /auth/me`.
7. Tarefa só pode ser excluída por quem tem permissão de edição ou exclusão e pertence à empresa.
8. Cadastro, foto, clientes, processos e notificações continuam funcionando.
9. Certifique-se que `JWT_SECRET`, senhas e Bearer tokens nunca aparecem em logs.

## Limitações e próximos passos

O limitador é **local à instância** e não protege tráfego distribuído nem todos os vetores de enumeração. Para produção, criar controle distribuído, métricas, proteção de cadastro público e autenticação HTTPS com cookies HttpOnly/Secure/SameSite e proteção CSRF (projeto atual usa Bearer token). O frontend passa a usar `sessionStorage`: isso reduz persistência em disco, mas **não impede roubo por XSS**.

Também restam revisões de autorização **por organização** em alguns Services; validação de JWT não substitui regras de acesso a cada recurso.

Essas mudanças foram feitas numa branch para revisão. Executar `mvnw.cmd test` no JDK 26 e testes de integração com MySQL antes do merge: este ambiente não dispõe do toolchain e serviço MySQL necessários para validar a compilação completa.
