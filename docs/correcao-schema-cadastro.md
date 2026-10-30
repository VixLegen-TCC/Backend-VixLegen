# Correção do cadastro e da geração indevida de colunas no VixLegen

## Causa confirmada (logs de 30/10/2026)
O cadastro `POST /auth/cadastro` falhava com HTTP 500: Hibernate consultava
`categoria.codigo_categoria`, enquanto o SQL legado criava
`categoria.codigoCategoria`. O `ddl-auto=update` tentou criar outra coluna
`AUTO_INCREMENT` e o MySQL recusou (só uma coluna automática e ela precisa
ser chave). Em `notificacao` aconteceu conflito semelhante.

Além disso, o SQL legado usa `Usuario` e `Cliente`, enquanto as entidades
atuais usam `usuarios` e `clientes` (nomes fisicamente distintos).

## Alterações neste PR
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
6. Atualize seu clone (`git pull` depois da aprovação/merge), mantendo
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
