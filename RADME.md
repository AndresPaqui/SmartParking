# Smart Parking

Aplicación de escritorio en Java (Swing) para la simulación y gestión operativa de un parqueadero universitario. El sistema modela el ciclo de vida de los tickets de estacionamiento en memoria, el pago simulado mediante código QR y el control de salidas con validación de tiempo de gracia.

## Contexto y problema

El parqueadero universitario cuenta con un aforo de 550 espacios y dispone únicamente de 2 máquinas físicas de pago. Durante las horas pico (mediodía y de 17:40 a 19:00), la concentración de usuarios genera colas de entre 3 y 15 minutos, a lo que se suman fallos en la lectura de tickets o pagos con tarjeta.

Esta saturación provoca tres inconvenientes principales:
- **Vencimiento de la ventana de gracia:** Una vez realizado el pago, el usuario tiene 15 minutos para retirar su vehículo. Las demoras en fila o el traslado hacia el auto hacen que se supere ese límite, generando un recargo automático de $1.00 (una hora adicional).
- **Congestión vehicular:** La lenta rotación en los puntos de pago retrasa la salida en los carriles internos.
- **Tickets fantasma:** Cuando una salida ya pagada no queda cerrada en el sistema, el ticket permanece activo y acumula horas de forma indebida.

## Solución y alcance técnico

El proyecto implementa una solución académica en Java con interfaz gráfica Swing y persistencia temporal en memoria (sin bases de datos externas ni pasarelas de pago reales). Los datos iniciales se cargan al iniciar la ejecución para simular distintos escenarios operativos.

Estructuras de datos y algoritmos utilizados:
- `HashMap`: indexación y búsqueda en $O(1)$ promedio de tickets activos por ID, placa y código QR.
- `ArrayDeque` (Cola FIFO): gestión de solicitudes de pago por QR en orden de llegada y re-encolado ante fallos simulados.
- `PriorityQueue`: agenda de eventos discretos en el simulador y priorización de tickets antiguos para detección de anomalías.
- `ArrayList`: almacenamiento secuencial de historiales de tickets retirados, pagos confirmados, registros de salida y auditoría.
- `Merge Sort`: ordenamiento de reportes por monto, tiempo de permanencia y fecha.

## Arquitectura modular y equipo

El sistema se divide en 6 módulos funcionales:

| Módulo | Descripción | Estructuras / Algoritmos | Integrante asignado |
| :--- | :--- | :--- | :--- |
| **M1. Entrada y tickets** | Registro de ingreso, generación de código QR y control de aforo (550 espacios). | `HashMap`, `ArrayList` | Kevin Quinatoa |
| **M2. Tarifa y parámetros** | Configuración de reglas del sistema ($1/h, 15 min gracia) y cálculo de cobros. | Aritmética con redondeo superior | Jostin Saca |
| **M3. Pago en línea por QR** | Encolamiento de pagos, simulación de fallos y reintentos (máximo 3). | `Queue` (`ArrayDeque`), `ArrayList` | Rafaela Ludeña |
| **M4. Control de salida** | Validación de pago y gracia, aplicación de recargos, cierre de ticket y salida manual. | `HashMap`, `ArrayList` | Valeria Caceres |
| **M5. Simulador de flujo** | Generación de llegadas y salidas mediante eventos discretos con tasas $\lambda$ y $\mu$. | `PriorityQueue` | Steven Vaca |
| **M6. Reportes y alertas** | Ordenamiento de historiales, corte de caja y detección de tickets rezagados (> 24 h). | `Merge Sort`, `PriorityQueue`, `ArrayList` | Andrés Paqui |

> **Estado actual (Progreso 2):** Implementación funcional de los módulos **M3 (Pago por QR)** y **M4 (Control de salida)** junto con la capa base de datos en memoria de **M1** y **M2**.

## Estructura del proyecto

```text
smart-parking/
├── README.md
├── .gitignore
└── src/
    └── main/
        └── java/
            ├── data/       # Carga de datos iniciales en memoria (DatosIniciales.java)
            ├── model/      # Entidades y enumeraciones (Ticket, Pago, RegistroSalida, ConfiguracionSistema)
            ├── service/    # Lógica de negocio y estructuras (GestorTickets, CalculadoraTarifa, ProcesadorPagos, ControlSalida)
            └── ui/         # Interfaz gráfica en Java Swing

```

## Requisitos e instrucciones de ejecución

**Requisitos**
- `Java Development Kit (JDK) 17 o superior.`
- `IDE compatible con proyectos Java (IntelliJ IDEA).`

**Ejecución desde IDE**

1. Clonar repositorio
 ```text
git clone <URL_DEL_REPOSITORIO>
```
2. Abrir la carpeta del proyecto en el IDE y verificar que la carpeta src/main/java esté marcada como directorio de código fuente (Sources Root).
3. 