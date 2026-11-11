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

Os valores pedem-se a um elemento da equipa.

## Testes
Corre `mvn test`. O teste `ConexaoBDTest` é ignorado (não falha) quando não há acesso à base de dados.

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
| JUnit 5 | Testes unitários | Exigidos no enunciado |
| `java.xml` (já vem com o Java) | Ler o XML das entregas e validá-lo com o XSD | É a usada nas aulas (DOM e `Validator`) e não obriga a juntar mais nenhuma biblioteca ao projeto |

## Regras de Git
- Uma branch por story: `feat-0XX-descricao` ou `fix-0XX-descricao`, em minúsculas.
- Commits curtos, que dizem o que mudou, um por ideia.
- As alterações entram por pull request revisto por outro elemento.
- A `Quality` só recebe alterações da `main`, no fim de cada sprint.