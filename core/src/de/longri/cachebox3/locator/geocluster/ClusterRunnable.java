package de.longri.cachebox3.locator.geocluster;


import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;

/**
 * Created by Longri on 21.12.16.
 */
public class ClusterRunnable implements Runnable {

    final static Logger log = LoggerFactory.getLogger(ClusterRunnable.class);

    protected final double distance;
    private final CallBack callBack;
    private final ClusteredList workList;
    private volatile boolean running = true;

    public interface CallBack {
        public void callBack(ClusteredList reduced);
    }

    public ClusterRunnable(double distance, final ClusteredList workList, final CallBack callBack) {
        this.callBack = callBack;
        this.distance = distance;
        this.workList = workList;
    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {

        log.debug("Runnable started");

        try {
            workList.clusterByDistance(distance);
            log.debug("callback with reduced to " + workList.size());
            callBack.callBack(workList);
        } catch (Exception e) {
            running = false;
            log.error("Runnable exception", e);
        }
    }
}
