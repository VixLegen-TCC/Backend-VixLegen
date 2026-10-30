# VixLegen — situação atual e pendências do TCC
**Atualizado em:** 30/10/2026  
**Referência:** Backend `main` após PR #19 (merge `1bbbe78`); Frontend `main` (revisão do código em 30/10/2026).

> Este é um retrato do projeto, não um atestado de homologação ou segurança. Separar: funcionalidade **implementada no código**, **confirmada por teste da equipe** e **pendente de teste**. A execução dos testes do CI não comprova o fluxo completo no navegador.

## 1. Resumo executivo
O VixLegen possui frontend em HTML/CSS/JavaScript, backend Spring Boot e banco MySQL. Cadastro público, login JWT, clientes, processos, tarefas, notificações e gerenciamento de minutas possuem implementação no repositório. O cadastro que falhava por inconsistências do schema voltou a funcionar segundo teste informado pela equipe em 30/10/2026.

A inteligência artificial foi **desenvolvida separadamente por Raphael (PH)**, conforme relato da equipe, em outra máquina. O código/modelo, arquitetura, endpoints, requisitos e resultados da IA externa **não foram recebidos ou verificados nesta revisão**. Por isso, a integração com o VixLegen permanece pendente; **não** classificar a IA como “não desenvolvida” nem como “integrada”.

## 2. O que está implementado e o que foi confirmado
| Componente | Estado em 30/10/2026 | Evidência e ressalvas |
|---|---|---|
| Cadastro público | **Funcionando segundo teste da equipe** | Relatado após correção da coluna legada `numerooab` no MySQL. Não é homologação em todos os ambientes. |
| Login JWT e revogação de sessões | **Implementado** | Código e testes de segurança; validar ponta a ponta após toda migração. |
| Banco MySQL | **Correções no backend integradas** | PR #19: mapeamentos explícitos e `spring.jpa.hibernate.ddl-auto=none`. A estrutura real do banco local depende de scripts executados nele. |
| Cadastro de empresas/escritórios | **Implementado** | Cada cadastro público cria nova entidade `empresas` com novo `id_empresa`, mesmo quando o nome já existe. |
| Separação de dados entre escritórios | **Implementada no backend** | Consultas e serviços aplicam escopo de `empresa_organizacao_id`, derivado do JWT; testes automatizados de isolamento existem. Ainda recomendável teste manual adversarial. |
| Clientes, processos e tarefas | **Implementados** | Validar CRUD, vínculos, exclusões e consulta após reinicialização. |
| Notificações de prazo | **Implementadas** | Verificação periódica a cada 15 min (configurável) e geração por consulta; janelas 7/3/1 dia, hoje e vencido. |
| Minutas/documentos e PDF | **Implementados** | Editor no frontend, API para salvar e exportar PDF; revisar resultados no navegador e formatação. |
| Extração de contexto de PDF/TXT para chat | **Implementada no backend** | `/chat/contexto/...`, sem OCR de PDF escaneado. |
| IA externa do PH | **Desenvolvida externamente, segundo equipe** | Integração e testes com o backend/frontend ainda pendentes. |

O workflow `Backend JWT Tests` passou na execução posterior à correção do PR #19. Isso valida a suíte configurada no GitHub Actions, não a instalação local nem todos os recursos da interface.

## 3. Arquitetura de organizações
- **Novo cadastro público = nova empresa.** O backend cria conta e organização e relaciona ambas.
- O vínculo de acesso é o **ID** da empresa, **não** o texto do nome. Exemplo: duas pessoas que informam “Silva Advocacia” recebem IDs de empresa diferentes e não devem compartilhar dados.
- Endpoints de entidades por escritório consultam o escopo extraído do usuário autenticado; o frontend não escolhe a empresa via parâmetro de autoridade.
- O cadastro atual **não** une automaticamente novos advogados a uma empresa de mesmo nome. Se for necessário equipe de múltiplos usuários no mesmo escritório, será preciso implementar vínculo administrado/convites.
- Categorias e categorias de documento têm caráter global, e operações administrativas de alteração estão bloqueadas até haver desenho próprio por organização.
- **Ponto de atenção confirmado:** CPF e CNPJ do modelo `Cliente` estão únicos no banco todo. Escritórios diferentes não conseguem cadastrar o mesmo documento simultaneamente, apesar do isolamento das empresas. Revisar a regra de negócio e, caso necessário, migrar para unicidade por organização.

## 4. Banco de dados — histórico e situação
### Erros investigados
1. SQL legado com nomenclatura divergente (ex.: `codigoCategoria` versus `codigo_categoria`, `Usuario` versus `usuarios`); `ddl-auto=update` criava novas colunas/tabelas involuntárias.
2. Cadastro com erro `Field 'numero_oab' doesn't have a default value` e depois `Field 'numerooab' doesn't have a default value`. O Java agora usa explicitamente `@Column(name = "numero_oab")`. A coluna legada `numerooab` ainda obrigatória bloqueava o INSERT mesmo quando a coluna oficial recebia valor.
3. A equipe confirmou que o cadastro voltou a funcionar após aplicar uma correção SQL **local** que permite NULL em `numerooab` quando ambas as colunas estão presentes.

### Padrão atual
- Propriedade de desenvolvimento/execução: `spring.jpa.hibernate.ddl-auto=none`. Isso **não cria tabelas** nem atualiza o schema automaticamente.
- Seleção do banco: variável `DB_NAME` (padrão `VixLegenDB`), além de `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`.
- O script versionado no repositório `docs/sql/01_corrigir_categoria_cadastro.sql` trata especificamente a categoria legada. Ele **não** migra todo o banco nem corrige automaticamente qualquer coluna.
- Um SQL para banco limpo `VixLegenDB_V2` e um reparo pontual de `numerooab` foram fornecidos separadamente na conversa. **Não supor que ambos foram executados, nem que estão versionados no GitHub.** Verificar qual banco está configurado no ambiente da equipe.
- O `vixlegen.sql` histórico contém `DROP DATABASE` e **não deve ser executado** sobre uma base com dados importantes.
- Próxima evolução: baseline/controle de migrations com Flyway ou Liquibase, backup verificável e homologação em cópia do banco antes de alterar a estrutura. Depois de consolidar schema, avaliar `ddl-auto=validate` para detectar divergências no startup.

**Importante:** desativar alterações automáticas resolve a origem das duplicações, mas não elimina colunas legadas já presentes. Não excluir `numerooab` nem qualquer coluna com dados sem backup e inspeção.

## 5. Pendências reais (ordem de prioridade)
### Alta
1. **Homologar fluxos completos** no banco efetivamente usado pela equipe: novo cadastro → login → cliente → processo → classificação → tarefa → alerta → minuta/PDF → logout → novo login. Conferir persistência após recarga.
2. **Consolidar schema e migrations**: identificar banco ativo, inventariar tabelas e colunas extras, criar scripts incrementais e testar restauração de backup.
3. **Testar isolamento por empresa** com dois usuários reais e IDs cruzados (GET, PUT, PATCH, DELETE, download PDF). A arquitetura já existe; é validação contra regressão, não uma nova implementação.

### Média
4. **Integrar a IA desenvolvida pelo PH**: receber projeto, requisitos de execução, formato de entrada/saída, endereço/contrato da API, proteção de dados jurídicos, autenticação, limites e tratamento de indisponibilidade; integrar backend e frontend; testar resultado real.
5. **Persistir edição dos dados da empresa**: a rotina atual do `script.js` altera campos na página e mostra sucesso sem chamar a API para salvá-los. Definir quais campos pertencem à entidade `Empresa` e criar/usar endpoint adequado.
6. **Reduzir duplicações do frontend**: `script.js` ainda contém interações locais antigas de tarefas, notificações e exportação, ao lado de `integration.js`, que executa operações via API. Revisar listeners duplicados e deixar uma única fonte de verdade.
7. **Revisar a regra de duplicidade CPF/CNPJ de clientes** entre escritórios (se compartilham identidade documental, mas não registros).

### Finalização/qualidade
8. Validar limites de upload, fotos de perfil, minutas sanitizadas e formato do PDF; corrigir usabilidade e mensagens confusas.
9. Atualizar documentação técnica e testes, preparar ambiente de apresentação, HTTPS/segredos, backup e procedimentos de recuperação.
10. Decidir se a primeira versão será multiusuário por escritório; a versão atual cria uma empresa por cadastro público, mesmo com nomes iguais.

## 6. Critérios de aceitação para encerramento
- [ ] Cadastro novo persiste `usuarios` e `empresas`, sem HTTP 409/500 por colunas legadas.
- [ ] Reiniciar backend não muda total de tabelas/colunas.
- [ ] Uma conta não vê registros de outra empresa ao consultar IDs conhecidos.
- [ ] Clientes, processos, tarefas e documentos continuam após atualizar a página e entrar novamente.
- [ ] Excluir processo respeita dependências; não deixa dados incoerentes.
- [ ] Notificações de prazo são emitidas na janela correta sem duplicação.
- [ ] Exportar e reabrir minutas/PDF conserva conteúdo esperado.
- [ ] Dados editados da empresa persistem (após implementar endpoint).
- [ ] Chat usa resposta real da IA do PH (após integração), com tratamento de falhas e proteção de dados.
- [ ] Backup, migração e restauração testados antes de apresentação ou entrega.

## 7. Fontes do projeto
- Backend: https://github.com/VixLegen-TCC/Backend-VixLegen
- Frontend: https://github.com/VixLegen-TCC/Frontend-VIx
- PR #19 (mapeamento e schema): https://github.com/VixLegen-TCC/Backend-VixLegen/pull/19
- Documentos técnicos: `docs/correcao-schema-cadastro.md`, `docs/isolamento-empresas.md`, `docs/login-jwt-seguranca.md` e `docs/chat-contexto-logs.md`.

*Nota de rastreabilidade:* “a IA está pronta em outra máquina” e “o cadastro está funcionando” foram informados pela equipe; os demais estados foram conferidos no código, docs e CI disponíveis em 30/10/2026.
