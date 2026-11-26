package main.java.model;

import java.time.LocalDateTime;

public class Ticket {
    private String id;
    private String placa;
    private String codigoQR;
    private LocalDateTime horaIngreso;
    private LocalDateTime horaPago;
    private LocalDateTime horaSalida;
    private EstadoTicket estado;
    private double monto;

    public Ticket(String id, String placa, LocalDateTime horaIngreso) {
        this.id = id;
        this.placa = placa;
        this.horaIngreso = horaIngreso;
        this.estado = EstadoTicket.ACTIVO;
        this.monto = 0.0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getCodigoQR() {
        return codigoQR;
    }

    public void setCodigoQR(String codigoQR) {
        this.codigoQR = codigoQR;
    }

    public LocalDateTime getHoraIngreso() {
        return horaIngreso;
    }

    public void setHoraIngreso(LocalDateTime horaIngreso) {
        this.horaIngreso = horaIngreso;
    }

    public LocalDateTime getHoraPago() {
        return horaPago;
    }

    public void setHoraPago(LocalDateTime horaPago) {
        this.horaPago = horaPago;
    }

    public LocalDateTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalDateTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public EstadoTicket getEstado() {
        return estado;
    }

    public void setEstado(EstadoTicket estado) {
        this.estado = estado;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    @Override
    public String toString() {
        return id + " | " + placa + " | " + estado;
    }
}
