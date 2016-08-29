package de.longri.cachebox3.types;

/**
 * Class with Cache and WP as return Type
 * 
 * @author Longri
 */
public class CacheWithWP {
	private Cache cache;
	private Waypoint waypoint;

	public CacheWithWP(Cache Cache, Waypoint waypoint) {
		this.cache = Cache;
		this.waypoint = waypoint;
	}

	public void dispose() {
		this.cache = null;
		this.waypoint = null;
	}

	public Cache getCache() {
		return this.cache;
	}

	public Waypoint getWaypoint() {
		return this.waypoint;
	}
}
