-- A fração ideal passou a ser calculada na leitura (1 / Condominio.quantidadeUnidades, ver V11).
-- Os valores que haviam sido digitados deixam de valer e são apagados, para que nenhuma unidade
-- tenha fração própria: todas seguem a mesma regra.
ALTER TABLE tb_unidade DROP COLUMN vl_fracao_ideal;
