package de.longri.cachebox3;

import android.os.Bundle;
import android.util.DisplayMetrics;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import org.oscim.backend.CanvasAdapter;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize platform bitmap factory
        org.oscim.android.canvas.AndroidGraphics.init();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        CanvasAdapter.dpi = (int) Math.max(metrics.xdpi, metrics.ydpi);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        initialize(new CacheboxMain(), config);
    }
}
