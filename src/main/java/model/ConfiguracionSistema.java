package main.java.model;

public class ConfiguracionSistema {
    private double tarifaPorFraccion;
    private int minutosFraccion;
    private int minutosGracia;
    private int aforoMaximo;
    private int segundosConfirmacion;
    private double probabilidadFallo;
    private int umbralFantasmaHoras;

    public ConfiguracionSistema() {
        this.tarifaPorFraccion = 1.00;
        this.minutosFraccion = 60;
        this.minutosGracia = 15;
        this.aforoMaximo = 550;
        this.segundosConfirmacion = 2;
        this.probabilidadFallo = 0.25;
        this.umbralFantasmaHoras = 24;
    }

    public double getTarifaPorFraccion() {
        return tarifaPorFraccion;
    }

    public void setTarifaPorFraccion(double tarifaPorFraccion) {
        if (tarifaPorFraccion < 0) {
            throw new IllegalArgumentException("La tarifa por fracción no puede ser negativa.");
        }
        this.tarifaPorFraccion = tarifaPorFraccion;
    }

    public int getMinutosFraccion() {
        return minutosFraccion;
    }

    public void setMinutosFraccion(int minutosFraccion) {
        if (minutosFraccion <= 0) {
            throw new IllegalArgumentException("Los minutos por fracción deben ser mayores a cero.");
        }
        this.minutosFraccion = minutosFraccion;
    }

    public int getMinutosGracia() {
        return minutosGracia;
    }

    public void setMinutosGracia(int minutosGracia) {
        if (minutosGracia < 0) {
            throw new IllegalArgumentException("Los minutos de gracia no pueden ser negativos.");
        }
        this.minutosGracia = minutosGracia;
    }

    public int getAforoMaximo() {
        return aforoMaximo;
    }

    public void setAforoMaximo(int aforoMaximo) {
        if (aforoMaximo <= 0) {
            throw new IllegalArgumentException("El aforo máximo debe ser mayor a cero.");
        }
        this.aforoMaximo = aforoMaximo;
    }

    public int getSegundosConfirmacion() {
        return segundosConfirmacion;
    }

    public void setSegundosConfirmacion(int segundosConfirmacion) {
        if (segundosConfirmacion < 0) {
            throw new IllegalArgumentException("Los segundos de confirmación no pueden ser negativos.");
        }
        this.segundosConfirmacion = segundosConfirmacion;
    }

    public double getProbabilidadFallo() {
        return probabilidadFallo;
    }

    public void setProbabilidadFallo(double probabilidadFallo) {
        if (probabilidadFallo < 0.0 || probabilidadFallo > 1.0) {
            throw new IllegalArgumentException("La probabilidad de fallo debe estar entre 0.0 y 1.0.");
        }
        this.probabilidadFallo = probabilidadFallo;
    }

    public int getUmbralFantasmaHoras() {
        return umbralFantasmaHoras;
    }

    public void setUmbralFantasmaHoras(int umbralFantasmaHoras) {
        if (umbralFantasmaHoras <= 0) {
            throw new IllegalArgumentException("El umbral de tickets fantasma debe ser mayor a cero.");
        }
        this.umbralFantasmaHoras = umbralFantasmaHoras;
    }
}