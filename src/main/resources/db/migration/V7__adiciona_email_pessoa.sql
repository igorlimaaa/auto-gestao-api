-- A flag in_envio_email indica que o boleto deve ser enviado por e-mail, mas nao havia campo
-- para o endereco de e-mail em si. Nullable: e-mail e opcional independente da flag.
ALTER TABLE tb_pessoa ADD COLUMN ds_email VARCHAR(255);
