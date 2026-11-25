# Hotel4You (Grupo 2)

Aplicação de gestão do Hotel4You, feita no âmbito de Laboratório de Projeto III (ISEP, 2026/2027).
Tecnologias: Java 17, JavaFX, SQL Server (JDBC), Maven e JUnit 5.

## Requisitos
- JDK 17
- IntelliJ IDEA
- Acesso à base de dados SQL Server atribuída ao grupo

## Como correr o projeto
1. Abre a pasta do projeto no IntelliJ.
2. No painel **Maven**, clica em **Reload All Maven Projects**, para o IntelliJ conhecer as dependências.
3. Cria o teu ficheiro `.env` (ver a secção seguinte).
4. Corre a classe `Launcher`.
5. Abre a janela **Hotel4You**. O botão **Testar ligação** diz se a ligação à base de dados funciona.

## Executável (sem IntelliJ)
**Gerar** (na raiz do projeto):

```
.\mvnw.cmd clean package -Pexecutavel -DskipTests
```

Fica tudo na pasta `target/executavel`:

| Ficheiro | Para quê |
|---|---|
| `Hotel4You.jar` | A aplicação com todas as bibliotecas, para Windows, Linux e Mac com processador Intel |
| `Hotel4You-mac-arm.jar` | A mesma aplicação, para Mac com processador Apple (M1, M2, M3...) |
| `Hotel4You.bat` | Abrir no Windows, com duplo clique |
| `Hotel4You.command` | Abrir no Mac, com duplo clique (escolhe sozinho o `.jar` certo) |
| `Hotel4You.sh` | Abrir no Linux |
| `.env.example` | Modelo do ficheiro de configuração da base de dados |

**Correr noutro computador:**
1. É preciso ter o Java 17 ou superior instalado (não é preciso o IntelliJ).
2. Copia a pasta `executavel` para o computador.
3. Na mesma pasta, copia o `.env.example` para um ficheiro `.env` e preenche-o (ver a secção seguinte).
4. Abre a aplicação:
   - **Windows:** duplo clique no `Hotel4You.bat`.
   - **Mac:** na primeira vez, no Terminal, dentro da pasta, corre `chmod +x Hotel4You.command`; depois é duplo clique no `Hotel4You.command`. Se o Mac disser que não pode abrir o ficheiro, clica com o botão direito em cima dele e escolhe **Abrir**.
   - **Linux:** no terminal, dentro da pasta, corre `sh Hotel4You.sh`.
   - Em qualquer sistema, também se pode abrir com `java -jar Hotel4You.jar` (no Mac com processador Apple, `java -jar Hotel4You-mac-arm.jar`), dentro da pasta.

A aplicação lê o `.env` da pasta onde está o `.jar`.

## Javadoc
**Gerar** (na raiz do projeto):

```
.\mvnw.cmd exec:exec@javadoc
```

A documentação fica em `target/javadoc` (abre o `index.html`). Inclui todos os pacotes. O comando falha se houver algum aviso de documentação; só não avisa dos getters e setters simples sem Javadoc.

## Configurar a base de dados (`.env`)
As credenciais **nunca** vão para o Git: o `.env` está no `.gitignore`.

1. Na raiz do projeto, copia o ficheiro `.env.example` para um novo ficheiro chamado `.env`.
2. Preenche os valores, sem espaços nem aspas:

```
DB_SERVER=
DB_DATABASE=
DB_USER=
DB_PASSWORD=
```

Os valores pedem-se a um elemento da equipa. Se o servidor não usar a porta normal, o `DB_SERVER` escreve-se `servidor:porta` (ex.: `localhost:14333`).

## Testes
Corre `mvn test`. O teste `ConexaoBDTest` é ignorado (não falha) quando não há acesso à base de dados.
Os testes da DAL gravam e apagam dados na base de dados do `.env`. Com o `.env` normal, isso acontece na base de dados da equipa.

Não corras os testes ao mesmo tempo que outra pessoa da equipa: no início, os testes da DAL apagam os restos de testes da base de dados, e podem apagar os dados que os testes da outra pessoa estão a usar.

## Problemas conhecidos
- **Erro de SSL ao testar a ligação** ("The driver could not establish a secure connection to SQL Server by using SSL encryption"): acontece com versões recentes do JDK 17, que recusam o TLS antigo do servidor. Se acontecer, avisa a equipa e indica a tua versão de Java (`java -version`, ou **File → Project Structure → SDK** no IntelliJ).
- **Erro "Module ... not found" ao correr no IntelliJ**: faz **Reload All Maven Projects** e, se continuar, **File → Invalidate Caches → Invalidate and Restart**.

## Estrutura de pacotes
Todos dentro de `pt.isep.lp3.g2.hotel4youg2`:

| Pacote | Função |
|---|---|
| (raiz) | Arranque da aplicação: `Launcher` e `Hotel4YouApplication` |
| `model` | Entidades do negócio (`Reserva`, `Cliente`, `Quarto`...) |
| `model.enums` | Enumerações (`EstadoReserva`, `Perfil`, `TipoQuarto`, `Vista`...) |
| `dal` | Acesso a dados: a ligação (`ConexaoBD`), os mappers e as classes `...DAL` que falam com a base de dados |
| `dal.interfaces` | Interfaces da DAL (`I...DAL`) e o `RowMapper` |
| `bll` | Regras de negócio e validações (classes `...BLL` e `Permissoes`) |
| `exception` | Exceções da aplicação (`ValidacaoException` e `DataAccessException`) |
| `controller` | Controladores JavaFX, que ligam os ecrãs à BLL, e a navegação (`Navegador`, `Mensagens`) |
| `util` | Classes de apoio (`Sessao`, `PasswordUtils`) |

Os recursos estão em `src/main/resources/pt/isep/lp3/g2/hotel4youg2`:

| Pasta | O que tem |
|---|---|
| `view` | Ecrãs FXML |
| `css` | Folha de estilos (`hotel4you.css`) |
| `images` | Ícones e logótipo |
| `xml` | XSD das entregas (`Hotel4You.xsd`) |

Os scripts da base de dados estão em `Scrum/Base de Dados`, numerados pela ordem em que se correm (`NN_descricao.sql`).

## Scripts da base de dados

**Base de dados nova** (por exemplo, para a demonstração). Corre no SSMS, por esta ordem:

1. `00_criar_base_de_dados.sql`: cria todas as tabelas e os dados base (tarifas, 20 quartos, serviços, horário do check-in e do check-out e a conta do gestor). É o mesmo que correr do `01` ao `30`.
2. `99_dados_demonstracao.sql` (opcional): enche a base de dados com dados para mostrar todos os ecrãs.

Os dois podem correr várias vezes sem erros: o `00` só cria o que falta e o `99` não volta a pôr os dados se já lá estiverem. O `99` recusa correr numa base de dados que já tenha outros dados, para não se misturar com eles.

**Base de dados que já existe** (a da equipa). Não se corre o `00` nem o `99`: corre-se só o script novo de cada sprint, do `01` ao `30` e os seguintes.

### Dados de demonstração

As datas do `99` contam a partir do dia em que se corre, por isso corre-se no próprio dia da demonstração.

| Para mostrar | O que o `99` põe |
|---|---|
| Cartão e planta do hotel | A Ana Martins tem uma reserva em curso no quarto 201 (cartão `C201`), com piscina e refeições, e já picou o quarto, a piscina e o restaurante. No quarto 205 o acesso é recusado |
| Área do cliente | A Ana tem uma reserva por fazer, uma em curso, uma concluída já avaliada e outra concluída por avaliar |
| Check-in com baixa de stock | O Bruno Costa tem uma reserva confirmada para hoje no quarto 208, com as toalhas ajustadas para 6. O check-in só se faz a partir das 15:00 (ou da hora escolhida em **Horas e taxas**) |
| Cancelar reservas sem check-in | O Diogo Ferreira tem uma reserva confirmada que começava ontem: é cancelada no primeiro login do dia |
| Pedidos extra | A Carla Sousa (quarto 302, em curso) tem dois pedidos por decidir; a Ana tem um aceite e um recusado |
| Entregas | Do fornecedor ProductHero (`11PT920`): uma aceite, uma recusada com motivo e uma por decidir. O fornecedor L'industrie Alimentaire (`964070328`) não tem entregas, para carregar o JSON na demonstração |
| Stock | Toalhas e secadores (voltam ao stock no check-out), champô (consumível), uma saída por estrago e um secador emprestado por devolver |
| Estatísticas e avaliações | Cerca de 20 reservas nos últimos 2 meses, com serviços, e 8 avaliações de 2 a 5 estrelas |
| Acessos dos funcionários | Picagens da rececionista (`F2`), da restauração (`R3`) e do cartão mestre (`C000`) |

Ficheiros para carregar nas entregas: `src/test/resources/pt/isep/lp3/g2/hotel4youg2/xml/entrega-valida.xml` (ProductHero), `json/entrega-valida.json` (L'industrie Alimentaire) e `xml/entrega-varios-erros.xml` (entrega com erros).

### Contas de demonstração

A palavra-passe de todas é `Hotel4You`. A do gestor é pedida de novo no primeiro login.

| Conta | Entra com |
|---|---|
| Gestor | `gestor` |
| Rececionista (Rita Almeida) | `rececao` |
| Restauração (Hugo Pinto) | `restauracao` |
| Cliente Ana Martins | NIF `246789123` |
| Cliente Carla Sousa | NIF `257891234` |
| Fornecedor ProductHero | `11PT920` |
| Fornecedor L'industrie Alimentaire | `964070328` |

## Regras de nomes
Seguir sempre estas regras nas classes e nos ficheiros novos:

| O quê | Nome | Pasta | Exemplo |
|---|---|---|---|
| Entidade | `Xxx` | `model` | `Reserva` |
| Enumeração | `Xxx` | `model.enums` | `EstadoReserva` |
| Interface da DAL | `IXxxDAL` | `dal.interfaces` | `IReservaDAL` |
| Acesso a dados | `XxxDAL` | `dal` | `ReservaDAL` |
| Regras de negócio | `XxxBLL` | `bll` | `ReservaBLL` |
| Exceção | `XxxException` | `exception` | `ValidacaoException` |
| Controlador | `XxxController` | `controller` | `GerirReservasController` |
| Ecrã | `xxx-view.fxml`, com o mesmo nome do controlador | `view` | `gerir-reservas-view.fxml` |
| Teste | `XxxTest`, no mesmo pacote da classe testada | `src/test/java` | `ReservaBLLTest` |
| Script SQL | `NN_descricao.sql`, com o número reservado no Sprint Planning | `Scrum/Base de Dados` | `15_acessos_cartao.sql` |

Nos campos, o nome segue o tipo: um `ReservaBLL` chama-se `reservaBLL` e um `IReservaDAL` chama-se `reservaDAL`.
Os controladores só falam com a BLL, e a BLL só fala com a base de dados através das interfaces da DAL.

## Bibliotecas externas (justificação)
| Biblioteca | Para que serve | Porquê |
|---|---|---|
| `mssql-jdbc` (Microsoft) | Driver JDBC para o SQL Server | O enunciado exige SQL Server, e este é o driver oficial |
| `dotenv-java` | Ler as credenciais do ficheiro `.env` | Evita passwords no código e no repositório |
| JavaFX (`javafx-controls`, `javafx-fxml`) | Interface gráfica | Recomendada no enunciado, com os ecrãs separados da lógica |
| `gson` | Ler o ficheiro JSON da entrega (US64) | Biblioteca da Google, leve e sem outras dependências; o modo estrito recusa JSON mal formado e indica a linha |
| JUnit 5 | Testes unitários | Exigidos no enunciado |
| `java.xml` (já vem com o Java) | Ler o XML das entregas e validá-lo com o XSD | É a usada nas aulas (DOM e `Validator`) e não obriga a juntar mais nenhuma biblioteca ao projeto |

Plugins do Maven (só servem para construir o projeto, não vão dentro da aplicação):

| Plugin | Para que serve | Porquê |
|---|---|---|
| `maven-shade-plugin` | Juntar a aplicação e as bibliotecas num só `.jar`, com o JavaFX de Windows, Linux e Mac | Assim o executável corre com `java -jar` em qualquer sistema, sem o IntelliJ nem o Maven. O Mac com processador Apple tem um `.jar` à parte, porque as bibliotecas do JavaFX para Mac Intel e para Mac Apple têm os mesmos nomes |
| `maven-resources-plugin` | Copiar os lançadores (`.bat`, `.command` e `.sh`) e o `.env.example` para a pasta do executável | É o plugin do próprio Maven para copiar ficheiros |
| `exec-maven-plugin` | Correr a ferramenta `javadoc` do Java com todos os pacotes | O projeto é um módulo e só exporta o pacote principal; desta forma o Javadoc inclui o `model`, a `bll`, a `dal` e o resto |

## Regras de Git
- Uma branch por story: `feat-0XX-descricao` ou `fix-0XX-descricao`, em minúsculas.
- Commits curtos, que dizem o que mudou, um por ideia.
- As alterações entram por pull request revisto por outro elemento.
- A `Quality` só recebe alterações da `main`, no fim de cada sprint.