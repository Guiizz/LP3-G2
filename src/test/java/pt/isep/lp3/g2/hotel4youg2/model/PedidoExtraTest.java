package pt.isep.lp3.g2.hotel4youg2.model;

import org.junit.jupiter.api.Test;
import pt.isep.lp3.g2.hotel4youg2.model.enums.EstadoPedido;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PedidoExtraTest {

    @Test
    public void testDeServico() {
        System.out.println("deServico");
        Servico jantar = new Servico(3, "Jantar", new BigDecimal("22.00"), new Categoria(1, "Refeição"), true);
        PedidoExtra expResult = new PedidoExtra(0, 30, 3, 0, "Jantar", 2, EstadoPedido.PENDENTE);
        PedidoExtra result = PedidoExtra.deServico(30, jantar, 2);
        assertEquals(expResult, result);
    }

    @Test
    public void testDeMaterial() {
        System.out.println("deMaterial");
        MaterialStock toalha = new MaterialStock(7, "M7", "Toalha", "Têxteis Lda", 10);
        PedidoExtra expResult = new PedidoExtra(0, 30, 0, 7, "Toalha", 1, EstadoPedido.PENDENTE);
        PedidoExtra result = PedidoExtra.deMaterial(30, toalha, 1);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTipoTextoServico() {
        System.out.println("getTipoTexto (serviço)");
        PedidoExtra instance = new PedidoExtra(1, 30, 3, 0, "Jantar", 1, EstadoPedido.PENDENTE);
        String expResult = "Serviço";
        String result = instance.getTipoTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTipoTextoConsumivel() {
        System.out.println("getTipoTexto (material consumível)");
        PedidoExtra instance = new PedidoExtra(1, 30, 0, 8, "Gel de banho", 1, EstadoPedido.PENDENTE);
        instance.setConsumivel(true);
        String expResult = "Material consumível";
        String result = instance.getTipoTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTipoTextoNaoConsumivel() {
        System.out.println("getTipoTexto (material que volta)");
        PedidoExtra instance = new PedidoExtra(1, 30, 0, 7, "Toalha", 1, EstadoPedido.PENDENTE);
        String expResult = "Material (volta no check-out)";
        String result = instance.getTipoTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testRecusar() {
        System.out.println("recusar");
        PedidoExtra instance = new PedidoExtra(1, 30, 0, 7, "Toalha", 1, EstadoPedido.PENDENTE);
        instance.recusar("Sem stock");
        PedidoExtra expResult = new PedidoExtra(1, 30, 0, 7, "Toalha", 1, EstadoPedido.RECUSADO);
        expResult.setMotivoRecusa("Sem stock");
        PedidoExtra result = instance;
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMotivoTextoSemMotivo() {
        System.out.println("getMotivoTexto (sem motivo)");
        PedidoExtra instance = new PedidoExtra(1, 30, 3, 0, "Jantar", 1, EstadoPedido.ACEITE);
        String expResult = "-";
        String result = instance.getMotivoTexto();
        assertEquals(expResult, result);
    }

    @Test
    public void testToString() {
        System.out.println("toString");
        PedidoExtra instance = new PedidoExtra(1, 30, 3, 0, "Jantar", 2, EstadoPedido.PENDENTE);
        String expResult = "2 x Jantar (Pendente)";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
