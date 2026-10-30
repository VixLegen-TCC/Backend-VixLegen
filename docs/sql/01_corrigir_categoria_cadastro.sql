-- VixLegen | Correcao conservadora da categoria para cadastro publico
-- Requisitos: MySQL 8.0+, backup do banco, API PARADA durante a execucao.
-- NAO apaga tabelas, colunas ou registros.
-- NAO unifica automaticamente tabelas legadas (Usuario) e novas (usuarios).
-- NUNCA execute novamente o SQL legado, pois inclui DROP DATABASE.
USE VixLegenDB;
SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS vl_verificar_coluna_categoria_20261030;
DROP PROCEDURE IF EXISTS vl_renomear_coluna_categoria_20261030;
DROP PROCEDURE IF EXISTS vl_corrigir_categoria_20261030;
DELIMITER $$

CREATE PROCEDURE vl_verificar_coluna_categoria_20261030(
    IN nome_antigo VARCHAR(64), IN nome_novo VARCHAR(64)
)
BEGIN
    DECLARE existe_antigo INT DEFAULT 0;
    DECLARE existe_novo INT DEFAULT 0;
    SELECT COUNT(*) INTO existe_antigo FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'categoria' AND BINARY COLUMN_NAME = BINARY nome_antigo;
    SELECT COUNT(*) INTO existe_novo FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'categoria' AND BINARY COLUMN_NAME = BINARY nome_novo;
    IF existe_antigo = 1 AND existe_novo = 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Categoria: colunas antigas e novas coexistem; revisar dados antes de migrar';
    END IF;
    IF existe_antigo = 0 AND existe_novo = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Categoria: coluna obrigatoria ausente; revisar schema antes de migrar';
    END IF;
END$$

CREATE PROCEDURE vl_renomear_coluna_categoria_20261030(
    IN nome_antigo VARCHAR(64), IN nome_novo VARCHAR(64)
)
BEGIN
    DECLARE existe_antigo INT DEFAULT 0;
    SELECT COUNT(*) INTO existe_antigo FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'categoria' AND BINARY COLUMN_NAME = BINARY nome_antigo;
    IF existe_antigo = 1 THEN
        SET @sql_migracao_vl = CONCAT('ALTER TABLE `categoria` RENAME COLUMN `', nome_antigo, '` TO `', nome_novo, '`');
        PREPARE comando_vl FROM @sql_migracao_vl;
        EXECUTE comando_vl;
        DEALLOCATE PREPARE comando_vl;
    END IF;
END$$

CREATE PROCEDURE vl_corrigir_categoria_20261030()
BEGIN
    DECLARE tabela_categoria INT DEFAULT 0;
    DECLARE existe_pk INT DEFAULT 0;
    DECLARE auto_pk INT DEFAULT 0;
    SELECT COUNT(*) INTO tabela_categoria FROM information_schema.TABLES
     WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'categoria';
    IF tabela_categoria <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tabela categoria nao encontrada ou ambigua; nao e seguro migrar';
    END IF;
    -- Verifica tudo ANTES das alteracoes para evitar migracao parcial.
    CALL vl_verificar_coluna_categoria_20261030('codigoCategoria', 'codigo_categoria');
    CALL vl_verificar_coluna_categoria_20261030('nivelAcesso', 'nivel_acesso');
    CALL vl_verificar_coluna_categoria_20261030('limiteProcessosSimultaneos', 'limite_processos_simultaneos');
    CALL vl_verificar_coluna_categoria_20261030('permissaoVisualizar', 'permissao_visualizar');
    CALL vl_verificar_coluna_categoria_20261030('permissaoEditar', 'permissao_editar');
    CALL vl_verificar_coluna_categoria_20261030('permissaoExcluir', 'permissao_excluir');

    SELECT COUNT(*) INTO existe_pk FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'categoria'
       AND BINARY COLUMN_NAME IN (BINARY 'codigoCategoria', BINARY 'codigo_categoria') AND COLUMN_KEY = 'PRI';
    SELECT COUNT(*) INTO auto_pk FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'categoria'
       AND BINARY COLUMN_NAME IN (BINARY 'codigoCategoria', BINARY 'codigo_categoria') AND EXTRA LIKE '%auto_increment%';
    IF existe_pk <> 1 OR auto_pk <> 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'PK de categoria inesperada; corrigir manualmente antes da migracao';
    END IF;

    -- RENAME COLUMN preserva IDs e atualiza referencias de chave estrangeira no MySQL 8.
    CALL vl_renomear_coluna_categoria_20261030('codigoCategoria', 'codigo_categoria');
    CALL vl_renomear_coluna_categoria_20261030('nivelAcesso', 'nivel_acesso');
    CALL vl_renomear_coluna_categoria_20261030('limiteProcessosSimultaneos', 'limite_processos_simultaneos');
    CALL vl_renomear_coluna_categoria_20261030('permissaoVisualizar', 'permissao_visualizar');
    CALL vl_renomear_coluna_categoria_20261030('permissaoEditar', 'permissao_editar');
    CALL vl_renomear_coluna_categoria_20261030('permissaoExcluir', 'permissao_excluir');
END$$
DELIMITER ;

CALL vl_corrigir_categoria_20261030();
DROP PROCEDURE vl_corrigir_categoria_20261030;
DROP PROCEDURE vl_renomear_coluna_categoria_20261030;
DROP PROCEDURE vl_verificar_coluna_categoria_20261030;

-- Garante categoria padrao sem duplicar registros.
INSERT INTO categoria
    (descricao, nivel_acesso, limite_processos_simultaneos,
     permissao_visualizar, permissao_editar, permissao_excluir)
SELECT 'Advogado Júnior', 3, 20, TRUE, TRUE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM categoria WHERE LOWER(descricao) = LOWER('Advogado Júnior')
);

SELECT codigo_categoria, descricao, nivel_acesso,
       limite_processos_simultaneos, permissao_visualizar,
       permissao_editar, permissao_excluir
  FROM categoria ORDER BY codigo_categoria;

SELECT TABLE_NAME, COLUMN_NAME, COLUMN_KEY, EXTRA
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND LOWER(TABLE_NAME) IN ('categoria', 'usuarios', 'usuario', 'notificacao', 'empresas')
 ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- ALERTA: Usuario/usuarios ou Cliente/clientes com dados precisam de
-- reconciliacao manual (IDs, senhas e chaves estrangeiras), nao DROP/RENAME.
SELECT TABLE_NAME, TABLE_ROWS
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'
 ORDER BY TABLE_NAME;
