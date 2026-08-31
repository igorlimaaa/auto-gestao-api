-- ---------------------------------------------------------------------------------------------
-- Unidade: o apartamento/sala em si, com dados proprios.
--
-- Antes disso a "unidade" era apenas tb_pessoa.nr_unidade, um numero solto: nao dava para
-- cadastrar uma unidade vazia, nem havia nada que impedisse um morador de apontar para uma
-- unidade que nao existe.
--
--   tb_condominio 1--N tb_unidade 1--N tb_pessoa
--
-- O vinculo morador->unidade e OPCIONAL nos dois sentidos: da para cadastrar uma unidade sem
-- morador nenhum e uma pessoa sem unidade, e ligar as duas depois. Por isso id_unidade e
-- NULL-avel; o que a FK garante e que, QUANDO houver vinculo, ele aponte para algo que existe.
-- ---------------------------------------------------------------------------------------------

CREATE TABLE tb_unidade (
    id_unidade BIGSERIAL PRIMARY KEY,
    id_condominio BIGINT NOT NULL,
    ds_bloco VARCHAR(30) NOT NULL DEFAULT '',
    nr_unidade VARCHAR(20) NOT NULL,
    nr_andar INTEGER,
    vl_fracao_ideal NUMERIC(10, 6),
    ds_observacao VARCHAR(300),
    in_ativa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_unidade_condominio FOREIGN KEY (id_condominio)
        REFERENCES tb_condominio (id_condominio)
);

-- Nao existem duas unidades com o mesmo bloco+numero no mesmo condominio.
--
-- ds_bloco e NOT NULL DEFAULT '' em vez de nulavel: no Postgres, NULL nao colide com NULL num
-- UNIQUE, entao bloco nulo exigiria um indice sobre COALESCE(ds_bloco, '') — expressao que o
-- H2 dos testes nao aceita. Com string vazia representando "sem bloco", o UNIQUE e comum e
-- vale nos dois bancos. Do lado da aplicacao a diferenca nao aparece: '' e falsy, entao a
-- unidade continua sendo exibida so pelo numero.
CREATE UNIQUE INDEX uk_unidade_condominio_bloco_numero
    ON tb_unidade (id_condominio, ds_bloco, nr_unidade);

CREATE INDEX ix_unidade_condominio ON tb_unidade (id_condominio);

-- ---------------------------------------------------------------------------------------------
-- Pessoa passa a apontar para a unidade.
-- ---------------------------------------------------------------------------------------------

ALTER TABLE tb_pessoa ADD COLUMN id_unidade BIGINT;

ALTER TABLE tb_pessoa ADD CONSTRAINT fk_pessoa_unidade
    FOREIGN KEY (id_unidade) REFERENCES tb_unidade (id_unidade);

CREATE INDEX ix_pessoa_unidade ON tb_pessoa (id_unidade);

-- Converte os numeros de unidade ja gravados em Unidades reais e religa os moradores. Roda a
-- vazio quando nao ha moradores cadastrados, mas precisa existir para bases que ja tenham dados.
INSERT INTO tb_unidade (id_condominio, ds_bloco, nr_unidade)
SELECT DISTINCT p.id_condominio, '', CAST(p.nr_unidade AS VARCHAR(20))
FROM tb_pessoa p
WHERE p.nr_unidade IS NOT NULL;

UPDATE tb_pessoa p
SET id_unidade = u.id_unidade
FROM tb_unidade u
WHERE u.id_condominio = p.id_condominio
  AND u.nr_unidade = CAST(p.nr_unidade AS VARCHAR(20))
  AND p.nr_unidade IS NOT NULL;

ALTER TABLE tb_pessoa DROP COLUMN nr_unidade;

-- O endereco proprio do morador deixa de ser obrigatorio: exigir um endereco completo para
-- cadastrar quem mora no condominio contraria justamente o cadastro independente.
ALTER TABLE tb_pessoa ALTER COLUMN id_endereco DROP NOT NULL;
