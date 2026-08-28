# auto-gestao-api

Backend do domínio de condomínio (Condomínio, Endereço, Pessoa, Taxa Extra, Meio de Contato)
do ecossistema **Gestão Condial**. Java 21 / Spring Boot 4.1.1 / Maven, contract-first
(OpenAPI) e Flyway. Este repositório é o **template arquitetural de referência**: veja
[`CLAUDE.md`](./CLAUDE.md) para a convenção completa de pacotes, nomes e fluxo de
desenvolvimento que todo novo microsserviço de domínio deve seguir.

## Stack

- Java 21, Spring Boot 4.1.1, Maven
- PostgreSQL + Flyway (migrations versionadas em `src/main/resources/db/migration`)
- springdoc-openapi (Swagger UI em `/docs`) + `openapi-generator-maven-plugin` (contract-first)
- Lombok + MapStruct
- Spring Security `oauth2-resource-server`, validando JWT do `gestao-condial-oauth-service`

## Como rodar local

1. Garanta que o Postgres compartilhado do ecossistema está no ar — base `gestao_condial`,
   schema `auto_gestao`. Duas opções (ver
   `/Users/igorlima/Documents/gestão-condial/infra/README.md` para detalhes):

   ```bash
   cp .env.example .env   # ajuste se necessário
   cd ../infra && docker compose up -d   # opção Docker
   ```

   Ou use um Postgres já instalado nativamente na máquina — crie a base `gestao_condial` e o
   schema `auto_gestao` uma vez (ver comandos SQL no `infra/README.md`). **Importante**: é a
   MESMA base física usada pelo `gestao-condial-oauth-service` — o que isola cada serviço é o
   schema, nunca um banco separado.

2. Rode a aplicação no profile `dev` (o Flyway cria as tabelas e insere o seed de
   desenvolvimento automaticamente na subida):

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   > Se `./mvnw` falhar por falta do Maven Wrapper (`.mvn/wrapper/`), use um Maven 3.9+ /
   > Java 21 instalados localmente (`mvn spring-boot:run -Dspring-boot.run.profiles=dev`).

3. Acesse a documentação interativa da API (Swagger UI, servida a partir do contrato YAML
   estático — não gerada por anotações):

   ```
   http://localhost:8080/docs
   ```

4. Health check público (usado por orquestradores/containers):

   ```
   http://localhost:8080/actuator/health
   ```

### Rodando os testes

```bash
./mvnw clean verify
```

Os testes usam H2 em memória (profile `test`, ver `application-test.properties` para a
justificativa dessa escolha em vez de Testcontainers) e não exigem Docker.

## Fluxo API-first (contract-first)

1. O contrato vive em [`openapi/auto-gestao-api.yaml`](./openapi/auto-gestao-api.yaml), na raiz
   do repo (fora de `src/`) — é a fonte da verdade de todo endpoint.
2. `./mvnw generate-sources` roda o `openapi-generator-maven-plugin`, que gera as interfaces de
   controller (`br.com.gestaocondial.autogestao.api`) e os modelos de request/response
   (`br.com.gestaocondial.autogestao.api.model`) sob `target/generated-sources/`.
3. As classes em `controller/` implementam essas interfaces geradas — a assinatura de cada
   endpoint vem sempre do contrato, nunca o contrário.
4. Para adicionar ou alterar um endpoint: editar o YAML primeiro, rodar
   `./mvnw generate-sources`, só então implementar/ajustar o controller correspondente.
5. `/docs` serve a Swagger UI apontando para o YAML estático publicado em
   `target/classes/static/openapi/` (`springdoc.api-docs.enabled=false` — não é geração via
   anotação em runtime).

## Estrutura e convenções

Ver [`CLAUDE.md`](./CLAUDE.md) — estrutura de pacotes (package-by-layer), convenção de nomes
(`XController/XService/XImpl/XRepository/XMapper/XDto`), prefixos de coluna (`id_/nr_/vl_/ds_/in_`),
regras de injeção por construtor e `@Transactional` no service, e como adicionar uma nova
entidade de domínio usando `Condominio` como referência.

## Authors

* **Igor Lima** - *Initial work* - [igorlimaaa](https://github.com/igorlimaaa)
