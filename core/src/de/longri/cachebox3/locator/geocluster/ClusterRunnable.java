package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.gui.map.layer.WaypointLayer;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Longri on 21.12.16.
 */
public class ClusterRunnable implements Runnable {

    final static org.slf4j.Logger log = LoggerFactory.getLogger(ClusterRunnable.class);

    protected final double factor;
    private final CallBack callBack;
    private final List<GeoCluster> workList;
    private volatile boolean running = true;

    public interface CallBack {
        public void callBack(List<GeoCluster> reduced);
    }

    public ClusterRunnable(double factor,final List<GeoCluster> workList, final CallBack callBack) {
        this.callBack = callBack;
        this.factor = factor;
        this.workList=workList;
    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {

        try {
            double maxDistance = this.factor * 5;
            FastGeoBoundingBoxContains boundingBoxContains = new FastGeoBoundingBoxContains(maxDistance);
            List<GeoCluster> reduced = new LinkedList<GeoCluster>();
            reduced.addAll(workList);
            REDUCE:
            while (true) {
                for (int i = 0; i < reduced.size(); ++i) {
                    for (int j = i + 1; j < reduced.size(); ++j) {

                        Thread.sleep(0);
                        if (!running) {
                            log.debug("CANCEL clustering");
                            return;
                        }

                        GeoCluster a = reduced.get(i);
                        GeoCluster b = reduced.get(j);

                        boundingBoxContains.setCenter(a.center());
                        if (boundingBoxContains.contains(b.center())) {
                            reduced.remove(a);
                            reduced.remove(b);
                            reduced.add(a.merge(b));
                            continue REDUCE;
                        }
                    }
                }
                break;
            }

            if (!running) {
                log.debug("CANCEL clustering");
                return;
            }

            callBack.callBack(reduced);
        } catch (InterruptedException e) {
            running = false;
        }
    }
}
