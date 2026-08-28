-- Seed de dados iniciais para ambiente de desenvolvimento/teste. Fica em uma
-- location Flyway separada (db/migration/dev), incluída apenas nos profiles
-- dev e test (spring.flyway.locations em application-dev.properties e
-- application-test.properties). O profile prod usa só classpath:db/migration.

INSERT INTO tb_endereco (id_endereco, nr_cep, ds_complemento, ds_endereco) VALUES (1, 50710265, 'Edifício Antália', 'Rua Pio IX, 496');
INSERT INTO tb_endereco (id_endereco, nr_cep, ds_complemento, ds_endereco) VALUES (2, 50030030, 'Edifício Anália', 'Rua Dr. Manoel de Almeida Belo');

INSERT INTO tb_condominio (id_condominio, nr_ddd, nr_telefone, vl_taxa, id_endereco, in_taxa_extra, vl_juros, vl_multa) VALUES (1, 81, 32045371, 500.0, 1, true, 1.0, 2.0);
INSERT INTO tb_condominio (id_condominio, nr_ddd, nr_telefone, vl_taxa, id_endereco, in_taxa_extra, vl_juros, vl_multa) VALUES (2, 81, 32557896, 700.0, 2, false, 0.0, 0.0);
