package pt.isep.lp3.g2.hotel4youg2.bll;

import pt.isep.lp3.g2.hotel4youg2.dal.EntregaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.IEntregaDAL;
import pt.isep.lp3.g2.hotel4youg2.dal.LeitorXmlEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Entrega;
import pt.isep.lp3.g2.hotel4youg2.model.LinhaEntrega;
import pt.isep.lp3.g2.hotel4youg2.model.Utilizador;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class EntregaBLL {

    private final LeitorXmlEntrega leitor;
    private final IEntregaDAL entregaDAL;

    public EntregaBLL() {
        this(new LeitorXmlEntrega(), new EntregaDAL());
    }

    public EntregaBLL(LeitorXmlEntrega leitor, IEntregaDAL entregaDAL) {
        this.leitor = leitor;
        this.entregaDAL = entregaDAL;
    }

    public Entrega lerFicheiro(File ficheiro) {
        if (ficheiro == null) {
            throw new IllegalArgumentException("Escolha um ficheiro XML.");
        }
        return leitor.ler(ficheiro);
    }

    public Entrega lerFicheiro(InputStream entrada) {
        return leitor.ler(entrada);
    }

    // as contas erradas não impedem o registo: devolvem avisos para quem regista decidir
    public List<String> verificarContas(Entrega entrega) {
        List<String> avisos = new ArrayList<>();
        for (LinhaEntrega linha : entrega.getLinhas()) {
            String inicio = "Linha " + linha.getNumero() + " (" + linha.getDescricao() + "): ";
            if (!linha.isValorSemIvaCorreto()) {
                avisos.add(inicio + "a quantidade vezes o preço dá "
                        + euros(linha.getPrecoUnitario().multiply(BigDecimal.valueOf(linha.getQuantidade())))
                        + ", mas o ficheiro diz " + euros(linha.getValorSemIva()) + ".");
            } else if (!linha.isIvaCorreto()) {
                avisos.add(inicio + "o IVA de " + linha.getTaxaIva() + "% devia ser "
                        + euros(linha.getValorSemIva().multiply(BigDecimal.valueOf(linha.getTaxaIva()))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                        + ", mas o ficheiro diz " + euros(linha.getValorIva()) + ".");
            } else if (!linha.isTotalCorreto()) {
                avisos.add(inicio + "o total devia ser " + euros(linha.getValorSemIva().add(linha.getValorIva()))
                        + ", mas o ficheiro diz " + euros(linha.getTotal()) + ".");
            }
        }
        return avisos;
    }

    public int registar(Utilizador quemAge, Entrega entrega) {
        if (quemAge == null) {
            throw new IllegalStateException("Entre na aplicação para registar entregas.");
        }
        if (entrega == null) {
            throw new IllegalArgumentException("Carregue primeiro um ficheiro XML válido.");
        }
        if (entregaDAL.existeEntrega(entrega.getFornecedor().getCodigo(), entrega.getNumeroEncomenda())) {
            throw new IllegalArgumentException("A entrega " + entrega.getNumeroEncomenda() + " do fornecedor "
                    + entrega.getFornecedor().getNome() + " já foi registada.");
        }
        return entregaDAL.registar(entrega, quemAge.getFuncionarioId());
    }

    private static String euros(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',') + " €";
    }
}
