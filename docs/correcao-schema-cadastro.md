# Correção do cadastro e da geração indevida de colunas no VixLegen

## Causa confirmada (logs de 30/10/2026)
O cadastro `POST /auth/cadastro` falhava com HTTP 500: Hibernate consultava
`categoria.codigo_categoria`, enquanto o SQL legado criava
`categoria.codigoCategoria`. O `ddl-auto=update` tentou criar outra coluna
`AUTO_INCREMENT` e o MySQL recusou (só uma coluna automática e ela precisa
ser chave). Em `notificacao` aconteceu conflito semelhante.

Além disso, o SQL legado usa `Usuario` e `Cliente`, enquanto as entidades
atuais usam `usuarios` e `clientes` (nomes fisicamente distintos).

## Alterações integradas no PR #19 (30/10/2026)
- `spring.jpa.hibernate.ddl-auto=none`: a aplicação NÃO altera mais
  o esquema por conta própria na inicialização.
- Nomes de colunas explícitos em `Categoria` e ids/relacionamentos
  relevantes de `Usuario` e `Notificacao`.
- SQL `docs/sql/01_corrigir_categoria_cadastro.sql`: normaliza a tabela
  `categoria` para a nomenclatura atual e garante a categoria pública
  `Advogado Júnior` sem duplicá-la.
- Teste de regressão de mapeamento da categoria.

## Execução segura
1. **Faça backup completo** do banco `VixLegenDB` no MySQL Workbench
   (Server > Data Export). Preferencialmente, guarde-o fora do computador.
2. **Pare a API** antes da migração.
3. Confira se seu banco contém dados em tabelas `Usuario` e `usuarios`.
   Não exclua nem una essas tabelas automaticamente: os IDs e FKs podem diferir.
4. Execute o arquivo `docs/sql/01_corrigir_categoria_cadastro.sql` inteiro
   no MySQL Workbench, com o banco correto selecionado.
5. Se a migração interromper com `SQLSTATE 45000`, há colunas conflitantes
   (ou ausentes). **Não force a execução**. Compare os dados e planeje migração
   específica; renomear/apagar às cegas pode causar perda de dados.
6. Atualize seu clone (`git pull` na `main`), mantendo
   `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` configurados.
7. Reinicie a API e teste cadastro com **e-mail e CPF inéditos**.
   Resultado esperado: HTTP 201 em `POST /auth/cadastro`, seguido de
   login HTTP 200. Verifique `usuarios`, `empresas` e `categoria`.
8. Reinicie a API de novo e confirme que o total de tabelas/colunas não mudou.

## Limites importantes
- O SQL é conservador: **não recria banco**, não elimina tabelas antigas e
  não migra automaticamente dados de `Usuario` para `usuarios`.
- `ddl-auto=none` protege dados existentes, mas pressupõe banco já
  provisionado. Para um banco vazio será preciso um esquema/migration inicial.
- Outros conflitos preexistentes (por exemplo, `notificacao.usuario_id` e
  `notificacao.data_envio`) continuam exigindo reconciliação de dados:
  este script não altera notificações nem converte `status` legados.
- A migração não foi executada no MySQL do usuário; é necessário validar
  com backup e cópia de teste. Sem o `SHOW CREATE TABLE` real não é possível
  garantir que toda instalação esteja no mesmo estado.
- **Nunca rode** o antigo `vixlegen.sql` integralmente: ele começa com
  `DROP DATABASE vixlegendb;` e apagará todos os dados.

## Próxima melhoria recomendada
Criar migrations versionadas (Flyway/Liquibase), depois de consolidar um
schema definitivo. Quando completamente reconciliado, trocar
`ddl-auto=none` por `validate` para falhar cedo em vez de ter erro de SQL
somente durante um endpoint.

## Atualização após o merge — cadastro confirmado pela equipe (30/10/2026)

O PR #19 foi integrado à `main`, e o GitHub Actions concluiu com sucesso após a correção da configuração de banco temporário nos testes. O erro posterior `Field 'numerooab' doesn't have a default value` revelou uma **coluna antiga obrigatória**, diferente de `numero_oab`, que já era preenchida pelo Java.

**Correção no backend:** o campo `Usuario.numeroOAB` passou a usar explicitamente `@Column(name = "numero_oab", nullable = false)`, com teste de mapeamento.

**Correção aplicada no MySQL local:** a equipe executou o reparo que permite `NULL` em `numerooab` somente quando existe também a coluna oficial `numero_oab` e confirmou que o cadastro voltou a funcionar. Esse reparo local **não prova** que outros ambientes tenham o mesmo schema e não substitui migration versionada.

### Como conferir sem alterar tabelas

1. Identifique o banco real da aplicação: `DB_NAME` (padrão `VixLegenDB`; alternativo `VixLegenDB_V2`).
2. Faça backup e execute consultas de diagnóstico:
   - `SELECT DATABASE();`
   - `SHOW COLUMNS FROM usuarios LIKE 'numerooab';`
   - `SHOW COLUMNS FROM usuarios LIKE 'numero_oab';`
3. O backend escreve em `numero_oab`. A coluna legada `numerooab` não deve continuar `NOT NULL` sem default se não é preenchida pelos inserts.
4. **Não exclua ou renomeie colunas automaticamente.** Verifique vínculos, valores antigos, FKs e faça backup antes de qualquer migração.

**Configuração vigente:** `spring.jpa.hibernate.ddl-auto=none`. Só os testes de CI em banco descartável usam criação temporária de schema; isso não modifica a base real.

Documentação consolidada: [estado do projeto](estado-projeto-2026-10-30.md).
