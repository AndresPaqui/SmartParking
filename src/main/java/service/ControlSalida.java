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
    // Revisa el estado del ticket sin dejar salir al vehículo todavía
    public synchronized ResultadoSalida validarSalida(String codigoQROPlaca) {
        if (codigoQROPlaca == null || codigoQROPlaca.isBlank()) {
            throw new IllegalArgumentException("Ingrese un código QR o una placa para validar la salida.");
        }

        Ticket ticket = buscarTicketActivo(codigoQROPlaca.trim());
        if (ticket == null) {
            return ResultadoSalida.TICKET_NO_ENCONTRADO;
        }
        if (ticket.getEstado() != EstadoTicket.PAGADO) {
            return ResultadoSalida.NO_PAGADO;
        }
        if (ticket.getHoraPago() == null) {
            throw new IllegalStateException("El ticket figura como pagado, pero no tiene registrada la hora de pago.");
        }
        if (!estaEnGracia(ticket, LocalDateTime.now())) {
            aplicarRecargo(ticket);
            ticket.setEstado(EstadoTicket.ACTIVO);
            return ResultadoSalida.GRACIA_VENCIDA;
        }
        return ResultadoSalida.AUTORIZADA;
    }

    // Indica si aún está dentro de los minutos de gracia desde el pago
    private boolean estaEnGracia(Ticket ticket, LocalDateTime ahora) {
        return Duration.between(ticket.getHoraPago(), ahora).toMinutes() <= configuracion.getMinutosGracia();
    }

    // Suma una fracción extra al monto cuando se vence la gracia
    private void aplicarRecargo(Ticket ticket) {
        ticket.setMonto(ticket.getMonto() + configuracion.getTarifaPorFraccion());
    }

    // Salida normal: el ticket debe estar pagado y dentro de la gracia
    public synchronized RegistroSalida autorizarSalida(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("El ticket no puede ser nulo.");
        }
        Ticket activo = ticketsActivos.get(ticket.getId());
        if (activo != ticket) {
            throw new IllegalStateException("El ticket ya no está activo en el sistema.");
        }
        if (ticket.getEstado() != EstadoTicket.PAGADO) {
            throw new IllegalStateException("No se puede autorizar la salida: el ticket no está pagado.");
        }
        if (ticket.getHoraPago() == null) {
            throw new IllegalStateException("No se puede autorizar la salida: falta la hora de pago.");
        }
        if (!estaEnGracia(ticket, LocalDateTime.now())) {
            aplicarRecargo(ticket);
            ticket.setEstado(EstadoTicket.ACTIVO);
            throw new IllegalStateException("El tiempo de gracia venció. Se aplicó el recargo y se requiere un nuevo pago.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        ticket.setHoraSalida(ahora);
        ticket.setEstado(EstadoTicket.SALIDO);
        retirarTicket(ticket);
        RegistroSalida registro = new RegistroSalida(ticket.getId(), ahora, TipoSalida.NORMAL, null, null);
        registroSalidas.add(registro);
        return registro;
    }

    // true si el ticket ya no está activo (cerrado correctamente)
    public synchronized boolean verificarCierre(String idTicket) {
        return idTicket == null || !ticketsActivos.containsKey(idTicket);
    }

    // Saca el ticket de los mapas activos y lo pasa al historial
    private void retirarTicket(Ticket ticket) {
        ticketsActivos.remove(ticket.getId());
        if (ticket.getPlaca() != null) {
            idPorPlaca.remove(ticket.getPlaca());
        }
        if (ticket.getCodigoQR() != null) {
            idPorCodigoQR.remove(ticket.getCodigoQR());
        }
        historial.add(ticket);
    }


    // Salida manual: exige operador y motivo para dejar constancia
    public synchronized RegistroSalida autorizarSalidaManual(String idTicket, String operador, String motivo) {
        if (operador == null || operador.isBlank()) {
            throw new IllegalArgumentException("Debe ingresar el nombre del operador para una salida manual.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe ingresar el motivo de la salida manual.");
        }
        if (idTicket == null || idTicket.isBlank()) {
            throw new IllegalArgumentException("Debe ingresar el ID del ticket para una salida manual.");
        }

        Ticket ticket = ticketsActivos.get(idTicket.trim());
        if (ticket == null) {
            throw new IllegalArgumentException("No existe un ticket activo con el ID " + idTicket.trim() + ".");
        }

        LocalDateTime ahora = LocalDateTime.now();
        ticket.setHoraSalida(ahora);
        ticket.setEstado(EstadoTicket.CERRADO_FORZOSO);
        retirarTicket(ticket);
        RegistroSalida registro = new RegistroSalida(ticket.getId(), ahora, TipoSalida.MANUAL,
                operador.trim(), motivo.trim());
        registroSalidas.add(registro);
        return registro;
    }

    public synchronized List<RegistroSalida> getSalidasManuales() {
        List<RegistroSalida> salidasManuales = new ArrayList<>();
        for (RegistroSalida registro : registroSalidas) {
            if (registro.getTipo() == TipoSalida.MANUAL) {
                salidasManuales.add(registro);
            }
        }
        return salidasManuales;
    }

    // Cuenta cuántas salidas hay de cada tipo (incluye los tipos con 0)
    public synchronized Map<TipoSalida, Integer> contarSalidasPorTipo() {
        Map<TipoSalida, Integer> conteo = new EnumMap<>(TipoSalida.class);
        for (TipoSalida tipo : TipoSalida.values()) {
            conteo.put(tipo, 0);
        }
        for (RegistroSalida registro : registroSalidas) {
            conteo.put(registro.getTipo(), conteo.get(registro.getTipo()) + 1);
        }
        return conteo;
    }
}

}
