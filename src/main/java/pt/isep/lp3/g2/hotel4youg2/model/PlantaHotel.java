package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Planta do hotel: os locais e os quartos de cada piso, para picar o cartão.
 */
public class PlantaHotel {

    private final List<Quarto> quartos;
    private final List<Integer> pisos = new ArrayList<>();

    /**
     * Cria a planta com os quartos do hotel. Os outros locais vêm de {@link LocalHotel}.
     *
     * @param quartos os quartos do hotel
     */
    public PlantaHotel(List<Quarto> quartos) {
        this.quartos = quartos == null ? new ArrayList<>() : new ArrayList<>(quartos);
        for (LocalHotel local : LocalHotel.values()) {
            if (local != LocalHotel.QUARTO) {
                juntarPiso(local.getPiso());
            }
        }
        for (Quarto quarto : this.quartos) {
            juntarPiso(quarto.getPiso());
        }
        Collections.sort(pisos);
    }

    private void juntarPiso(int piso) {
        if (!pisos.contains(piso)) {
            pisos.add(piso);
        }
    }

    /**
     * @return os pisos do hotel, do mais baixo para o mais alto
     */
    public List<Integer> getPisos() {
        return new ArrayList<>(pisos);
    }

    /**
     * @param piso o piso
     * @return os locais do piso: primeiro os outros locais e depois os quartos
     */
    public List<LocalPicagem> locaisDoPiso(int piso) {
        List<LocalPicagem> locais = new ArrayList<>();
        for (LocalHotel local : LocalHotel.values()) {
            if (local != LocalHotel.QUARTO && local.getPiso() == piso) {
                locais.add(new LocalPicagem(local, 0));
            }
        }
        for (Quarto quarto : quartos) {
            if (quarto.getPiso() == piso) {
                locais.add(LocalPicagem.quarto(quarto.getNumero()));
            }
        }
        return locais;
    }

    /**
     * @param piso o piso
     * @return a primeira metade dos locais do piso (a fila de cima da planta)
     */
    public List<LocalPicagem> filaDeCima(int piso) {
        List<LocalPicagem> locais = locaisDoPiso(piso);
        int metade = (locais.size() + 1) / 2;
        return new ArrayList<>(locais.subList(0, metade));
    }

    /**
     * @param piso o piso
     * @return a segunda metade dos locais do piso (a fila de baixo da planta)
     */
    public List<LocalPicagem> filaDeBaixo(int piso) {
        List<LocalPicagem> locais = locaisDoPiso(piso);
        int metade = (locais.size() + 1) / 2;
        return new ArrayList<>(locais.subList(metade, locais.size()));
    }

    /**
     * @param piso o piso
     * @return {@code true} se o piso tem quartos
     */
    public boolean temQuartos(int piso) {
        for (Quarto quarto : quartos) {
            if (quarto.getPiso() == piso) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param local o local
     * @return o piso onde fica o local (0 se o quarto não existir)
     */
    public int pisoDe(LocalPicagem local) {
        if (local.getLocal() == LocalHotel.QUARTO) {
            Quarto quarto = procurarQuarto(local.getQuartoNumero());
            return quarto == null ? 0 : quarto.getPiso();
        }
        return local.getLocal().getPiso();
    }

    /**
     * @param numero o número do quarto
     * @return o quarto, ou {@code null} se não existir
     */
    public Quarto procurarQuarto(int numero) {
        for (Quarto quarto : quartos) {
            if (quarto.getNumero() == numero) {
                return quarto;
            }
        }
        return null;
    }
}