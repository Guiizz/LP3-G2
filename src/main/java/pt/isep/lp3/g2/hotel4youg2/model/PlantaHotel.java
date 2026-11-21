package pt.isep.lp3.g2.hotel4youg2.model;

import pt.isep.lp3.g2.hotel4youg2.model.enums.LocalHotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlantaHotel {

    private final List<Quarto> quartos;
    private final List<Integer> pisos = new ArrayList<>();

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

    public List<Integer> getPisos() {
        return new ArrayList<>(pisos);
    }

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

    public List<LocalPicagem> filaDeCima(int piso) {
        List<LocalPicagem> locais = locaisDoPiso(piso);
        int metade = (locais.size() + 1) / 2;
        return new ArrayList<>(locais.subList(0, metade));
    }

    public List<LocalPicagem> filaDeBaixo(int piso) {
        List<LocalPicagem> locais = locaisDoPiso(piso);
        int metade = (locais.size() + 1) / 2;
        return new ArrayList<>(locais.subList(metade, locais.size()));
    }

    public boolean temQuartos(int piso) {
        for (Quarto quarto : quartos) {
            if (quarto.getPiso() == piso) {
                return true;
            }
        }
        return false;
    }

    public int pisoDe(LocalPicagem local) {
        if (local.getLocal() == LocalHotel.QUARTO) {
            Quarto quarto = procurarQuarto(local.getQuartoNumero());
            return quarto == null ? 0 : quarto.getPiso();
        }
        return local.getLocal().getPiso();
    }

    public Quarto procurarQuarto(int numero) {
        for (Quarto quarto : quartos) {
            if (quarto.getNumero() == numero) {
                return quarto;
            }
        }
        return null;
    }
}