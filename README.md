# VixLegen — Backend

API do sistema de gestão jurídica VixLegen, construída em Java/Spring Boot com persistência MySQL e autenticação JWT.

## Documentação atualizada (30/10/2026)

**[Situação oficial do projeto, arquitetura, funcionalidades e pendências](docs/estado-projeto-2026-10-30.md)**

Materiais técnicos:
- [Banco de dados, cadastro e reparo de schema](docs/correcao-schema-cadastro.md)
- [Separação de dados por escritório](docs/isolamento-empresas.md)
- [Login JWT e segurança](docs/login-jwt-seguranca.md)
- [Chat contextual e preparação da IA externa](docs/chat-contexto-logs.md)
- [Erros e auditoria](docs/erros-logs-auditoria.md)
- [Recuperação de vínculos legados](docs/recuperacao-clientes-processos.md)

## Estado resumido

- O PR [#19](https://github.com/VixLegen-TCC/Backend-VixLegen/pull/19) foi integrado em 30/10/2026: nomes de colunas explícitos, cadastro e `ddl-auto=none`.
- Segundo a equipe, o cadastro voltou a funcionar após o reparo no MySQL de uma coluna legada `numerooab`. Essa correção SQL foi **local**; outras bases podem exigir migração.
- Cada **novo cadastro público cria sua própria empresa** e `id_empresa`. Nomes idênticos não unificam escritórios. O escopo da API usa o ID da organização.
- A IA foi desenvolvida **separadamente por Raphael (PH)**, segundo a equipe. A integração com a API e o frontend ainda não está concluída. A extração de contexto (PDF/TXT e recursos internos) já existe.
- Testes automatizados do backend passaram no GitHub Actions após o PR #19. Testes end-to-end no banco real continuam necessários.

## Execução local

Pasta do projeto Maven: `ProjetoVixLegen1.0-master/ProjetoVixLegen1.0/`.

Requisitos: JDK 26, Maven compatível e MySQL, com schema compatível com as entidades (o backend **não** cria tabelas automaticamente).

Variáveis de ambiente necessárias:
- `DB_USERNAME` e `DB_PASSWORD` — credenciais MySQL.
- `DB_NAME` — opcional, padrão `VixLegenDB` (use `VixLegenDB_V2` se esse for o schema criado).
- `JWT_SECRET` — segredo forte, definido fora do repositório e mantido estável entre reinícios.
- `FRONTEND_ORIGINS` — se o frontend usar uma origem diferente das permitidas por padrão.

Na pasta Maven, execute `mvn test` e `mvn spring-boot:run`. O arquivo de configuração atual contém `spring.jpa.hibernate.ddl-auto=none`: alterações no schema exigem SQL/migrations executados de forma explícita, com backup prévio.

**Não execute o SQL histórico `vixlegen.sql` diretamente em uma base com dados**: ele contém comando para excluir o banco antigo. Consulte a documentação de migração acima.

Frontend: https://github.com/VixLegen-TCC/Frontend-VIx
