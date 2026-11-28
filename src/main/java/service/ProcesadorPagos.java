package main.java.service;

import main.java.model.ConfiguracionSistema;
import main.java.model.EstadoPago;
import main.java.model.EstadoTicket;
import main.java.model.Pago;
import main.java.model.Ticket;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class ProcesadorPagos {
    private static final int MAX_INTENTOS = 3;
    private static final EstadoPago[] FALLOS = {
            EstadoPago.RECHAZADO, EstadoPago.TIEMPO_AGOTADO, EstadoPago.QR_NO_RECONOCIDO
    };

    private final Queue<Pago> colaPagos = new ArrayDeque<>();
    private final List<Pago> historialPagos = new ArrayList<>();
    private final Set<String> ticketsEnCola = new HashSet<>();

    private final GestorTickets tickets;
    private final CalculadoraTarifa calculadora;
    private final ConfiguracionSistema config;
    private final Random random;
    private int contadorPagos = 0;

    public ProcesadorPagos(GestorTickets tickets, CalculadoraTarifa calculadora, ConfiguracionSistema config) {
        this(tickets, calculadora, config, new Random());
    }


    public ProcesadorPagos(GestorTickets tickets, CalculadoraTarifa calculadora,
                           ConfiguracionSistema config, Random random) {
        if (tickets == null || calculadora == null || config == null || random == null) {
            throw new IllegalArgumentException("GestorTickets, CalculadoraTarifa, configuración y Random son obligatorios.");
        }
        this.tickets = tickets;
        this.calculadora = calculadora;
        this.config = config;
        this.random = random;
    }


    public synchronized Pago solicitarPago(String codigoQR) {
        if (codigoQR == null || codigoQR.isBlank()) {
            throw new IllegalArgumentException("Ingrese un código QR para solicitar el pago.");
        }
        String qr = codigoQR.trim();
        Ticket ticket = tickets.buscarPorCodigoQR(qr);
        if (ticket == null) {
            throw new IllegalArgumentException("No existe un ticket activo con el código QR " + qr + ".");
        }
        if (ticket.getEstado() != EstadoTicket.ACTIVO) {
            throw new IllegalStateException("El ticket " + ticket.getId() + " no está activo (estado: "
                    + ticket.getEstado() + "), no requiere pago.");
        }
        if (ticketsEnCola.contains(ticket.getId())) {
            throw new IllegalStateException("El ticket " + ticket.getId() + " ya tiene un pago pendiente en la cola.");
        }

        double monto = calculadora.calcularMonto(ticket, LocalDateTime.now());
        Pago pago = new Pago(String.format("PAG-%04d", ++contadorPagos), ticket, monto);
        colaPagos.offer(pago);
        ticketsEnCola.add(ticket.getId());
        return pago;
    }

    public synchronized Pago procesarSiguiente() {
        Pago pago = colaPagos.poll();
        if (pago == null) {
            return null;
        }
        // El ticket pudo cerrarse mientras esperaba en cola (p. ej. salida manual)
        if (pago.getTicket().getEstado() != EstadoTicket.ACTIVO) {
            pago.setEstado(EstadoPago.RECHAZADO);
            cerrarPago(pago);
            return pago;
        }

        EstadoPago resultado = simularResultado();
        pago.incrementarIntento();
        pago.setEstado(resultado);

        if (resultado == EstadoPago.APROBADO) {
            confirmarPago(pago);
            cerrarPago(pago);
        } else if (pago.getIntentos() < MAX_INTENTOS) {
            colaPagos.offer(pago); // reintento: vuelve al final de la cola
        } else {
            cerrarPago(pago); // fallo definitivo
        }
        return pago;
    }

    public EstadoPago simularResultado() {
        if (random.nextDouble() < config.getProbabilidadFallo()) {
            return FALLOS[random.nextInt(FALLOS.length)];
        }
        return EstadoPago.APROBADO;
    }


    public synchronized void confirmarPago(Pago p) {
        if (p == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo.");
        }
        Ticket ticket = p.getTicket();
        if (ticket.getEstado() != EstadoTicket.ACTIVO) {
            throw new IllegalStateException("El ticket " + ticket.getId() + " ya no está activo; no se puede confirmar el pago.");
        }
        LocalDateTime ahora = LocalDateTime.now();
        ticket.setEstado(EstadoTicket.PAGADO);
        ticket.setHoraPago(ahora);
        ticket.setMonto(p.getMonto());
        p.setEstado(EstadoPago.APROBADO);
        p.setHoraConfirmacion(ahora);
    }

    public synchronized int getLongitudCola() {
        return colaPagos.size();
    }


    public synchronized double calcularEsperaPromedio() {
        if (colaPagos.isEmpty()) {
            return 0.0;
        }
        LocalDateTime ahora = LocalDateTime.now();
        double totalSegundos = 0;
        for (Pago pago : colaPagos) {
            totalSegundos += Duration.between(pago.getHoraSolicitud(), ahora).toMillis() / 1000.0;
        }
        return totalSegundos / colaPagos.size();
    }


    public synchronized List<Pago> getColaPendiente() {
        return new ArrayList<>(colaPagos);
    }

    public synchronized List<Pago> getHistorialPagos() {
        return new ArrayList<>(historialPagos);
    }

    public int getMaxIntentos() {
        return MAX_INTENTOS;
    }

    private void cerrarPago(Pago pago) {
        historialPagos.add(pago);
        ticketsEnCola.remove(pago.getTicket().getId());
    }
