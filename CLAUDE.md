# auto-gestao-api

Backend do domínio de condomínio (Condomínio, Endereço, Pessoa, Taxa Extra, Meio de Contato).
Este repositório é o **template arquitetural de referência**: todo novo microsserviço de
domínio do ecossistema Gestão Condial deve copiar a estrutura, convenções e fluxo descritos
aqui. Ver `/Users/igorlima/Documents/gestão-condial/CLAUDE.md` para a visão geral do sistema
multi-repositório.

## Stack

- Java 21, Spring Boot 4.1.1, Maven.
- PostgreSQL, Flyway (migrations versionadas — nunca `hibernate.ddl-auto=update`).
- Lombok + MapStruct (mapeamento entidade ↔ DTO).
- springdoc-openapi (serve o contrato em `/docs/`) + `openapi-generator-maven-plugin`
  (gera interfaces de controller + DTOs a partir do contrato).
- Spring Security (`oauth2-resource-server`), validando JWT emitido pelo
  `gestao-condial-oauth-service`.

## Estrutura de pacotes (package-by-layer)

```
br.com.gestaocondial.autogestao
├── config       # SecurityConfig, GlobalExceptionHandler, etc.
├── controller   # @RestController — implementam as interfaces geradas pelo openapi-generator
├── domain       # entidades JPA
├── dto          # DTOs de entrada/saída (nunca @Component/@Service — são POJOs)
├── enumeration
├── exception
├── impl         # implementação real dos services (XImpl implements XService)
├── mapper       # interfaces MapStruct (@Mapper(componentModel = "spring"))
├── repository   # interfaces JpaRepository — sem anotações extras
└── service      # interfaces (nunca @Service — o bean é o XImpl)
```

Decisão: manter package-by-layer (não por feature) enquanto o domínio for pequeno e
fortemente relacionado em um único agregado de condomínio. Revisitar para package-by-feature
se o número de agregados desacoplados crescer bastante.

### Convenção de nomes

- Por camada: `XController`, `XService` (interface), `XImpl` (implementação, em `impl/`),
  `XRepository`, `XMapper`, `XDto`, `X` (entidade em `domain/`).
- Colunas de banco prefixadas por tipo: `id_` (PK/FK), `nr_` (numérico), `vl_` (monetário),
  `ds_` (texto descritivo), `in_` (booleano/indicador). Tabelas: `tb_*`.

### Regras de implementação obrigatórias

- Injeção **sempre por construtor** (`@RequiredArgsConstructor` do Lombok + campos `final`).
  Nunca `@Autowired` em campo.
- `@Transactional` no `XImpl` (service), nunca no controller. Métodos de escrita sem
  `readOnly`; métodos de leitura com `@Transactional(readOnly = true)`.
- Erros tratados centralmente em `config/GlobalExceptionHandler.java`
  (`@RestControllerAdvice`), populando `ListaDeErrosOutputDto`/`ErroDeParametroOutputDto`.
  Não deixar exceptions sem handler.
- DTOs são POJOs simples — nunca anotar com `@Component`/`@Service`.
- Repositories são só `extends JpaRepository<X, Long>` — sem `@Configuration`,
  `@ComponentScan` ou `@EnableAutoConfiguration`.

## Modelo de domínio

```
Condominio 1──N Unidade 1──N Pessoa
     │                          │
     └──────────── 1──N ────────┘        (Pessoa.condominio, sempre obrigatório)
Condominio 1──N TaxaExtra
```

- **`Unidade` (V3)** é o apartamento/sala em si. Antes existia só `Pessoa.nr_unidade`, um
  número solto: não dava para cadastrar unidade vazia, nem havia o que impedisse um morador de
  apontar para uma unidade inexistente.
- **`Pessoa.unidade` é opcional nos dois sentidos**: unidade sem morador e morador sem unidade
  são cadastros válidos. A FK é NULL-ável de propósito — o que ela garante é que, *havendo*
  vínculo, a unidade exista.
- **`ds_bloco` é `NOT NULL DEFAULT ''`**, não nulável. No Postgres NULL não colide com NULL num
  UNIQUE, então bloco nulo exigiria índice sobre `COALESCE(ds_bloco,'')` — expressão que o H2
  dos testes não aceita. String vazia = "sem bloco", e o UNIQUE fica simples nos dois bancos.
- **`Pessoa.condominio` e `Pessoa.endereco` são `@ManyToOne`** (V4). Eram `@OneToOne`, e as
  UNIQUE que isso gerava significavam, na prática, *um morador por condomínio*.
- `Pessoa.endereco` é opcional: exigir endereço completo para cadastrar quem mora no condomínio
  contraria o cadastro independente.

### Migrations

`db/migration` é o schema; `db/migration-dev` é o seed de desenvolvimento. O seed mora **fora**
de `db/migration` porque o Flyway varre cada location **recursivamente** — uma subpasta
`db/migration/dev` entra em todo profile, inclusive `test` e `prod`, por mais que só o dev a
liste. Foi assim que o seed quebrou os testes em H2 com sintaxe Postgres-only.

## Integração com o Banco Inter (somente leitura)

As cobranças nascem no Inter (recorrência criada lá). Este serviço só **consulta** (`GET`; o único
`POST` é o do token OAuth) e guarda o que precisa. Código em `banking/` (cliente) +
`impl/SincronizacaoBancariaImpl` + `job/SincronizadorDeCobrancasInterJob`.

- **Uma conta Inter por condomínio.** Segredos em arquivos, nunca no banco nem na imagem:
  `${INTER_CREDENCIAIS_DIR}/<idCondominio>/{credenciais.properties, certificado.crt, chave.key}`
  (`client-id` / `client-secret`; chave PKCS#8 em PEM). Condomínio sem a pasta não é integrado e
  é ignorado. Em prod a pasta é o volume `infra/inter-credenciais` (read-only). Em dev:
  `./credenciais-inter/` (no `.gitignore`).
- **mTLS sem `.p12`**: `InterSslContexts` monta o keystore em memória a partir dos PEM. Endpoints,
  paginação (`paginacao.paginaAtual`, 0-based) e URL do token seguem o SDK `inter-co/pj-sdk-java`.
- **Casamento com a unidade**: `seuNumero` = `<unidade>-<ano>` (`1203-26`). Como a mesma unidade
  tem uma cobrança por mês, a **competência vem do mês do vencimento**. Se o mesmo número existir
  em mais de um bloco, o `seuNumero` é ambíguo e a cobrança é ignorada (com `WARN`).
- **Imutável vs. mutável**: PIX copia-e-cola, linha digitável, código de barras, nosso número,
  txid e `codigoSolicitacao` são gravados uma vez (V9) — a tela nunca consulta o banco para
  exibi-los. Situação, pagamento, valor e origem do recebimento são atualizados pelo job.
- **Jobs**: importação diária às 03:30 (janela −30/+60 dias por vencimento) e reconsulta horária
  das cobranças vindas do banco, sem `dt_pagamento` e fora de `RECEBIDO/CANCELADO/EXPIRADO`.
  Disparo manual: `POST /cobrancaOrdinaria/sincronizar`.
- **Sem transação durante HTTP**: cada gravação é uma transação curta (`TransactionTemplate`); a
  falha de uma cobrança não derruba o lote, e 3 falhas seguidas num condomínio encerram a rodada
  dele.
- **Desconto por unidade** (`tb_unidade.nr_percentual_desconto_taxa`, V10): 0 = taxa cheia, 50 =
  metade, **100 = isenta** (ex.: unidade do síndico). É marca explícita na unidade, não derivada de
  `Pessoa.isSindico`. O job de geração pula unidade isenta e aplica o desconto parcial ao valor; a
  importação do Inter ignora (com `WARN`) cobrança de unidade isenta; para desconto parcial o valor
  vem do banco, que prevalece. Cobrança já criada não é alterada ao marcar a isenção — para
  removê-la: `infra/apagar-cobranca-unidade-isenta.sh`.
- **Smoke test real** (opt-in): `INTER_SMOKE_DIR=<pasta> ./mvnw test -Dtest=InterCobrancaClientSmokeTest`.

## Segurança

Todo endpoint de domínio exige um JWT de acesso válido emitido pelo
`gestao-condial-oauth-service`. Só `/docs/**`, `/openapi/**`, `/actuator/health` e `/error`
ficam abertos — o último porque o job de deploy usa o health check para confirmar que o serviço
subiu.

- **A cadeia é a mesma em dev e prod** (`SecurityConfig`, `@Profile("!test")`). O profile dev já
  foi `permitAll()` em tudo; o efeito era o comportamento de autenticação só aparecer em
  produção. Para exercitar a API localmente, pegue um token em
  `POST http://localhost:8081/auth/login`.
- **`jwk-set-uri`, não só `issuer-uri`**: com `issuer-uri` sozinho o Spring faz OpenID Discovery
  na criação do bean, o que obrigaria o oauth-service a estar no ar quando este serviço sobe. O
  `issuer-uri` continua configurado porque é ele que faz validar o claim `iss`.
- **`EscopoDoCondominio`**: ter `MORADOR_LER` não é ver os moradores de todos os condomínios.
  O recorte vem do claim `condominio` do token — o condomínio do perfil ativo. Perfis globais
  (`ADMINISTRADORA`) não têm esse claim e enxergam tudo. Listagens são filtradas mesmo sem o
  cliente pedir; pedir outro condomínio responde 403, em vez de devolver o próprio em silêncio.
- **`ValidadorDeEscopoDeAcesso`**: o oauth-service emite dois tipos de token com a mesma chave e
  o mesmo issuer — o de acesso e o de seleção de perfil. Só o claim `escopo` os separa; sem esse
  validador, um token de seleção passaria por `authenticated()`.
- **`ConversorDeAutoridadesDoJwt`**: `papeis` viram `ROLE_<CODIGO>` (para `hasRole`) e
  `permissoes` viram authorities de mesmo nome (para `hasAuthority`). Nenhum endpoint usa isso
  ainda — hoje todos exigem apenas autenticação.
- **`RespostaDeErroDeSeguranca`**: 401/403 saem no mesmo envelope `ListaDeErrosOutputDto` do
  `GlobalExceptionHandler`, e não com o corpo vazio padrão do Spring Security. É o 401 em JSON
  que o frontend usa como gatilho para levar o usuário de volta ao login.
- `ClaimsDoToken` é uma cópia deliberada da classe homônima do oauth-service: os serviços não
  compartilham biblioteca — o contrato entre eles é o próprio token. Mudar um nome de claim
  exige mudar nos dois.

## Fluxo API-first (contract-first)

1. O contrato vive em `openapi/auto-gestao-api.yaml`, na raiz do repo (fora de `src/`).
2. `./mvnw generate-sources` roda o `openapi-generator-maven-plugin`, que gera as interfaces
   de controller e os DTOs de request/response sob `target/generated-sources/`.
3. As classes em `controller/` implementam essas interfaces geradas — a assinatura do endpoint
   vem sempre do contrato, nunca o contrário.
4. Para adicionar/alterar um endpoint: editar o YAML primeiro, rodar
   `./mvnw generate-sources`, só então implementar/ajustar o controller.
5. `/docs/` serve a Swagger UI apontando para o YAML (não para anotações no código —
   `springdoc.api-docs.enabled=false`).

## Banco de dados

- **Base física compartilhada** com todos os microsserviços (`gestao_condial`) — o que isola
  este serviço é o schema, nunca um banco separado. Ver
  `/Users/igorlima/Documents/gestão-condial/infra/README.md`.
- Schema próprio: `auto_gestao` (`spring.jpa.properties.hibernate.default_schema`,
  `spring.flyway.schemas`).
- Migrations em `src/main/resources/db/migration/V<n>__descricao.sql`.
- `hibernate.ddl-auto=validate` em todos os profiles — o Flyway é o dono do schema.

## Como rodar local

```
# Postgres compartilhado (Docker ou nativo local — ver infra/README.md na pasta-mãe):
cd ../infra && docker compose up -d && cd -
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Swagger UI: `http://localhost:8080/docs`.

## Como adicionar uma nova entidade de domínio

Use `Condominio` como referência: `domain/Condominio.java` → `dto/CondominioDto.java` →
`mapper/CondominioMapper.java` → `repository/CondominioRepository.java` →
`service/CondominioService.java` (interface) → `impl/CondominioImpl.java` (implementação,
injeção por construtor) → endpoint no `openapi/auto-gestao-api.yaml` → gerar interface →
`controller/CondominioController.java` implementando a interface gerada → migration Flyway
para a(s) nova(s) tabela(s).
