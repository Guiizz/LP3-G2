package pt.isep.lp3.g2.hotel4youg2.dal;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Unidade;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

/**
 * Lê o ficheiro XML de uma entrega de um fornecedor. Primeiro confirma que o XML está bem formado,
 * depois valida-o com o XSD combinado com os fornecedores e só no fim o converte em objetos do modelo.
 */
public class LeitorXmlEntrega {

    private static final String XSD = "/pt/isep/lp3/g2/hotel4youg2/xml/Hotel4You.xsd";

    /**
     * Lê e valida o ficheiro XML de uma entrega.
     *
     * @param ficheiro o ficheiro XML da entrega
     * @return a entrega lida
     * @throws IllegalArgumentException se o ficheiro não abrir, não for um XML bem formado
     *                                  ou não seguir o formato das entregas
     */
    public Entrega ler(File ficheiro) {
        try {
            return ler(Files.readAllBytes(ficheiro.toPath()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível abrir o ficheiro " + ficheiro.getName() + ".");
        }
    }

    /**
     * Lê e valida o XML de uma entrega a partir de um fluxo de dados já aberto.
     *
     * @param entrada o fluxo com o conteúdo do XML
     * @return a entrega lida
     * @throws IllegalArgumentException se o fluxo não puder ser lido, não for um XML bem formado
     *                                  ou não seguir o formato das entregas
     */
    public Entrega ler(InputStream entrada) {
        try {
            return ler(entrada.readAllBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o ficheiro.");
        }
    }

    private Entrega ler(byte[] conteudo) {
        Document documento = lerDocumento(conteudo);
        validarComXsd(conteudo);
        return converter(documento);
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
            throw new IllegalArgumentException("O ficheiro não é um XML bem formado (linha "
                    + e.getLineNumber() + "): " + e.getMessage());
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new IllegalArgumentException("O ficheiro não é um XML válido: " + e.getMessage());
        }
    }

    // 2.º passo: o XML tem de seguir o XSD combinado com os fornecedores
    private void validarComXsd(byte[] conteudo) {
        try (InputStream xsd = LeitorXmlEntrega.class.getResourceAsStream(XSD)) {
            SchemaFactory fabrica = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            fabrica.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            fabrica.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Schema esquema = fabrica.newSchema(new StreamSource(xsd));
            Validator validador = esquema.newValidator();
            validador.validate(new StreamSource(new ByteArrayInputStream(conteudo)));
        } catch (SAXParseException e) {
            throw new IllegalArgumentException("O ficheiro não segue o formato das entregas (linha "
                    + e.getLineNumber() + "): " + e.getMessage());
        } catch (SAXException | IOException e) {
            throw new IllegalArgumentException("Não foi possível validar o ficheiro: " + e.getMessage());
        }
    }

    // 3.º passo: passar do XML para os objetos do modelo
    private Entrega converter(Document documento) {
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

        Entrega entrega = new Entrega(0, texto(cabecalho, "OrderNumber"), lerData(data), fornecedor);

        NodeList linhas = encomenda.getElementsByTagName("Line");
        for (int i = 0; i < linhas.getLength(); i++) {
            entrega.adicionarLinha(converterLinha((Element) linhas.item(i)));
        }
        return entrega;
    }

    private LinhaEntrega converterLinha(Element linha) {
        Element produto = filho(linha, "Product");
        Element quantidade = filho(filho(linha, "Quantity"), "Value");
        Element ajuste = filho(linha, "MonetaryAdjustment");
        Element imposto = filho(ajuste, "TaxAdjustment");
        int numero = Integer.parseInt(texto(linha, "LineNumber"));
        int quantidadeLida = Integer.parseInt(quantidade.getTextContent().trim());
        Unidade unidade = Unidade.doXml(quantidade.getAttribute("UOM"));

        return new LinhaEntrega(
                numero,
                texto(produto, "ProductIdentifier"),
                texto(produto, "ProductDescription"),
                quantidadeLida,
                unidade,
                lerUnidades(linha, numero, quantidadeLida, unidade),
                valor(filho(linha, "PricePerUnit")),
                valor(filho(ajuste, "MonetaryAdjustmentStartAmount")),
                Integer.parseInt(texto(imposto, "TaxPercent")),
                valor(filho(imposto, "TaxAmount")),
                valor(filho(linha, "LineBaseAmount")));
    }

    // total da linha em unidades: se a quantidade já vem em unidades é essa; se vem em caixas ou quilos,
    // é a contagem em unidades que o ficheiro indica em <InformationalQuantity>
    private static int lerUnidades(Element linha, int numeroLinha, int quantidade, Unidade unidade) {
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
        throw new IllegalArgumentException("A linha " + numeroLinha + " vem em " + unidade.getDescricao()
                + " e o ficheiro não indica quantas unidades tem.");
    }

    private static LocalDate lerData(Element data) {
        int ano = Integer.parseInt(texto(data, "Year"));
        int mes = Integer.parseInt(texto(data, "Month"));
        int dia = Integer.parseInt(texto(data, "Day"));
        try {
            return LocalDate.of(ano, mes, dia);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("A data da encomenda não existe: " + dia + "/" + mes + "/" + ano + ".");
        }
    }

    private static BigDecimal valor(Element pai) {
        return new BigDecimal(texto(pai, "CurrencyValue"));
    }

    private static String texto(Element pai, String nome) {
        return filho(pai, nome).getTextContent().trim();
    }

    // devolve o filho direto com este nome (o XSD garante que existe)
    private static Element filho(Element pai, String nome) {
        for (Node no = pai.getFirstChild(); no != null; no = no.getNextSibling()) {
            if (no instanceof Element elemento && elemento.getTagName().equals(nome)) {
                return elemento;
            }
        }
        throw new IllegalArgumentException("Falta o elemento <" + nome + "> dentro de <" + pai.getTagName() + ">.");
    }
}
