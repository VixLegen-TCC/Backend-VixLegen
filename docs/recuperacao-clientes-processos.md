# Recuperação dos vínculos de empresas, clientes e processos

O erro observado em 09/10/2026 é `Cannot add or update a child row` durante a criação da FK `clientes.empresa_organizacao_id -> empresas.id_empresa`.

**Procedimento em ambiente local:**
1. Pare o Spring Boot e exporte o banco VixLegenDB para backup.
2. Execute o diagnóstico do script SQL, verifique os vínculos existentes.
3. Só depois execute o bloco de reparo, que associa cada usuário legado sem empresa a uma organização própria e herda para os clientes a organização do responsável.
4. Confirme que não há usuários/clientes sem empresa válida nem clientes vinculados a uma organização diferente da organização do advogado responsável.
5. Reinicie a aplicação e confira `GET /clientes` e `GET /processos` com login JWT, além dos registros no MySQL.

**Atenção:** duas contas com o mesmo nome de empresa NÃO são automaticamente membros da mesma organização. Um administrador deve validar explicitamente a necessidade de compartilhamento. Não altere o escopo de acesso em massa.

A correção permanente no cadastro já cria a entidade Empresa e a associa ao Usuario na mesma transação. Ela não recupera, por si só, vínculos antigos.

Arquivo do script para execução manual: enviado na conversa como `VixLegen_Corrigir_Clientes_Processos.sql`.
