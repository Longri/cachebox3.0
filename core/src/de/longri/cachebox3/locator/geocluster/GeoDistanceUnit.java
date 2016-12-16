package de.longri.cachebox3.locator.geocluster;

public enum GeoDistanceUnit {
    METERS(1000.0),
    MILES(1 / 1.60934),
    KILOMETERS(1.0);

    private final double factor;

    GeoDistanceUnit(double factor) {
        this.factor = factor;
    }

    public double fromKm(double val) {
        return val * factor;
    }

    public double toKm(double val) {
        return val / factor;
    }
}
