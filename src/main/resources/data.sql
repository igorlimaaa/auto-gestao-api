INSERT INTO TB_ENDERECO (id_endereco, nr_cep, ds_complemento, ds_endereco) VALUES(1, 50710265, 'Edifício Antália', 'Rua Pio IX, 496');
INSERT INTO TB_ENDERECO (id_endereco, nr_cep, ds_complemento, ds_endereco) VALUES(2, 50030030, 'Edifício Anália', 'Rua Dr. Manoel de Almeida Belo');

INSERT INTO tb_condominio (id_condominio, nr_ddd, nr_telefone, vl_taxa, id_endereco, in_taxa_extra, vl_juros, vl_multa) VALUES(1, 81, 32045371, 500.0, 1, true, 1.0, 2.0);
INSERT INTO tb_condominio (id_condominio, nr_ddd, nr_telefone, vl_taxa, id_endereco, in_taxa_extra, vl_juros, vl_multa) VALUES(2, 81, 32557896, 700.0, 2, false, 0.0, 0.0);

--INSERT INTO tb_pessoa (id_pessoa, nr_cpf, ds_email, in_envio_email, ds_pessoa, nr_celular, nr_telefone, nr_unidade, id_condominio, id_endereco, in_envio_impresso) VALUES(1, '09605653451', 'priscillabasto@gmail.com', true, 'Priscilla Elisa de Azevedo Basto', 992918424, NULL, 302, 1, 1, false);
