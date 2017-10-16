package de.longri.cachebox3.types;

/**
 * Class with Cache and WP as return Type
 * 
 * @author Longri
 */
public class CacheWithWP {
	private AbstractCache abstractCache;
	private Waypoint waypoint;

	public CacheWithWP(AbstractCache AbstractCache, Waypoint waypoint) {
		this.abstractCache = AbstractCache;
		this.waypoint = waypoint;
	}

	public void dispose() {
		this.abstractCache = null;
		this.waypoint = null;
	}

	public AbstractCache getCache() {
		return this.abstractCache;
	}

	public Waypoint getWaypoint() {
		return this.waypoint;
	}
}
