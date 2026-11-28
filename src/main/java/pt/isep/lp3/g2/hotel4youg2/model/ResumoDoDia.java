package pt.isep.lp3.g2.hotel4youg2.model;

public class ResumoDoDia {

    private final int checkins;
    private final int checkouts;
    private final int chegadas;
    private final int pedidos;
    private final int entregas;

    public ResumoDoDia(int checkins, int checkouts, int chegadas, int pedidos, int entregas) {
        if (checkins < 0 || checkouts < 0 || chegadas < 0 || pedidos < 0 || entregas < 0) {
            throw new IllegalArgumentException("As contagens não podem ser negativas.");
        }
        this.checkins = checkins;
        this.checkouts = checkouts;
        this.chegadas = chegadas;
        this.pedidos = pedidos;
        this.entregas = entregas;
    }

    public int getCheckins() { return checkins; }
    public int getCheckouts() { return checkouts; }
    public int getChegadas() { return chegadas; }
    public int getPedidos() { return pedidos; }
    public int getEntregas() { return entregas; }

    public boolean temTarefas() {
        return checkins + checkouts + pedidos + entregas > 0;
    }
}