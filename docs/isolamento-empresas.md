# Segurança multiempresa — fechamento de autorização por objeto

## Controle de acesso

Todos os IDs em rotas de tarefas, documentos, IA, movimentações, partes e classificações são consultados pelo ID da empresa derivada exclusivamente do JWT. Consultas por ID devolvem 404 para recurso externo ou inexistente; respostas `GET` de listagem retornam exclusivamente os dados do escritório autenticado. Não confundir a presença de `SCOPE_visualizar` com acesso a dados de outros escritórios.

- **Tarefas:** criar/atribuir exige processo e usuário da própria empresa; ler/alterar/excluir verifica empresa do processo.
- **Documentos, minutas/PDF, IA, partes, movimentações, classificações:** as consultas por ID e listagens filtram a empresa. Vínculos de processo enviados pelo navegador são conferidos com `TenantAccessService.processo` antes de persistir.
- **Usuários:** consultas administrativas, edição, alteração de categoria, ativação/desativação e exclusão ficam limitadas ao escritório autenticado. Cadastro administrativo integra a empresa atual; cadastro público continua criando uma empresa isolada. Admin não pode excluir a própria conta.
- **Notificações:** o painel pessoal só consulta a própria conta; funções administrativas só acessam usuários e notificações da própria empresa.
- **Colaboradores:** recebe a coluna `empresa_organizacao_id`, preenchida pelo backend na criação. Colaboradores antigos sem empresa **não aparecem** até que sejam migrados de forma conferida. A API não aceita uma empresa passada pelo frontend.
- **Categorias e categorias de documentos:** as entidades são globais no banco, sem campo empresa. Para evitar que um admin local altere papéis de todos os escritórios, **POST/PUT/PATCH/DELETE foram bloqueados com 403** até implementarmos dados por organização; leitura continua possível conforme papéis vigentes.

## Implantação

1. Fazer backup do MySQL.
2. Validar a nova coluna nullable `colaboradores.empresa_organizacao_id`, gerada por Hibernate (`ddl-auto=update` atual). Planejar Flyway para produção.
3. Não atribuir colaboradores órfãos automaticamente por nome da empresa. Migrar somente após confirmação administrativa do vínculo.
4. Após backend e frontend atualizados, autenticar em dois escritórios distintos e testar `GET`, `PUT`, `PATCH`, `DELETE` com um ID existente do outro escritório: deve responder **404**. A consulta da própria empresa deve continuar funcionando.
5. Testar criação de tarefa com usuário/processo externo, download de PDF externo, editor de documentos externo, `/inteligencia-artificial`, notificações de outro usuário e listagem de usuários via admin de outra empresa.
6. Confirmar JWT_SECRET seguro, HTTPS, e que chave/CPF/textos jurídicos não vão para logs.

**Escopo remanescente de segurança operacional:** ainda precisamos proteger o frontend contra XSS (especialmente HTML das minutas), executar testes ponta a ponta em navegador, criar migrations explícitas para o banco, e configurar backup/HTTPS/deploy. Este PR fecha a autorização multiempresa na API, não equivale a auditoria de segurança formal nem a pentest.
