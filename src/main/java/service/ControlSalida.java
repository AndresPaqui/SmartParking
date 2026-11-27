package main.java.service;

import main.java.model.ConfiguracionSistema;
import main.java.model.EstadoTicket;
import main.java.model.RegistroSalida;
import main.java.model.ResultadoSalida;
import main.java.model.TipoSalida;
import main.java.model.Ticket;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;-
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlSalida {
    private final List<RegistroSalida> registroSalidas = new ArrayList<>();
    private final HashMap<String, Ticket> ticketsActivos = new HashMap<>();
    private final HashMap<String, String> idPorCodigoQR = new HashMap<>();
    private final HashMap<String, String> idPorPlaca = new HashMap<>();
    private final ArrayList<Ticket> historial = new ArrayList<>();
    private final ConfiguracionSistema configuracion;

    public ControlSalida(List<Ticket> ticketsIniciales, ConfiguracionSistema configuracion) {
        if (ticketsIniciales == null) {
            throw new IllegalArgumentException("La lista de tickets iniciales no puede ser nula.");
        }
        if (configuracion == null) {
            throw new IllegalArgumentException("La configuración del sistema no puede ser nula.");
        }
        this.configuracion = configuracion;
        for (Ticket ticket : ticketsIniciales) {
            registrarTicket(ticket);
        }
    }

    // Busca por QR o por placa y devuelve el ticket activo (o null)
    public synchronized Ticket buscarTicketActivo(String codigoQROPlaca) {
        if (codigoQROPlaca == null) {
            return null;
        }
        String identificador = codigoQROPlaca.trim();
        String id = idPorCodigoQR.get(identificador);
        if (id == null) {
            id = idPorPlaca.get(identificador);
        }
        return id == null ? null : ticketsActivos.get(id);
    }

    public synchronized List<RegistroSalida> getRegistroSalidas() {
        return new ArrayList<>(registroSalidas);
    }

    public synchronized List<Ticket> getTicketsActivos() {
        return new ArrayList<>(ticketsActivos.values());
    }

    // Valida que no haya ID, placa ni QR duplicados antes de guardar
    private void registrarTicket(Ticket ticket) {
        if (ticket == null || ticket.getId() == null || ticket.getId().isBlank()) {
            throw new IllegalArgumentException("Cada ticket inicial debe tener un ID válido.");
        }
        if (ticketsActivos.containsKey(ticket.getId())) {
            throw new IllegalArgumentException("Hay más de un ticket inicial con el ID " + ticket.getId() + ".");
        }
        if (ticket.getPlaca() != null && idPorPlaca.containsKey(ticket.getPlaca())) {
            throw new IllegalArgumentException("Hay más de un ticket inicial con la placa " + ticket.getPlaca() + ".");
        }
        if (ticket.getCodigoQR() != null && idPorCodigoQR.containsKey(ticket.getCodigoQR())) {
            throw new IllegalArgumentException("Hay más de un ticket inicial con el mismo código QR.");
        }
        ticketsActivos.put(ticket.getId(), ticket);
        if (ticket.getPlaca() != null) {
            idPorPlaca.put(ticket.getPlaca(), ticket.getId());
        }
        if (ticket.getCodigoQR() != null) {
            idPorCodigoQR.put(ticket.getCodigoQR(), ticket.getId());
        }
    }
}
