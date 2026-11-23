package pt.isep.lp3.g2.hotel4youg2.dal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import pt.isep.lp3.g2.hotel4youg2.exception.ErrosFicheiroException;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.Fornecedor;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.enums.Unidade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê e valida o ficheiro JSON de uma entrega, com as mesmas regras do XML.
 */
public class LeitorJsonEntrega {

    private static final Pattern LINHA_COLUNA = Pattern.compile("line (\\d+) column (\\d+)");

    /**
     * Lê e valida o ficheiro JSON de uma entrega.
     *
     * @param ficheiro o ficheiro JSON da entrega
     * @param erros    onde ficam os erros do conteúdo encontrados ao converter
     * @return a entrega lida (só é válida se {@code erros} continuar vazia)
     * @throws ErrosFicheiroException se o ficheiro não abrir, não for um JSON bem formado ou faltarem campos
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
     * @param entrada o fluxo com o conteúdo do JSON
     * @param erros   onde ficam os erros do conteúdo encontrados ao converter
     * @return a entrega lida (só é válida se {@code erros} continuar vazia)
     * @throws ErrosFicheiroException se o fluxo não puder ser lido, não for um JSON bem formado ou faltarem campos
     */
    public Entrega ler(InputStream entrada, List<String> erros) {
        try {
            return ler(entrada.readAllBytes(), erros);
        } catch (IOException e) {
            throw new ErrosFicheiroException("Não foi possível ler o ficheiro.");
        }
    }

    private Entrega ler(byte[] conteudo, List<String> erros) {
        JsonObject raiz = lerDocumento(new String(conteudo, StandardCharsets.UTF_8));
        List<String> errosEstrutura = new ArrayList<>();
        Entrega entrega = converter(raiz, errosEstrutura, erros);
        if (!errosEstrutura.isEmpty()) {
            throw new ErrosFicheiroException(errosEstrutura);
        }
        return entrega;
    }

    private JsonObject lerDocumento(String texto) {
        try {
            JsonReader leitor = new JsonReader(new StringReader(texto));
            leitor.setStrictness(Strictness.STRICT);
            JsonElement documento = new Gson().getAdapter(JsonElement.class).read(leitor);
            if (leitor.peek() != JsonToken.END_DOCUMENT) {
                throw new ErrosFicheiroException("O JSON não está bem formado: há texto a mais depois do fim.");
            }
            if (documento == null || !documento.isJsonObject()) {
                throw new ErrosFicheiroException("O ficheiro não é um JSON de uma entrega: falta o objeto principal.");
            }
            return documento.getAsJsonObject();
        } catch (IOException | JsonParseException | IllegalStateException e) {
            throw new ErrosFicheiroException(semFormato(e.getMessage()));
        }
    }

    private static String semFormato(String mensagem) {
        Matcher posicao = LINHA_COLUNA.matcher(mensagem == null ? "" : mensagem);
        if (posicao.find()) {
            return "Linha " + posicao.group(1) + ", coluna " + posicao.group(2)
                    + " do ficheiro: o JSON não está bem formado.";
        }
        return "O ficheiro não é um JSON bem formado.";
    }

    private Entrega converter(JsonObject raiz, List<String> estrutura, List<String> erros) {
        JsonObject encomenda = objeto(raiz, "Order", "Ficheiro", estrutura);
        JsonObject cabecalho = objeto(encomenda, "Header", "Order", estrutura);
        String numero = texto(cabecalho, "OrderNumber", "Cabeçalho", estrutura);
        LocalDate data = lerData(texto(cabecalho, "OrderDate", "Cabeçalho", estrutura), erros);
        Fornecedor fornecedor = lerFornecedor(objeto(cabecalho, "SupplierParty", "Cabeçalho", estrutura), estrutura);

        JsonArray linhas = lista(encomenda, "Line", "Order", estrutura);
        List<LinhaEntrega> lidas = new ArrayList<>();
        String moeda = null;
        if (linhas != null) {
            if (linhas.isEmpty()) {
                estrutura.add("Order, campo Line: a encomenda não tem linhas.");
            }
            for (int i = 0; i < linhas.size(); i++) {
                String onde = "Linha " + (i + 1);
                if (!linhas.get(i).isJsonObject()) {
                    estrutura.add(onde + ": a linha tem de ser um objeto.");
                    continue;
                }
                JsonObject linha = linhas.get(i).getAsJsonObject();
                if (moeda == null) {
                    moeda = moedaDe(objeto(linha, "PricePerUnit", onde, estrutura), onde, estrutura);
                }
                LinhaEntrega lida = converterLinha(linha, onde, moeda, estrutura, erros);
                if (lida != null) {
                    lidas.add(lida);
                }
            }
        }
        if (!estrutura.isEmpty()) {
            return null;
        }
        Entrega entrega = new Entrega(0, numero, data, fornecedor);
        entrega.setMoeda(moeda);
        for (LinhaEntrega linha : lidas) {
            entrega.adicionarLinha(linha);
        }
        return entrega;
    }

    private static Fornecedor lerFornecedor(JsonObject fornecedor, List<String> estrutura) {
        String onde = "Cabeçalho, SupplierParty";
        JsonObject nomeMorada = objeto(fornecedor, "NameAddress", onde, estrutura);
        JsonObject pais = objeto(nomeMorada, "Country", onde + ".NameAddress", estrutura);
        return new Fornecedor(0,
                texto(fornecedor, "PartyIdentifier", onde, estrutura),
                corrigirAcentos(texto(nomeMorada, "Name", onde + ".NameAddress", estrutura)),
                corrigirAcentos(texto(nomeMorada, "Address1", onde + ".NameAddress", estrutura)),
                corrigirAcentos(opcional(nomeMorada, "Address2")),
                corrigirAcentos(texto(nomeMorada, "City", onde + ".NameAddress", estrutura)),
                texto(nomeMorada, "PostalCode", onde + ".NameAddress", estrutura),
                corrigirAcentos(texto(pais, "Value", onde + ".NameAddress.Country", estrutura)),
                texto(pais, "ISOCountryCode", onde + ".NameAddress.Country", estrutura));
    }

    private LinhaEntrega converterLinha(JsonObject linha, String onde, String moeda, List<String> estrutura,
                                        List<String> erros) {
        int antes = estrutura.size();
        Integer numero = inteiro(linha, "LineNumber", onde, estrutura);
        String inicio = numero == null ? onde : "Linha " + numero;
        JsonObject produto = objeto(linha, "Product", inicio, estrutura);
        String codigo = codigoDoProduto(produto, inicio, estrutura);
        String descricao = corrigirAcentos(texto(produto, "ProductDescription", inicio + ", Product", estrutura));
        JsonObject quantidade = objeto(objeto(linha, "Quantity", inicio, estrutura), "Value", inicio + ", Quantity",
                estrutura);
        String uom = texto(quantidade, "UOM", inicio + ", Quantity.Value", estrutura);
        Integer quantidadeLida = inteiro(quantidade, "Value", inicio + ", Quantity.Value", estrutura);

        JsonObject preco = objeto(linha, "PricePerUnit", inicio, estrutura);
        JsonObject ajuste = objeto(linha, "MonetaryAdjustment", inicio, estrutura);
        JsonObject semIva = objeto(ajuste, "MonetaryAdjustmentStartAmount", inicio + ", MonetaryAdjustment", estrutura);
        JsonObject imposto = objeto(ajuste, "TaxAdjustment", inicio + ", MonetaryAdjustment", estrutura);
        Integer taxa = inteiro(imposto, "TaxPercent", inicio + ", TaxAdjustment", estrutura);
        JsonObject iva = objeto(imposto, "TaxAmount", inicio + ", TaxAdjustment", estrutura);
        JsonObject total = objeto(linha, "LineBaseAmount", inicio, estrutura);
        BigDecimal valorPreco = valor(preco, inicio + ", PricePerUnit", estrutura);
        BigDecimal valorSemIva = valor(semIva, inicio + ", MonetaryAdjustmentStartAmount", estrutura);
        BigDecimal valorIva = valor(iva, inicio + ", TaxAmount", estrutura);
        BigDecimal valorTotal = valor(total, inicio + ", LineBaseAmount", estrutura);
        if (estrutura.size() > antes) {
            return null;
        }

        verificarMoeda(preco, "PricePerUnit", inicio, moeda, erros);
        verificarMoeda(semIva, "MonetaryAdjustmentStartAmount", inicio, moeda, erros);
        verificarMoeda(iva, "TaxAmount", inicio, moeda, erros);
        verificarMoeda(total, "LineBaseAmount", inicio, moeda, erros);
        Unidade unidade;
        try {
            unidade = Unidade.doXml(uom);
        } catch (IllegalArgumentException e) {
            erros.add(inicio + ", campo Quantity.Value.UOM: a unidade " + uom
                    + " não é conhecida (use Unit, Box ou Kilogram).");
            return null;
        }
        int unidades = lerUnidades(linha, inicio, quantidadeLida, unidade, erros);
        return new LinhaEntrega(numero, codigo, descricao, quantidadeLida, unidade, unidades, valorPreco,
                valorSemIva, taxa, valorIva, valorTotal);
    }

    private static String codigoDoProduto(JsonObject produto, String onde, List<String> estrutura) {
        JsonArray identificadores = lista(produto, "ProductIdentifier", onde + ", Product", estrutura);
        if (identificadores == null) {
            return null;
        }
        String codigo = null;
        for (JsonElement elemento : identificadores) {
            if (elemento.isJsonObject()) {
                JsonObject identificador = elemento.getAsJsonObject();
                String agencia = opcional(identificador, "Agency");
                if (codigo == null || "Supplier".equals(agencia)) {
                    codigo = opcional(identificador, "Code");
                }
            }
        }
        if (codigo == null) {
            estrutura.add(onde + ", campo Product.ProductIdentifier: falta o código do produto (Code).");
        }
        return codigo;
    }

    private static int lerUnidades(JsonObject linha, String onde, int quantidade, Unidade unidade, List<String> erros) {
        if (unidade == Unidade.UNIT) {
            return quantidade;
        }
        JsonElement informativas = linha.get("InformationalQuantity");
        if (informativas != null && informativas.isJsonArray()) {
            for (JsonElement elemento : informativas.getAsJsonArray()) {
                if (elemento.isJsonObject() && elemento.getAsJsonObject().get("Value") != null
                        && elemento.getAsJsonObject().get("Value").isJsonObject()) {
                    JsonObject valor = elemento.getAsJsonObject().getAsJsonObject("Value");
                    BigDecimal unidades = numero(valor.get("Value"));
                    if ("Unit".equals(opcional(valor, "UOM")) && unidades != null && unidades.signum() > 0
                            && unidades.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0) {
                        return unidades.intValue();
                    }
                }
            }
        }
        erros.add(onde + ", campo InformationalQuantity: a quantidade vem em " + unidade.getDescricao()
                + " e o ficheiro não indica quantas unidades são.");
        return 0;
    }

    private static LocalDate lerData(String data, List<String> erros) {
        if (data == null) {
            return null;
        }
        try {
            return LocalDate.parse(data);
        } catch (DateTimeParseException e) {
            erros.add("Cabeçalho, campo OrderDate: a data da encomenda não existe ou não está no formato "
                    + "AAAA-MM-DD (" + data + ").");
            return null;
        }
    }

    private static void verificarMoeda(JsonObject campo, String nome, String onde, String moeda, List<String> erros) {
        String moedaDoCampo = opcional(campo.getAsJsonObject("CurrencyValue"), "CurrencyType");
        if (moeda != null && !moeda.equals(moedaDoCampo)) {
            erros.add(onde + ", campo " + nome + ": o valor está em " + moedaDoCampo + ", mas a encomenda está em "
                    + moeda + ".");
        }
    }

    private static String moedaDe(JsonObject campo, String onde, List<String> estrutura) {
        JsonObject valor = objeto(campo, "CurrencyValue", onde + ", PricePerUnit", estrutura);
        return texto(valor, "CurrencyType", onde + ", PricePerUnit.CurrencyValue", estrutura);
    }

    private static BigDecimal valor(JsonObject campo, String onde, List<String> estrutura) {
        JsonObject valor = objeto(campo, "CurrencyValue", onde, estrutura);
        if (valor == null) {
            return null;
        }
        BigDecimal lido = numero(valor.get("Value"));
        if (lido == null) {
            estrutura.add(onde + ", campo CurrencyValue.Value: tem de ser um número.");
        }
        return lido;
    }

    private static Integer inteiro(JsonObject pai, String nome, String onde, List<String> estrutura) {
        if (pai == null) {
            return null;
        }
        JsonElement campo = pai.get(nome);
        if (campo == null) {
            estrutura.add(onde + ": falta o campo " + nome + ".");
            return null;
        }
        BigDecimal lido = numero(campo);
        if (lido == null || lido.stripTrailingZeros().scale() > 0) {
            estrutura.add(onde + ", campo " + nome + ": tem de ser um número inteiro.");
            return null;
        }
        try {
            return lido.intValueExact();
        } catch (ArithmeticException e) {
            estrutura.add(onde + ", campo " + nome + ": tem de ser um número inteiro.");
            return null;
        }
    }

    private static BigDecimal numero(JsonElement campo) {
        if (campo == null || !campo.isJsonPrimitive() || !campo.getAsJsonPrimitive().isNumber()) {
            return null;
        }
        return campo.getAsBigDecimal();
    }

    private static String texto(JsonObject pai, String nome, String onde, List<String> estrutura) {
        if (pai == null) {
            return null;
        }
        JsonElement campo = pai.get(nome);
        if (campo == null || campo.isJsonNull()) {
            estrutura.add(onde + ": falta o campo " + nome + ".");
            return null;
        }
        if (!campo.isJsonPrimitive() || campo.getAsString().isBlank()) {
            estrutura.add(onde + ", campo " + nome + ": tem de ter um texto.");
            return null;
        }
        return campo.getAsString().trim();
    }

    private static String opcional(JsonObject pai, String nome) {
        if (pai == null || pai.get(nome) == null || !pai.get(nome).isJsonPrimitive()) {
            return null;
        }
        return pai.get(nome).getAsString().trim();
    }

    private static JsonObject objeto(JsonObject pai, String nome, String onde, List<String> estrutura) {
        if (pai == null) {
            return null;
        }
        JsonElement campo = pai.get(nome);
        if (campo == null) {
            estrutura.add(onde + ": falta o campo " + nome + ".");
            return null;
        }
        if (!campo.isJsonObject()) {
            estrutura.add(onde + ", campo " + nome + ": tem de ser um objeto { }.");
            return null;
        }
        return campo.getAsJsonObject();
    }

    private static JsonArray lista(JsonObject pai, String nome, String onde, List<String> estrutura) {
        if (pai == null) {
            return null;
        }
        JsonElement campo = pai.get(nome);
        if (campo == null) {
            estrutura.add(onde + ": falta o campo " + nome + ".");
            return null;
        }
        if (!campo.isJsonArray()) {
            estrutura.add(onde + ", campo " + nome + ": tem de ser uma lista [ ].");
            return null;
        }
        return campo.getAsJsonArray();
    }

    static String corrigirAcentos(String texto) {
        if (texto == null || (texto.indexOf('Ã') < 0 && texto.indexOf('Â') < 0)) {
            return texto;
        }
        for (int i = 0; i < texto.length(); i++) {
            if (texto.charAt(i) > 0xFF) {
                return texto;
            }
        }
        String corrigido = new String(texto.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        return corrigido.indexOf('�') >= 0 ? texto : corrigido;
    }
}