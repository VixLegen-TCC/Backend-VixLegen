# Autorização e perfis de acesso

## Perfis

Os perfis são derivados do campo `nivelAcesso` da Categoria:

| nivelAcesso | Perfil |
|---|---|
| 1 | ADMIN |
| 2 | ADVOGADO_SENIOR |
| 3 | ADVOGADO_JUNIOR |
| 4 | ESTAGIARIO |

O JWT recebe o perfil no claim `role`, além dos scopes baseados em
`permissaoVisualizar`, `permissaoEditar` e `permissaoExcluir`.

## Permissões gerais

| Perfil | Visualizar | Editar | Excluir |
|---|---:|---:|---:|
| Administrador Geral | Sim | Sim | Sim |
| Advogado Sênior | Sim | Sim | Sim |
| Advogado Júnior | Sim | Sim | Não |
| Estagiário | Sim | Não | Não |

Nos recursos jurídicos comuns:

- GET exige `SCOPE_visualizar`;
- POST, PUT e PATCH exigem `SCOPE_editar`;
- DELETE exige `SCOPE_excluir`.

## Regras administrativas

### Administrador Geral

É o único perfil que pode:

- criar usuários;
- alterar dados de usuários;
- alterar a categoria de um usuário;
- ativar ou desativar usuários;
- excluir usuários;
- criar, alterar ou excluir categorias;
- alterar permissões e limites de uma categoria.

### Advogado Sênior

Pode consultar usuários, mas não pode:

- criar usuários;
- alterar dados de usuários;
- trocar categoria;
- ativar/desativar usuários;
- excluir usuários;
- administrar categorias ou permissões.

Nos demais recursos jurídicos, segue as permissões gerais da Categoria.

### Advogado Júnior

Não administra usuários nem categorias.
Nos recursos jurídicos pode visualizar e editar, mas não excluir.

### Estagiário

Não administra usuários nem categorias.
Nos recursos jurídicos possui apenas visualização.

## Endpoints administrativos de usuário

- `POST /usuarios` — cria usuário;
- `PUT /usuarios/{id}` — altera os dados pessoais/profissionais;
- `PATCH /usuarios/{id}/categoria` — troca a categoria;
- `PATCH /usuarios/{id}/ativar` — ativa a conta;
- `PATCH /usuarios/{id}/desativar` — desativa a conta;
- `DELETE /usuarios/{id}` — exclui a conta.

Todos esses endpoints são exclusivos do perfil `ADMIN`.

Os endpoints GET de `/usuarios/**` são permitidos para `ADMIN` e
`ADVOGADO_SENIOR`.

## Segurança do cadastro

Não existe auto-cadastro público. Isso evita que um cliente escolha diretamente
uma categoria privilegiada no JSON de cadastro.

A primeira conta administrativa deve ser criada por seed/migração do banco
durante a instalação do sistema. Esse bootstrap deve ser tratado junto da
sincronização definitiva do SQL com as entidades JPA.

## Observação

Os nomes dos perfis são usados para autorização administrativa. As permissões
booleanas da Categoria continuam sendo usadas para as operações comuns do
sistema, mantendo a modelagem original de visualizar/editar/excluir.
