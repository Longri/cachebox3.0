package de.longri.cachebox3.types;

/**
 * Class with Cache and WP as return Type
 * 
 * @author Longri
 */
public class CacheWithWP {
	private AbstractCache abstractCache;
	private AbstractWaypoint waypoint;

	public CacheWithWP(AbstractCache cache, AbstractWaypoint waypoint) {
		this.abstractCache = cache;
		this.waypoint = waypoint;
	}

	public void dispose() {
		this.abstractCache = null;
		this.waypoint = null;
	}

	public AbstractCache getCache() {
		return this.abstractCache;
	}

	public AbstractWaypoint getWaypoint() {
		return this.waypoint;
	}
}
