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

- **Fração ideal é calculada, não digitada** (V11/V12): `Condominio.quantidadeUnidades` →
  `UnidadeDto.fracaoIdeal = 1 / quantidade` (6 casas, `UnidadeMapper`). Nasce na leitura, então
  acompanha qualquer mudança da quantidade e vale igual para todas as unidades. Sem a quantidade
  informada, a fração é nula. A coluna `tb_unidade.vl_fracao_ideal` foi removida na V12 (o seed de
  dev V5 ainda a preenche, mas roda antes da V12). A soma das frações pode diferir de 1 por
  arredondamento.

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
- **O banco é a única origem das cobranças.** Não existe mais geração local: o job de hora em hora
  (`SincronizadorDeCobrancasInterJob`) primeiro **importa** (uma listagem por condomínio, janela
  −30/+60 dias por vencimento: cria o registro e o vínculo com a unidade) e depois **reconsulta** uma
  a uma as em aberto (cobre as que já saíram da janela). Disparo manual:
  `POST /cobrancaOrdinaria/sincronizar`. Valor e vencimento acompanham o banco enquanto a cobrança
  está em aberto. Condomínio sem credenciais do Inter não gera cobrança.
- **Pendências** (`tb_cobranca_banco_pendente`, V13): cobrança do banco que não virou cobrança no
  sistema — `SEM_UNIDADE`, `UNIDADE_AMBIGUA`, `UNIDADE_ISENTA`, `COMPETENCIA_DUPLICADA`. É o retrato
  da última importação (recalculado a cada rodada; importação que falha mantém o anterior). Aparece
  como aviso na tela de Cobranças, só para administrador e síndico
  (`GET /cobrancaOrdinaria/pendencias-do-banco`).
- **Sem transação durante HTTP**: cada gravação é uma transação curta (`TransactionTemplate`); a
  falha de uma cobrança não derruba o lote, e 3 falhas seguidas num condomínio encerram a rodada
  dele.
- **Desconto por unidade** (`tb_unidade.nr_percentual_desconto_taxa`, V10): marca explícita na
  unidade, não derivada de `Pessoa.isSindico`. **100 = isenta** (ex.: unidade do síndico): cobrança
  que o banco emitir para ela é ignorada (vira pendência `UNIDADE_ISENTA`). Abaixo de 100 é só
  informativo — o valor cobrado é o que o banco emite.
- **Boleto em PDF** (`GET /cobrancaOrdinaria/{id}/pdf`, `BoletoImpl`): busca no Inter **a cada clique**
  e não guarda nada. O Inter devolve JSON `{"pdf": base64}`; aqui sai decodificado, `application/pdf`
  inline, `no-store`. Só para cobrança vinda do banco e em aberto (409 nas demais). Quem abre
  (`AlcanceDoPerfil`): administrador e síndico (tipo de perfil ou papel) qualquer unidade do alcance;
  os demais só a **própria unidade**, achada pelo e-mail do token (`sub`) no cadastro de moradores.
  A lista traz `podeVerBoleto` já calculado, e o frontend só mostra o ícone quando é `true`.
  Falha do Inter vira 502 com mensagem fixa (a causa fica só no log). O proxy `/api/backend` do
  frontend lê o corpo como texto, por isso o PDF passa por rota própria (`/api/cobrancas/{id}/pdf`).
- **Morador só vê o que é seu** (`AlcanceDoPerfil.Alcance.apenasProprio`): perfil de tipo `MORADOR`
  (sem papel de síndico/administrador) recebe, em listas e detalhes, só a **própria unidade**, o
  **próprio cadastro** (`Pessoa` achada pelo e-mail do token) e as **cobranças da própria unidade**;
  detalhe de registro alheio dá 403, lista dá vazio. Aplicado nos services (`UnidadeImpl`,
  `PessoaImpl`, `CobrancaOrdinariaImpl`), não nos controllers. Os demais tipos (síndico, subsíndico,
  financeiro, portaria...) seguem vendo o condomínio inteiro. Chamada sem JWT (job/teste) não é
  restringida — os endpoints nunca chegam aqui sem token. As permissões `MORADOR_LER` e
  `COBRANCA_LER` do papel MORADOR vêm da migration V6 do oauth-service. Taxas Extra (`TAXA_LER`) o
  morador continua vendo inteira (é do condomínio, não pessoal).
- **Agenda** (`Evento`, `AreaComum`; `EventoImpl`, `AreaComumImpl`): reunião, reserva de área, evento
  social, manutenção. **Morador gera `PENDENTE`; os demais, `APROVADO`.** Decide quem tem
  `EVENTO_APROVAR` (síndico e administrador). Todos veem os aprovados; pendentes/recusados/cancelados
  só o autor e quem aprova — e só eles veem o solicitante. Reserva (`RESERVA_AREA`) exige área
  `reservavel` e `ativa`, dentro de `horaAbertura`/`horaFechamento`, no futuro, no mesmo dia, e sem
  sobrepor `RESERVA_AREA`/`MANUTENCAO` aprovada da mesma área (409; reconferido ao aprovar). Intervalos
  são semiabertos (`fim` exclusivo). A resposta já traz `ehMeu`/`podeEditar`/`podeCancelar`/`podeDecidir`.
  Catálogo de áreas é do síndico (`AREA_ESCREVER`); o morador só escolhe. Permissões na V7 do
  oauth-service.
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
