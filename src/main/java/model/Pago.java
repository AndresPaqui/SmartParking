package main.java.model;

import java.time.LocalDateTime;

public class Pago {
    private String idPago;
    private Ticket ticket;
    private double monto;
    private EstadoPago estado;
    private int intentos;
    private LocalDateTime horaSolicitud;
    private LocalDateTime horaConfirmacion;

    public Pago(String idPago, Ticket ticket, double monto) {
        this.idPago = idPago;
        this.ticket = ticket;
        this.monto = monto;
        this.estado = EstadoPago.PENDIENTE;
        this.intentos = 0;
        this.horaSolicitud = LocalDateTime.now();
    }

    public void incrementarIntento() {
        this.intentos++;
    }

    public String getIdPago() {
        return idPago;
    }

    public void setIdPago(String idPago) {
        this.idPago = idPago;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public int getIntentos() {
        return intentos;
    }

    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }

    public LocalDateTime getHoraSolicitud() {
        return horaSolicitud;
    }

    public void setHoraSolicitud(LocalDateTime horaSolicitud) {
        this.horaSolicitud = horaSolicitud;
    }

    public LocalDateTime getHoraConfirmacion() {
        return horaConfirmacion;
    }

    public void setHoraConfirmacion(LocalDateTime horaConfirmacion) {
        this.horaConfirmacion = horaConfirmacion;
    }
}
