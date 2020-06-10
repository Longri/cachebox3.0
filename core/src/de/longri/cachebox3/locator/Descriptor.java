package de.longri.cachebox3.locator;


import de.longri.cachebox3.utils.MathUtils;

/**
 * has x,y,zoom for defining a tile
 * also a hashCode for quick identification (calculated in getter on the fly)
 */
public class Descriptor implements Comparable<Descriptor> {
    private static final int maxZoom = 25;
    private static final int[] tileOffset = new int[maxZoom];
    private static final int[] tilesPerLine = new int[maxZoom];
    private static final int[] tilesPerColumn = new int[maxZoom];

    static {
        tileOffset[0] = 0;
        for (int i = 0; i < maxZoom - 1; i++) {
            tilesPerLine[i] = (int) (2 * Math.pow(2, i));
            tilesPerColumn[i] = (int) Math.pow(2, i);
            tileOffset[i + 1] = tileOffset[i] + (tilesPerLine[i] * tilesPerColumn[i]);
        }
    }

    private Object data = null;
    private int x;
    private int y;
    private int zoom;
    private long hashCode;
    public Descriptor(int _x, int _y, int _zoom) {
        x = _x;
        y = _y;
        zoom = _zoom;
        hashCode = 0;
    }

    public Descriptor(double latitude, double longitude, int _zoom) {
        zoom = _zoom;
        x = (int) longitudeToTileX(longitude, zoom);
        y = (int) latitudeToTileY(latitude, zoom);
        hashCode = 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZoom() {
        return zoom;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Berechnet aus dem übergebenen Längengrad die X-Koordinate im OSM-Koordinatensystem der gewünschten Zoom-Stufe
     *
     * @param zoom      Zoom-Stufe, in der die Koordinaten ausgedrückt werden sollen
     * @param longitude Longitude
     * @return double
     */
    private double longitudeToTileX(double longitude, int zoom) {
        return (longitude + 180.0) / 360.0 * Math.pow(2, zoom);
    }

    /**
     * Berechnet aus dem übergebenen Breitengrad die Y-Koordinate im OSM-Koordinatensystem der gewünschten Zoom-Stufe
     *
     * @param zoom     Zoom-Stufe, in der die Koordinaten ausgedrückt werden sollen
     * @param latitude Latitude
     * @return double
     */
    private double latitudeToTileY(double latitude, int zoom) {
        double latRad = latitude * MathUtils.DEG_RAD;
        return (1 - Math.log(Math.tan(latRad) + (1.0 / Math.cos(latRad))) / Math.PI) / 2 * Math.pow(2, zoom);
    }

    /*
    private double latitudeToTileY(byte zoom, double latitude, int tileSize) {
        double sinLatitude = Math.sin(latitude * (Math.PI / 180));
        long mapSize = tileSize << zoom;
        // FIX ME improve this formula so that it works correctly without the clipping
        double pixelY = (0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI)) * mapSize;
        return Math.min(Math.max(0, pixelY), mapSize);
    }
     */

    /**
     * Berechnet aus der übergebenen OSM-X-Koordinate den entsprechenden Längengrad
     */
    private double tileXToLongitude(int _x) {
        return -180.0 + (360.0 * _x) / Math.pow(2, zoom);
    }

    /**
     * Berechnet aus der übergebenen OSM-Y-Koordinate den entsprechenden Breitengrad
     */
    private double tileYToLatitude(int _y) {
        double exp = Math.exp(4 * Math.PI * Math.pow(2, -zoom) * _y);
        double xNom = Math.exp(2 * Math.PI) - exp;
        double xDen = Math.exp(2 * Math.PI) + exp;

        double v = -1 + Math.pow(2, 1 - zoom) * _y;
        double yNom = 2 * Math.exp(-Math.PI * v);
        double yDen = Math.exp(-2 * Math.PI * v) + 1;

        return Math.atan2(xNom / xDen, yNom / yDen) * MathUtils.RAD_DEG;
    }

    public long getHashCode() {
        if (hashCode != 0)
            return hashCode;
        hashCode = ((tileOffset[zoom]) + (long) (tilesPerLine[zoom]) * y + x);
        return hashCode;
    }

    public String toString() {
        return "X = " + x + ", Y = " + y + ", Zoom = " + zoom;
    }

    @Override
    public int compareTo(Descriptor another) {
        return Long.compare(getHashCode(), another.getHashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Descriptor) {
            return getHashCode() == ((Descriptor) obj).getHashCode();
        }
        return false;
    }

    /**
     * Return the center coordinate of this Descriptor
     *
     * @return ?
     */
    public Coordinate getCenterCoordinate() {
        double lon = tileXToLongitude(x);
        double lat = tileYToLatitude(y);

        double lon1 = tileXToLongitude(x + 1);
        double lat1 = tileYToLatitude(y + 1);

        return new Coordinate((lat + lat1) / 2 , (lon + lon1) / 2);
    }

    /**
     * Returns a pseudo distance to the given Descriptor!
     *
     * @param descriptor ?
     * @return ?
     */
    public int getDistance(Descriptor descriptor) {
        int xDistance = Math.abs(descriptor.x - x);
        int yDistance = Math.abs(descriptor.y - y);
        return Math.max(xDistance, yDistance);
    }
}
