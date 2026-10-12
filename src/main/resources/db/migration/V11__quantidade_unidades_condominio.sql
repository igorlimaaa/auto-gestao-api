-- Quantas unidades o condomínio tem. É a base da fração ideal de cada unidade (1 / quantidade),
-- que deixa de ser digitada: tb_unidade.vl_fracao_ideal fica sem uso (mantida só por não perder
-- o que foi digitado antes) e a fração passa a ser calculada na leitura.
-- Nulo = ainda não informado (condomínios criados antes deste campo).
ALTER TABLE tb_condominio ADD COLUMN nr_unidades INTEGER;
ALTER TABLE tb_condominio ADD CONSTRAINT ck_condominio_nr_unidades CHECK (nr_unidades IS NULL OR nr_unidades > 0);
