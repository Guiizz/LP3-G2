package pt.isep.lp3.g2.hotel4youg2.controller;

import javafx.util.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtualizadorDeTabelasTest {

    private static class Linha {
        private final int id;
        private final String texto;

        Linha(int id, String texto) {
            this.id = id;
            this.texto = texto;
        }
    }

    private static class IdDaLinha implements AtualizadorDeTabelas.Chave<Linha> {
        @Override
        public Object de(Linha linha) {
            return linha.id;
        }
    }

    private static class EcraFalso implements AtualizadorDeTabelas.Ecra {
        private int vezes = 0;

        @Override
        public void recarregar() {
            vezes++;
        }
    }

    private final AtualizadorDeTabelas.Chave<Linha> pelaChave = new IdDaLinha();
    private final EcraFalso reservas = new EcraFalso();
    private final EcraFalso pedidos = new EcraFalso();

    @AfterEach
    public void tearDown() {
        AtualizadorDeTabelas.esquecer(reservas);
        AtualizadorDeTabelas.esquecer(pedidos);
    }

    @Test
    public void testIntervalo() {
        System.out.println("INTERVALO (30 em 30 segundos)");
        Duration expResult = Duration.seconds(30);
        Duration result = AtualizadorDeTabelas.INTERVALO;
        assertEquals(expResult, result);
    }

    @Test
    public void testPosicaoDaLinhaQueMudou() {
        System.out.println("posicao (a linha escolhida mudou de dados, mas é a mesma)");
        Linha escolhida = new Linha(2, "pendente");
        List<Linha> novas = List.of(new Linha(1, "a"), new Linha(2, "aceite"), new Linha(3, "c"));
        int expResult = 1;
        int result = AtualizadorDeTabelas.posicao(novas, escolhida, pelaChave);
        assertEquals(expResult, result);
    }

    @Test
    public void testPosicaoDepoisDeMudarAOrdem() {
        System.out.println("posicao (a linha escolhida passou para o início da tabela)");
        Linha escolhida = new Linha(3, "c");
        List<Linha> novas = List.of(new Linha(3, "c"), new Linha(1, "a"), new Linha(2, "b"));
        int expResult = 0;
        int result = AtualizadorDeTabelas.posicao(novas, escolhida, pelaChave);
        assertEquals(expResult, result);
    }

    @Test
    public void testPosicaoDaLinhaQueSaiu() {
        System.out.println("posicao (a linha escolhida já não está na tabela)");
        Linha escolhida = new Linha(4, "recusado");
        List<Linha> novas = List.of(new Linha(1, "a"), new Linha(2, "b"));
        int expResult = -1;
        int result = AtualizadorDeTabelas.posicao(novas, escolhida, pelaChave);
        assertEquals(expResult, result);
    }

    @Test
    public void testPosicaoSemLinhaEscolhida() {
        System.out.println("posicao (nenhuma linha estava escolhida)");
        List<Linha> novas = List.of(new Linha(1, "a"), new Linha(2, "b"));
        int expResult = -1;
        int result = AtualizadorDeTabelas.posicao(novas, null, pelaChave);
        assertEquals(expResult, result);
    }

    @Test
    public void testPosicaoNumaTabelaVazia() {
        System.out.println("posicao (a tabela ficou vazia)");
        Linha escolhida = new Linha(1, "a");
        List<Linha> novas = List.of();
        int expResult = -1;
        int result = AtualizadorDeTabelas.posicao(novas, escolhida, pelaChave);
        assertEquals(expResult, result);
    }

    @Test
    public void testAtualizarAbertos() {
        System.out.println("atualizarAbertos (todos os ecrãs abertos recarregam uma vez)");
        AtualizadorDeTabelas.registar(reservas);
        AtualizadorDeTabelas.registar(pedidos);
        AtualizadorDeTabelas.atualizarAbertos();
        List<Integer> expResult = Arrays.asList(1, 1);
        List<Integer> result = Arrays.asList(reservas.vezes, pedidos.vezes);
        assertEquals(expResult, result);
    }

    @Test
    public void testAtualizarAbertosSemOEcraFechado() {
        System.out.println("atualizarAbertos (o ecrã que fechou já não recarrega)");
        AtualizadorDeTabelas.registar(reservas);
        AtualizadorDeTabelas.registar(pedidos);
        AtualizadorDeTabelas.esquecer(reservas);
        AtualizadorDeTabelas.atualizarAbertos();
        List<Integer> expResult = Arrays.asList(0, 1);
        List<Integer> result = Arrays.asList(reservas.vezes, pedidos.vezes);
        assertEquals(expResult, result);
    }

    @Test
    public void testRegistarDuasVezes() {
        System.out.println("registar (o mesmo ecrã duas vezes só recarrega uma)");
        AtualizadorDeTabelas.registar(reservas);
        AtualizadorDeTabelas.registar(reservas);
        AtualizadorDeTabelas.atualizarAbertos();
        int expResult = 1;
        int result = reservas.vezes;
        assertEquals(expResult, result);
    }

    @Test
    public void testAtualizarSemEcrasAbertos() {
        System.out.println("atualizarAbertos (sem ecrãs abertos não faz nada)");
        AtualizadorDeTabelas.atualizarAbertos();
        int expResult = 0;
        int result = reservas.vezes;
        assertEquals(expResult, result);
    }
}
