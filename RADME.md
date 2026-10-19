# 🚗 smartParking

**smartParking** es un sistema ciberfísico de gestión de recaudo y control de parqueaderos universitarios, diseñado para integrarse como un aditivo tecnológico a las infraestructuras de barreras físicas existentes.

## ⚠️ El Problema
Actualmente, el sistema de parqueaderos universitarios sufre de cuellos de botella masivos durante las horas pico. El proceso de pago es presencial, centralizado y secuencial (los usuarios deben caminar hacia una máquina o ventanilla física para pagar su ticket).

Esto genera largas filas peatonales que exceden el "tiempo de gracia" para salir del parqueadero. Como consecuencia:
- Los usuarios reciben recargos automáticos injustificados (pago de horas extra).
- Se genera una severa congestión vehicular (tráfico) en los carriles de salida y vías urbanas aledañas.
- Ocurren fallos mecánicos frecuentes en las máquinas lectoras de efectivo.

## 💡 La Solución
Implementar un ecosistema de *smart parking* descentralizado compuesto por un backend en **Java** y una **Web App responsiva**. La solución permite a los conductores pagar su tarifa de forma remota desde su dispositivo móvil sin necesidad de instalar aplicaciones nativas.

El sistema utiliza estructuras de datos avanzadas (HashMaps, Queues, Red-Black Trees, PriorityQueues) y algoritmos para procesar pagos de forma concurrente, detectar "autos fantasma", emparejar matrículas en tiempo real para agilizar la salida y generar analítica de ocupación.

## 👥 Equipo de Desarrollo y Módulos Funcionales

El proyecto está dividido en 6 módulos algorítmicos. Cada integrante es responsable del desarrollo técnico de un módulo específico y de sus estructuras de datos subyacentes:

| Integrante | Rol / Responsabilidad | Módulo Asignado |
| :--- | :--- | :--- |
| **Kevin Quinatoa** | Desarrollador Backend | **Módulo 1:** Gestión y Procesamiento Estructural de Tickets (`HashMap` y Lógica Tarifaria). |
| **Jostin Saca** | Desarrollador Backend | **Módulo 2:** Encolamiento y Concurrencia de Pagos (`ConcurrentQueue` para pasarelas). |
| **Rafaela Ludeña** | Desarrollador Backend | **Módulo 3:** Gestión de Matrículas y Control de Salidas (`TreeSet` para autocompletado rápido). |
| **Valeria Caceres** | Desarrollador Backend | **Módulo 4:** Detección Algorítmica de Anomalías (`PriorityQueue` / `BST` para tickets fantasma). |
| **Steven Vaca** | Desarrollador Backend | **Módulo 5:** Analítica de Ocupación y Ordenamiento (`MergeSort` / `QuickSort` para reportes). |
| **Andrés Paqui** | Líder Integración / Frontend | **Módulo 6:** Enrutamiento de Eventos y Caché de Interfaz (`Stack LIFO` y `LinkedHashMap` - LRU Caché). |

## 📂 Estructura Básica de Carpetas

El proyecto sigue una arquitectura MVC (Modelo-Vista-Controlador) adaptada para el desarrollo de la API REST en Java:

```text
smartParking/
│
├── README.md                   # Documentación principal del proyecto
├── .gitignore                  # Archivos ignorados por el control de versiones
│
└── src/
    └── main/
        ├── java/
        │   └── controller/   (Puntos de entrada del sistema)
        │   └── model/        (Clases de dominio: Ticket, Vehiculo, etc.)
        │   └── repository/   (Estructuras de datos en memoria: TreeSet, HashMaps)
        │   └── service/      (Lógica de negocio y algoritmos de ordenamiento/búsqueda)
        │   └── Main.java     (Clase de arranque del sistema)