-- Carga de DESENVOLVIMENTO das telas de dominio. Roda so nos profiles dev e test
-- (db/migration/dev), nunca em prod.
--
-- Cobre de proposito os tres estados que o cadastro permite, para que as telas possam ser
-- exercitadas em todos eles:
--   - unidade com morador;
--   - unidade vazia (sem ninguem vinculado);
--   - morador sem unidade (ainda nao alocado).

INSERT INTO tb_unidade (id_condominio, ds_bloco, nr_unidade, nr_andar, vl_fracao_ideal, in_ativa) VALUES
    (1, 'A', '101', 1, 0.041667, TRUE),
    (1, 'A', '102', 1, 0.041667, TRUE),
    (1, 'A', '201', 2, 0.041667, TRUE),
    (1, 'B', '101', 1, 0.041667, TRUE),
    (1, 'B', '102', 1, 0.041667, FALSE),
    (2, '', '11',  1, 0.083333, TRUE),
    (2, '', '12',  1, 0.083333, TRUE),
    (2, '', 'Loja 1', 0, 0.125000, TRUE);

-- Moradores. Os dois ultimos ficam sem unidade de proposito.
INSERT INTO tb_pessoa (ds_pessoa, nr_cpf, in_envio_email, in_envio_impresso, in_sindico, id_condominio, id_unidade)
SELECT v.nome, v.cpf, v.email, v.impresso, v.sindico, v.id_condominio,
       (SELECT u.id_unidade FROM tb_unidade u
         WHERE u.id_condominio = v.id_condominio
           AND u.ds_bloco = v.bloco
           AND u.nr_unidade = v.numero)
FROM (VALUES
    ('Maria Souza',      '11144477735', TRUE,  FALSE, TRUE,  1::BIGINT, 'A', '101'),
    ('Joao Pereira',     '52998224725', TRUE,  FALSE, FALSE, 1::BIGINT, 'A', '102'),
    ('Ana Beatriz Lima', '11122233396', FALSE, TRUE,  FALSE, 1::BIGINT, 'A', '201'),
    ('Carlos Eduardo',   '12345678909', TRUE,  FALSE, FALSE, 1::BIGINT, 'B', '101'),
    ('Fernanda Rocha',   '39053344705', TRUE,  FALSE, TRUE,  2::BIGINT, '', '11'),
    ('Rafael Nunes',     '15350946056', FALSE, TRUE,  FALSE, 2::BIGINT, '', '12'),
    ('Patricia Alves',   '71428793860', TRUE,  FALSE, FALSE, 1::BIGINT, '', NULL),
    ('Bruno Carvalho',   '48195207040', TRUE,  FALSE, FALSE, 2::BIGINT, '', NULL)
) AS v (nome, cpf, email, impresso, sindico, id_condominio, bloco, numero);

INSERT INTO tb_taxa_extra (id_condominio, vl_taxa_extra, nr_parcelas, ds_taxa_extra, dt_cadastro) VALUES
    (1, 150.00, 10, 'Reforma da fachada',        CURRENT_TIMESTAMP),
    (1,  80.00,  5, 'Troca do portao eletronico', CURRENT_TIMESTAMP);
