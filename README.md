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
| `model` | Entidades do negócio e enumerações |
| `dal` | Acesso a dados: a ligação (`ConexaoBD`) e, depois, os repositórios |
| `bll` | Regras de negócio e validações |
| `controller` | Controladores JavaFX, que ligam os ecrãs à BLL |
| `util` | Classes de apoio |

Os ecrãs (FXML) estão em `src/main/resources/pt/isep/lp3/g2/hotel4youg2/view`.

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