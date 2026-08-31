-- ---------------------------------------------------------------------------------------------
-- tb_pessoa nascia com UNIQUE em id_condominio e em id_endereco, reflexo de a entidade Pessoa
-- mapear os dois como @OneToOne.
--
-- Na pratica isso significava UM MORADOR POR CONDOMINIO: cadastrar o segundo violava a
-- constraint. E o unique em id_endereco impedia que duas pessoas dividissem o mesmo endereco,
-- o que e o caso normal de quem mora na mesma unidade.
--
-- Um condominio tem muitos moradores, e um endereco pode ser de varios. As duas viram
-- ManyToOne do lado da entidade.
-- ---------------------------------------------------------------------------------------------

ALTER TABLE tb_pessoa DROP CONSTRAINT IF EXISTS uk_pessoa_condominio;
ALTER TABLE tb_pessoa DROP CONSTRAINT IF EXISTS uk_pessoa_endereco;

CREATE INDEX IF NOT EXISTS ix_pessoa_condominio ON tb_pessoa (id_condominio);
