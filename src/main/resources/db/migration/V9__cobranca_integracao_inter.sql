-- Integração com o Banco Inter (somente leitura). As cobranças nascem no banco (recorrência
-- criada lá); aqui guardamos o que NÃO muda depois da emissão — para a tela nunca precisar
-- consultar o banco só para exibir PIX ou boleto — e o que muda quando alguém paga.
--
-- Imutáveis: codigoSolicitacao, seuNumero, nossoNumero, codigoBarras, txid, dataEmissao
-- (e os ja existentes ds_pix_copia_cola / ds_linha_digitavel, agora preenchidos pelo banco).
-- Mutáveis, atualizados pelo job horário: situacaoBanco, dt_pagamento, vl_total_recebido,
-- ds_origem_recebimento, dt_ultima_sincronizacao.

ALTER TABLE tb_cobranca_ordinaria ADD COLUMN ds_codigo_solicitacao   VARCHAR(40);
ALTER TABLE tb_cobranca_ordinaria ADD COLUMN ds_seu_numero           VARCHAR(30);
ALTER TABLE tb_cobranca_ordinaria ADD COLUMN ds_nosso_numero         VARCHAR(30);
ALTER TABLE tb_cobranca_ordinaria ADD COLUMN ds_codigo_barras        VARCHAR(60);
ALTER TABLE tb_cobranca_ordinaria ADD COLUMN ds_txid                 VARCHAR(40);
ALTER TABLE tb_cobranca_ordinaria ADD COLUMN dt_emissao              DATE;
ALTER TABLE tb_cobranca_ordinaria ADD COLUMN ds_situacao_banco       VARCHAR(30);
ALTER TABLE tb_cobranca_ordinaria ADD COLUMN vl_total_recebido       DOUBLE PRECISION;
ALTER TABLE tb_cobranca_ordinaria ADD COLUMN ds_origem_recebimento   VARCHAR(10);
ALTER TABLE tb_cobranca_ordinaria ADD COLUMN dt_ultima_sincronizacao TIMESTAMP;

-- NULL não colide com NULL em UNIQUE (Postgres e H2): cobranças sem vínculo com o banco seguem válidas.
ALTER TABLE tb_cobranca_ordinaria
    ADD CONSTRAINT uk_cobranca_codigo_solicitacao UNIQUE (ds_codigo_solicitacao);
