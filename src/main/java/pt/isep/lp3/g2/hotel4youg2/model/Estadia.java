package pt.isep.lp3.g2.hotel4youg2.model;

import java.time.LocalDate;
import java.util.Objects;

public class Estadia {
    private final LocalDate dataEntrada;
    private final LocalDate dataSaida;

    public Estadia(LocalDate dataEntrada, LocalDate dataSaida) {
        this.dataEntrada = dataEntrada;
        this.dataSaida = dataSaida;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public LocalDate getDataSaida() {
        return dataSaida;
    }

    public boolean ocupaNoite(LocalDate dia) {
        return !dia.isBefore(dataEntrada) && dia.isBefore(dataSaida);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Estadia outra)) {
            return false;
        }
        return Objects.equals(dataEntrada, outra.dataEntrada) && Objects.equals(dataSaida, outra.dataSaida);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataEntrada, dataSaida);
    }

    @Override
    public String toString() {
        return dataEntrada + " a " + dataSaida;
    }
}
