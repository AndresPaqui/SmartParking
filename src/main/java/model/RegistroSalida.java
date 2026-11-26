package main.java.model;

import java.time.LocalDateTime;

public class RegistroSalida {
    private String idTicket;
    private LocalDateTime hora;
    private TipoSalida tipo;
    private String operador;
    private String motivo;

    public RegistroSalida(String idTicket, LocalDateTime hora, TipoSalida tipo, String operador, String motivo) {
        this.idTicket = idTicket;
        this.hora = hora;
        this.tipo = tipo;
        this.operador = operador;
        this.motivo = motivo;
    }

    public String getIdTicket() {
        return idTicket;
    }

    public void setIdTicket(String idTicket) {
        this.idTicket = idTicket;
    }

    public LocalDateTime getHora() {
        return hora;
    }

    public void setHora(LocalDateTime hora) {
        this.hora = hora;
    }

    public TipoSalida getTipo() {
        return tipo;
    }

    public void setTipo(TipoSalida tipo) {
        this.tipo = tipo;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
