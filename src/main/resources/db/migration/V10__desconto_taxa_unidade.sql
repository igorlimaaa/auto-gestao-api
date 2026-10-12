-- Desconto percentual sobre a taxa ordinária, por unidade. 0 = paga a taxa cheia; 100 = isenta
-- (a unidade do síndico, por exemplo) e o job de geração não cria cobrança para ela; entre os
-- dois, a cobrança nasce com o valor da taxa menos o desconto (50 = metade).
ALTER TABLE tb_unidade ADD COLUMN nr_percentual_desconto_taxa NUMERIC(5, 2) NOT NULL DEFAULT 0;
ALTER TABLE tb_unidade ADD COLUMN ds_motivo_desconto_taxa VARCHAR(100);
ALTER TABLE tb_unidade ADD CONSTRAINT ck_unidade_percentual_desconto
    CHECK (nr_percentual_desconto_taxa >= 0 AND nr_percentual_desconto_taxa <= 100);
