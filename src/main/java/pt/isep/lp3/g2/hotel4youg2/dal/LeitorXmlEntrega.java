package pt.isep.lp3.g2.hotel4youg2.dal;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import pt.isep.lp3.g2.hotel4youg2.exception.ErrosFicheiroException;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Unidade;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lê o ficheiro XML de uma entrega de um fornecedor, em três passos: confirma que o XML está bem formado,
 * valida-o com o XSD e converte-o em objetos do modelo. O XSD é o do projeto, ou o que o fornecedor
 * importou enquanto a aplicação está aberta (US53).
 */
public class LeitorXmlEntrega {

    private static final String XSD_DO_PROJETO = "/pt/isep/lp3/g2/hotel4youg2/xml/Hotel4You.xsd";

    // o XSD importado pelo fornecedor; null enquanto não importar nenhum (usa-se o do projeto)
    private static Schema xsdImportado;
    private static String nomeXsdImportado;

    /**
     * Importa o XSD que passa a validar as entregas enquanto a aplicação estiver aberta.
     * Se o ficheiro não for um XSD válido, é recusado e continua o XSD anterior.
     *
     * @param ficheiro o ficheiro XSD
     * @throws ErrosFicheiroException se o ficheiro não abrir ou não for um XSD válido
     */
    public static void importarXsd(File ficheiro) {
        if (!ficheiro.isFile()) {
            throw new ErrosFicheiroException("Não foi possível abrir o ficheiro " + ficheiro.getName() + ".");
        }
        Schema novo = compilarXsd(new StreamSource(ficheiro));
        xsdImportado = novo;
        nomeXsdImportado = ficheiro.getName();
    }

    /**
     * Esquece o XSD importado e volta a usar o XSD do projeto.
     */
    public static void usarXsdDoProjeto() {
        xsdImportado = null;
        nomeXsdImportado = null;
    }

    /**
     * @return o nome do ficheiro XSD importado, ou {@code null} se estiver a ser usado o XSD do projeto
     */
    public static String getNomeXsdImportado() {
        return nomeXsdImportado;
    }

    /**
     * Lê e valida o ficheiro XML de uma entrega.
     *
     * @param ficheiro o ficheiro XML da entrega
     * @param erros    onde ficam os erros do conteúdo (data, unidades, moeda) encontrados ao converter
     * @return a entrega lida (só é válida se {@code erros} continuar vazia)
     * @throws ErrosFicheiroException se o ficheiro não abrir, não for um XML bem formado
     *                                ou não seguir o XSD (com todos os erros do XSD)
     */
    public Entrega ler(File ficheiro, List<String> erros) {
        try {
            return ler(Files.readAllBytes(ficheiro.toPath()), erros);
        } catch (IOException e) {
            throw new ErrosFicheiroException("Não foi possível abrir o ficheiro " + ficheiro.getName() + ".");
        }
    }

    /**
     * Como {@link #ler(File, List)}, mas lê de um fluxo de dados já aberto.
     *
     * @param entrada o fluxo com o conteúdo do XML
     * @param erros   onde ficam os erros do conteúdo encontrados ao converter
     * @return a entrega lida (só é válida se {@code erros} continuar vazia)
     * @throws ErrosFicheiroException se o fluxo não puder ser lido, não for um XML bem formado
     *                                ou não seguir o XSD
     */
    public Entrega ler(InputStream entrada, List<String> erros) {
        try {
            return ler(entrada.readAllBytes(), erros);
        } catch (IOException e) {
            throw new ErrosFicheiroException("Não foi possível ler o ficheiro.");
        }
    }

    private Entrega ler(byte[] conteudo, List<String> erros) {
        Document documento = lerDocumento(conteudo);
        validarComXsd(conteudo);
        try {
            return converter(documento, erros);
        } catch (NumberFormatException e) {
            // só acontece com um XSD importado que aceita texto onde a aplicação espera números
            throw new ErrosFicheiroException("O ficheiro tem um número que a aplicação não consegue ler: "
                    + e.getMessage());
        }
    }

    // 1.º passo: o XML tem de estar bem formado (tags fechadas, uma só raiz...)
    private Document lerDocumento(byte[] conteudo) {
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setExpandEntityReferences(false);
            DocumentBuilder construtor = fabrica.newDocumentBuilder();
            construtor.setErrorHandler(null);
            return construtor.parse(new ByteArrayInputStream(conteudo));
        } catch (SAXParseException e) {
            throw new ErrosFicheiroException("Linha " + e.getLineNumber()
                    + " do ficheiro: o XML não está bem formado. " + e.getMessage());
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new ErrosFicheiroException("O ficheiro não é um XML válido: " + e.getMessage());
        }
    }

    // 2.º passo: o XML tem de seguir o XSD; junta todos os erros antes de recusar
    private void validarComXsd(byte[] conteudo) {
        List<String> errosXsd = new ArrayList<>();
        try {
            Validator validador = xsdEmUso().newValidator();
            validador.setErrorHandler(new JuntarErros(errosXsd));
            validador.validate(new StreamSource(new ByteArrayInputStream(conteudo)));
        } catch (SAXException e) {
            // erro grave: o validador parou; os erros que já encontrou estão na lista
            if (errosXsd.isEmpty()) {
                errosXsd.add("Não foi possível validar o ficheiro: " + e.getMessage());
            }
        } catch (IOException e) {
            errosXsd.add("Não foi possível validar o ficheiro: " + e.getMessage());
        }
        if (!errosXsd.isEmpty()) {
            throw new ErrosFicheiroException(errosXsd);
        }
    }

    private static Schema xsdEmUso() {
        if (xsdImportado != null) {
            return xsdImportado;
        }
        try (InputStream xsd = LeitorXmlEntrega.class.getResourceAsStream(XSD_DO_PROJETO)) {
            return compilarXsd(new StreamSource(xsd));
        } catch (IOException e) {
            throw new ErrosFicheiroException("Não foi possível abrir o XSD do projeto.");
        }
    }

    // transforma o ficheiro XSD num esquema; se não for um XSD válido, recusa com o primeiro erro
    // (um ficheiro que nem é XSD dá dezenas de erros, que não ajudam ninguém)
    private static Schema compilarXsd(Source xsd) {
        List<String> errosXsd = new ArrayList<>();
        try {
            SchemaFactory fabrica = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            fabrica.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            fabrica.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            fabrica.setErrorHandler(new JuntarErros(errosXsd));
            Schema esquema = fabrica.newSchema(xsd);
            if (errosXsd.isEmpty()) {
                return esquema;
            }
        } catch (SAXException e) {
            if (errosXsd.isEmpty()) {
                errosXsd.add(e.getMessage());
            }
        }
        throw new ErrosFicheiroException("O ficheiro não é um XSD válido. " + errosXsd.get(0));
    }

    // 3.º passo: passar do XML para os objetos do modelo, apontando os erros do conteúdo
    private Entrega converter(Document documento, List<String> erros) {
        Element encomenda = documento.getDocumentElement();
        Element cabecalho = filho(encomenda, "Header");
        Element data = filho(filho(cabecalho, "OrderDate"), "Date");
        Element fornecedorXml = filho(cabecalho, "SupplierParty");
        Element nomeMorada = filho(fornecedorXml, "NameAddress");
        Element pais = filho(nomeMorada, "Country");

        Fornecedor fornecedor = new Fornecedor(0,
                texto(fornecedorXml, "PartyIdentifier"),
                texto(nomeMorada, "Name"),
                texto(nomeMorada, "Address1"),
                texto(nomeMorada, "Address2"),
                texto(nomeMorada, "City"),
                texto(nomeMorada, "PostalCode"),
                pais.getTextContent().trim(),
                pais.getAttribute("ISOCountryCode"));

        Entrega entrega = new Entrega(0, texto(cabecalho, "OrderNumber"), lerData(data, erros), fornecedor);

        NodeList linhas = encomenda.getElementsByTagName("Line");
        // a moeda da encomenda é a do preço da primeira linha; as outras têm de ser iguais
        String moeda = moeda(filho(filho((Element) linhas.item(0), "PricePerUnit"), "CurrencyValue"));
        entrega.setMoeda(moeda);
        for (int i = 0; i < linhas.getLength(); i++) {
            entrega.adicionarLinha(converterLinha((Element) linhas.item(i), moeda, erros));
        }
        return entrega;
    }

    private LinhaEntrega converterLinha(Element linha, String moeda, List<String> erros) {
        Element produto = filho(linha, "Product");
        Element quantidade = filho(filho(linha, "Quantity"), "Value");
        Element ajuste = filho(linha, "MonetaryAdjustment");
        Element imposto = filho(ajuste, "TaxAdjustment");
        int numero = Integer.parseInt(texto(linha, "LineNumber"));
        int quantidadeLida = Integer.parseInt(quantidade.getTextContent().trim());
        Unidade unidade = Unidade.doXml(quantidade.getAttribute("UOM"));

        Element preco = filho(linha, "PricePerUnit");
        Element valorSemIva = filho(ajuste, "MonetaryAdjustmentStartAmount");
        Element valorIva = filho(imposto, "TaxAmount");
        Element total = filho(linha, "LineBaseAmount");
        verificarMoeda(preco, numero, moeda, erros);
        verificarMoeda(valorSemIva, numero, moeda, erros);
        verificarMoeda(valorIva, numero, moeda, erros);
        verificarMoeda(total, numero, moeda, erros);

        return new LinhaEntrega(
                numero,
                texto(produto, "ProductIdentifier"),
                texto(produto, "ProductDescription"),
                quantidadeLida,
                unidade,
                lerUnidades(linha, numero, quantidadeLida, unidade, erros),
                valor(preco),
                valor(valorSemIva),
                Integer.parseInt(texto(imposto, "TaxPercent")),
                valor(valorIva),
                valor(total));
    }

    // cada valor em dinheiro tem de estar na moeda da encomenda
    private static void verificarMoeda(Element campo, int numeroLinha, String moeda, List<String> erros) {
        String moedaDoCampo = moeda(filho(campo, "CurrencyValue"));
        if (!moedaDoCampo.equals(moeda)) {
            erros.add("Linha " + numeroLinha + ", campo " + campo.getTagName() + ": o valor está em "
                    + moedaDoCampo + ", mas a encomenda está em " + moeda + ".");
        }
    }

    private static String moeda(Element valor) {
        return valor.getAttribute("CurrencyType").trim();
    }

    // total da linha em unidades: se a quantidade já vem em unidades é essa; se vem em caixas ou quilos,
    // é a contagem em unidades que o ficheiro indica em <InformationalQuantity>
    private static int lerUnidades(Element linha, int numeroLinha, int quantidade, Unidade unidade,
                                   List<String> erros) {
        if (unidade == Unidade.UNIT) {
            return quantidade;
        }
        for (Node no = linha.getFirstChild(); no != null; no = no.getNextSibling()) {
            if (no instanceof Element elemento && elemento.getTagName().equals("InformationalQuantity")
                    && elemento.getAttribute("QuantityType").equals("Count")) {
                Element valor = filho(elemento, "Value");
                if (valor.getAttribute("UOM").equals("Unit")) {
                    int unidades = Integer.parseInt(valor.getTextContent().trim());
                    if (unidades > 0) {
                        return unidades;
                    }
                }
            }
        }
        erros.add("Linha " + numeroLinha + ", campo InformationalQuantity: a quantidade vem em "
                + unidade.getDescricao() + " e o ficheiro não indica quantas unidades são.");
        return 0;
    }

    // devolve null se a data não existir (por exemplo, mês 13), e aponta o erro
    private static LocalDate lerData(Element data, List<String> erros) {
        int ano = Integer.parseInt(texto(data, "Year"));
        int mes = Integer.parseInt(texto(data, "Month"));
        int dia = Integer.parseInt(texto(data, "Day"));
        try {
            return LocalDate.of(ano, mes, dia);
        } catch (DateTimeException e) {
            erros.add("Cabeçalho, campo OrderDate: a data da encomenda não existe (" + dia + "/" + mes + "/"
                    + ano + ").");
            return null;
        }
    }

    private static BigDecimal valor(Element pai) {
        return new BigDecimal(texto(pai, "CurrencyValue"));
    }

    private static String texto(Element pai, String nome) {
        return filho(pai, nome).getTextContent().trim();
    }

    // devolve o filho direto com este nome (o XSD do projeto garante que existe)
    private static Element filho(Element pai, String nome) {
        for (Node no = pai.getFirstChild(); no != null; no = no.getNextSibling()) {
            if (no instanceof Element elemento && elemento.getTagName().equals(nome)) {
                return elemento;
            }
        }
        throw new ErrosFicheiroException("Falta o elemento <" + nome + "> dentro de <" + pai.getTagName() + ">.");
    }

    // junta os erros do XSD numa lista em vez de parar no primeiro
    private static class JuntarErros implements ErrorHandler {

        private final List<String> erros;

        JuntarErros(List<String> erros) {
            this.erros = erros;
        }

        @Override
        public void warning(SAXParseException e) {
            // os avisos não impedem a validação
        }

        @Override
        public void error(SAXParseException e) {
            erros.add(descrever(e));
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            erros.add(descrever(e));
            throw e;
        }

        private static String descrever(SAXParseException e) {
            return "Linha " + e.getLineNumber() + " do ficheiro: " + e.getMessage();
        }
    }
}
