# Guía Técnica y Contexto del Proyecto: Smart Parking (Progreso 2)

Este documento sirve como referencia técnica y contexto directo para el desarrollo de los **Módulos 3 y 4** del sistema **Smart Parking (UDLA - Programación III)**. Si vas a utilizar un asistente de IA (Gemini, Claude, ChatGPT, etc.) para apoyarte en la codificación, **pégale este archivo completo junto con los archivos Java indicados en la Sección 5**.

---

## 1. Reglas de Arquitectura y Restricciones Globales
* **Lenguaje e Interfaz:** Java estándar con interfaz gráfica en **Java Swing** (`javax.swing`). No usar frameworks web, Spring Boot ni bases de datos externas.
* **Persistencia:** 100% en memoria volátil utilizando estructuras de datos del *Java Collections Framework* (**Solución A**).
* **Consistencia UML:** Todos los nombres de paquetes, clases, enumeraciones, atributos y firmas de métodos deben respetar estrictamente el diagrama de clases del proyecto.
* **Control de Versiones (Git):** Cada integrante debe trabajar en su propia rama (ej. `feature/modulo-3-pagos`, `feature/modulo-4-salida`) y realizar **commits incrementales** que evidencien el avance real. Está penalizado subir todo en un único commit final.

---

## 2. Estado Actual del Proyecto (Capa `model` Implementada)

El paquete `main.java.model` ya está construido y compilando. **No modifiques estas clases sin avisar al equipo**, ya que son el contrato compartido entre todos los módulos.

### Enumeraciones disponibles (`main.java.model`)
* `EstadoTicket`: `ACTIVO`, `PAGADO`, `SALIDO`, `CERRADO_FORZOSO`
* `EstadoPago`: `PENDIENTE`, `APROBADO`, `RECHAZADO`, `TIEMPO_AGOTADO`, `QR_NO_RECONOCIDO`
* `ResultadoSalida`: `AUTORIZADA`, `TICKET_NO_ENCONTRADO`, `NO_PAGADO`, `GRACIA_VENCIDA`
* `TipoSalida`: `NORMAL`, `MANUAL`

### Clases de Dominio disponibles (`main.java.model`)
1. **`Ticket`**
   * **Atributos:** `id` (`String`), `placa` (`String`), `codigoQR` (`String`), `horaIngreso` (`LocalDateTime`), `horaPago` (`LocalDateTime`), `horaSalida` (`LocalDateTime`), `estado` (`EstadoTicket`), `monto` (`double`).
   * **Constructor:** `public Ticket(String id, String placa, LocalDateTime horaIngreso)` (inicializa `estado = EstadoTicket.ACTIVO` y `monto = 0.0`).
   * **Métodos:** Getters y setters estándar para todos sus atributos.
2. **`ConfiguracionSistema`**
   * **Atributos (con valores por defecto cargados):** `tarifaPorFraccion` (`1.00`), `minutosFraccion` (`60`), `minutosGracia` (`15`), `aforoMaximo` (`550`), `segundosConfirmacion` (`2`), `probabilidadFallo` (`0.25`), `umbralFantasmaHoras` (`24`).
   * **Métodos:** Getters y setters validados (lanzan `IllegalArgumentException` ante valores inválidos).
3. **`Pago`**
   * **Atributos:** `idPago` (`String`), `ticket` (`Ticket`), `monto` (`double`), `estado` (`EstadoPago`), `intentos` (`int`), `horaSolicitud` (`LocalDateTime`), `horaConfirmacion` (`LocalDateTime`).
   * **Constructor:** `public Pago(String idPago, Ticket ticket, double monto)` (inicializa `estado = EstadoPago.PENDIENTE`, `intentos = 0`, `horaSolicitud = LocalDateTime.now()`).
   * **Métodos:** `incrementarIntento()` + getters y setters estándar.
4. **`RegistroSalida`**
   * **Atributos:** `idTicket` (`String`), `hora` (`LocalDateTime`), `tipo` (`TipoSalida`), `operador` (`String`), `motivo` (`String`).
   * **Constructor:** `public RegistroSalida(String idTicket, LocalDateTime hora, TipoSalida tipo, String operador, String motivo)`.
   * **Métodos:** Getters y setters estándar.

---

## 3. Servicios Base de Soporte (M1 y M2) que Consumirán M3 y M4

En el paquete `main.java.model`, los módulos 3 y 4 dependen de las siguientes operaciones de soporte:

* **`GestorTickets` (M1):**
  * `buscarPorId(String id): Ticket` — Búsqueda en $O(1)$ usando `HashMap<String, Ticket>`.
  * `buscarPorPlaca(String placa): Ticket` — Búsqueda en $O(1)$ usando `HashMap<String, String>`.
  * `buscarPorCodigoQR(String codigoQR): Ticket` — Búsqueda en $O(1)$ usando `HashMap<String, String>`.
  * `retirarTicket(String id): void` — Elimina el ticket de los mapas activos ($O(1)$) y lo mueve al `ArrayList<Ticket> historial`.
* **`CalculadoraTarifa` (M2):**
  * `calcularMinutosPermanencia(Ticket t, LocalDateTime ahora): long`
  * `calcularMonto(Ticket t, LocalDateTime ahora): double` — Aplica redondeo hacia arriba (`Math.ceil`) por fracción de hora.
  * `estaEnGracia(Ticket t, LocalDateTime ahora): boolean` — Verifica si `Duration.between(t.getHoraPago(), ahora).toMinutes() <= config.getMinutosGracia()`.
  * `aplicarRecargo(Ticket t): double` — Suma una fracción de tarifa (`config.getTarifaPorFraccion()`) al monto del ticket cuando vence la gracia.

---

## 4. Especificaciones de Implementación por Módulo

### Tarea A: Módulo 3 — Pago en línea simulado por QR
* **Archivo a crear/modificar:** `src/main/java/service/ProcesadorPagos.java`
* **Interfaz Swing asociada:** Panel/pestaña de Pagos QR en `src/main/java/ui/`.
* **Estructuras de datos obligatorias:**
  * `private Queue<Pago> colaPagos = new ArrayDeque<>();` (Cola FIFO para pagos pendientes de confirmación, operaciones `offer()` y `poll()` en $O(1)$).
  * `private List<Pago> historialPagos = new ArrayList<>();` (Registro de pagos aprobados o definitivamente fallidos).
  * `private static final int MAX_INTENTOS = 3;`
* **Métodos obligatorios y reglas de negocio:**
  1. `public Pago solicitarPago(String codigoQR)`:
     * Valida que el QR no sea nulo/vacío y busca el ticket en `GestorTickets` ($O(1)$).
     * Si el ticket no existe o su estado no es `EstadoTicket.ACTIVO`, lanza `IllegalArgumentException` o `IllegalStateException`.
     * Calcula el monto actual usando `CalculadoraTarifa.calcularMonto(ticket, LocalDateTime.now())`.
     * Crea el objeto `Pago` con un ID único (ej. `PAG-0001`), lo inserta al final de `colaPagos` (`offer`) y lo retorna.
  2. `public Pago procesarSiguiente()`:
     * Extrae el pago del frente de `colaPagos` (`poll()`). Si la cola está vacía, retorna `null`.
     * Llama a `simularResultado()` e incrementa el contador de intentos (`pago.incrementarIntento()`).
     * **Si el resultado es `EstadoPago.APROBADO`:** llama a `confirmarPago(pago)` y lo agrega a `historialPagos`.
     * **Si el resultado es un fallo (`RECHAZADO`, `TIEMPO_AGOTADO`, `QR_NO_RECONOCIDO`):** verifica si `pago.getIntentos() < MAX_INTENTOS`. Si aún tiene intentos, **vuelve a encolarlo al final de `colaPagos` (`offer`)** para reintento; si alcanzó los 3 intentos, lo mueve a `historialPagos` como fallido definitivo.
  3. `public EstadoPago simularResultado()`:
     * Usa `config.getProbabilidadFallo()` para determinar aleatoriamente si aprueba (`APROBADO`) o falla con alguno de los estados de error (`RECHAZADO`, `TIEMPO_AGOTADO`, `QR_NO_RECONOCIDO`).
  4. `public void confirmarPago(Pago p)`:
     * Actualiza el ticket asociado: `estado = EstadoTicket.PAGADO`, `horaPago = LocalDateTime.now()`, `monto = p.getMonto()`.
     * Fija `p.setHoraConfirmacion(LocalDateTime.now())`.
  5. `public int getLongitudCola()`, `public double calcularEsperaPromedio()`, `public List<Pago> getColaPendiente()` (para renderizar en `JTable`), `public List<Pago> getHistorialPagos()`.

---

### Tarea B: Módulo 4 — Control de salida reforzado
* **Archivo a crear/modificar:** `src/main/java/service/ControlSalida.java`
* **Interfaz Swing asociada:** Panel/pestaña de Control de Salida en `src/main/java/ui/`.
* **Estructuras de datos obligatorias:**
  * `private List<RegistroSalida> registroSalidas = new ArrayList<>();`
  * Consultas directas al `HashMap` de `GestorTickets` en $O(1)$.
* **Métodos obligatorios y reglas de negocio:**
  1. `public ResultadoSalida validarSalida(String codigoQROPlaca)`:
     * Busca el ticket activo por código QR o por placa en `GestorTickets` ($O(1)$).
     * Si no existe: retorna `ResultadoSalida.TICKET_NO_ENCONTRADO`.
     * Si existe pero `ticket.getEstado() != EstadoTicket.PAGADO`: retorna `ResultadoSalida.NO_PAGADO`.
     * Si está `PAGADO`, consulta `calculadora.estaEnGracia(ticket, LocalDateTime.now())`.
     * **Regla crítica (Gracia vencida):** Si han pasado más de 15 minutos desde `horaPago`, llama a `calculadora.aplicarRecargo(ticket)`, cambia `ticket.setEstado(EstadoTicket.ACTIVO)` para exigir un nuevo pago, y retorna `ResultadoSalida.GRACIA_VENCIDA`.
     * Si está dentro de los 15 minutos: retorna `ResultadoSalida.AUTORIZADA`.
  2. `public RegistroSalida autorizarSalida(Ticket t)`:
     * Ejecuta el cierre atómico: fija `t.setHoraSalida(LocalDateTime.now())`, cambia `t.setEstado(EstadoTicket.SALIDO)` y llama a `tickets.retirarTicket(t.getId())` para eliminarlo de los activos.
     * Crea y guarda un `RegistroSalida` con `TipoSalida.NORMAL` en `registroSalidas` y lo retorna.
  3. `public RegistroSalida autorizarSalidaManual(String idTicket, String operador, String motivo)`:
     * Valida que `operador` y `motivo` no sean nulos ni estén en blanco (si lo están, lanza `IllegalArgumentException`).
     * Busca el ticket activo por ID; si no existe, lanza `IllegalArgumentException`.
     * Fija `horaSalida = LocalDateTime.now()`, cambia `estado = EstadoTicket.CERRADO_FORZOSO`, retira el ticket de `GestorTickets` y guarda el `RegistroSalida` con `TipoSalida.MANUAL`.
  4. `public boolean verificarCierre(String idTicket)`:
     * Retorna `true` si `tickets.buscarPorId(idTicket) == null` (confirma que ya no quedó como ticket fantasma activo).
  5. `public List<RegistroSalida> getSalidasManuales()`, `public Map<TipoSalida, Integer> contarSalidasPorTipo()`, `public List<RegistroSalida> getRegistroSalidas()`.

---

## 5. Instrucciones para Trabajar con tu Asistente de IA

Cuando le pidas ayuda a Gemini, Claude o ChatGPT para generar tu módulo o su interfaz gráfica en Swing, **adjunta siempre estos archivos** para que la IA no invente nombres ni rompa la compilación:
1. Este archivo (`CONTEXTO_MODULOS_3_Y_4.md`).
2. Las clases del paquete `model/` (`Ticket.java`, `Pago.java`, `RegistroSalida.java`, `ConfiguracionSistema.java` y los 4 `enum`).
3. Las clases `GestorTickets.java` y `CalculadoraTarifa.java` del paquete `service/`.

### Prompt recomendado para copiar y pegar en tu IA:
> *"Actúa como un desarrollador senior en Java. Lee el archivo `CONTEXTO_MODULOS_3_Y_4.md` y las clases adjuntas del paquete `model` y `service`. Necesito implementar el **[Módulo 3: ProcesadorPagos / Módulo 4: ControlSalida]** y su panel correspondiente en **Java Swing** dentro del paquete `main.java.`. Respeta exactamente los nombres de clases, métodos, enums y estructuras de datos (`ArrayDeque` / `HashMap` / `ArrayList`) especificados en el contexto. Incluye manejo de excepciones con mensajes claros (`JOptionPane`) para entradas inválidas o estados incorrectos, y asegúrate de que las tablas (`JTable`) se actualicen automáticamente tras cada operación."*