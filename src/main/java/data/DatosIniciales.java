package main.java.data;

import main.java.model.ConfiguracionSistema;
import main.java.model.EstadoTicket;
import main.java.model.Ticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatosIniciales {

    /**
     * Retorna la configuración base del parqueadero universitario:
     * $1.00 por fracción de 60 min, 15 min de gracia, aforo de 550 espacios.
     */
    public static ConfiguracionSistema cargarConfiguracion() {
        return new ConfiguracionSistema();
    }

    /**
     * Genera una lista inicial de 10 tickets en memoria que cubren todos los
     * escenarios de prueba para los Módulos 3 (Pagos QR) y 4 (Control de Salida).
     */
    public static List<Ticket> cargarTicketsIniciales() {
        List<Ticket> tickets = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        // --- ESCENARIO 1: Tickets ACTIVOS pendientes de pago (Para probar Módulo 3) ---

        // T-001: 25 min de permanencia (1 fracción = $1.00)
        Ticket t1 = crearTicket("T-001", "PBA-1234", ahora.minusMinutes(25));
        tickets.add(t1);

        // T-002: 75 min de permanencia (2 fracciones = $2.00)
        Ticket t2 = crearTicket("T-002", "PCK-8921", ahora.minusMinutes(75));
        tickets.add(t2);

        // T-003: 190 min de permanencia (4 fracciones = $4.00)
        Ticket t3 = crearTicket("T-003", "GTR-4510", ahora.minusMinutes(190));
        tickets.add(t3);

        // T-004: 10 min de permanencia (1 fracción = $1.00 - ideal para probar reintentos)
        Ticket t4 = crearTicket("T-004", "PDD-3312", ahora.minusMinutes(10));
        tickets.add(t4);

        // --- ESCENARIO 2: Tickets PAGADOS dentro de los 15 min de gracia (Salida Normal M4) ---

        // T-005: Ingresó hace 50 min, pagó hace 4 min ($1.00) -> Salida autorizada
        Ticket t5 = crearTicket("T-005", "PQA-7741", ahora.minusMinutes(50));
        t5.setEstado(EstadoTicket.PAGADO);
        t5.setHoraPago(ahora.minusMinutes(4));
        t5.setMonto(1.00);
        tickets.add(t5);

        // T-006: Ingresó hace 110 min, pagó hace 11 min ($2.00) -> Salida autorizada
        Ticket t6 = crearTicket("T-006", "PBC-9012", ahora.minusMinutes(110));
        t6.setEstado(EstadoTicket.PAGADO);
        t6.setHoraPago(ahora.minusMinutes(11));
        t6.setMonto(2.00);
        tickets.add(t6);

        // --- ESCENARIO 3: Tickets PAGADOS con gracia vencida > 15 min (Caso límite M4) ---

        // T-007: Ingresó hace 80 min, pagó hace 18 min -> Venció por 3 min (aplica recargo $1)
        Ticket t7 = crearTicket("T-007", "PDH-5520", ahora.minusMinutes(80));
        t7.setEstado(EstadoTicket.PAGADO);
        t7.setHoraPago(ahora.minusMinutes(18));
        t7.setMonto(1.00);
        tickets.add(t7);

        // T-008: Ingresó hace 140 min, pagó hace 30 min -> Venció por 15 min (aplica recargo $1)
        Ticket t8 = crearTicket("T-008", "PAA-1029", ahora.minusMinutes(140));
        t8.setEstado(EstadoTicket.PAGADO);
        t8.setHoraPago(ahora.minusMinutes(30));
        t8.setMonto(2.00);
        tickets.add(t8);

        // --- ESCENARIO 4: Tickets rezagados / fantasma > 24 horas (Salida Manual M4) ---

        // T-009: Ingresó hace 27 horas (caso real documentado en Progreso 1)
        Ticket t9 = crearTicket("T-009", "PXY-9988", ahora.minusHours(27));
        tickets.add(t9);

        // T-010: Ingresó hace 36 horas sin registro de salida
        Ticket t10 = crearTicket("T-010", "PZZ-0011", ahora.minusHours(36));
        tickets.add(t10);

        return tickets;
    }

    private static Ticket crearTicket(String id, String placa, LocalDateTime horaIngreso) {
        Ticket ticket = new Ticket(id, placa, horaIngreso);
        // Formato definido en UML: PQ-000123-ABC1234
        String numeroFormateado = String.format("%06d", Integer.parseInt(id.replace("T-", "")));
        String placaLimpia = placa.replace("-", "");
        ticket.setCodigoQR("PQ-" + numeroFormateado + "-" + placaLimpia);
        return ticket;
    }
}
