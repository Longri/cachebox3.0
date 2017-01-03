/*
 * Copyright 2013 Hannes Janetzek
 * Copyright 2016 devemux86
 * Copyright 2016 Izumi Kawashima
 *
 * This file is part of the OpenScienceMap project (http://www.opensciencemap.org).
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.longri.cachebox3.gui.map.layer.cluster;

import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.PointF;

public class ClusterSymbol {


    final Bitmap mBitmap;


    public ClusterSymbol(Bitmap bitmap, float relX, float relY) {
        this(bitmap, relX, relY, true);
    }

    public ClusterSymbol(Bitmap bitmap, float relX, float relY, boolean billboard) {
        mBitmap = bitmap;
    }

    public ClusterSymbol(Bitmap bitmap) {
        this(bitmap, true);
    }

    public ClusterSymbol(Bitmap bitmap, boolean billboard) {
        mBitmap = bitmap;
    }


    public Bitmap getBitmap() {
        return mBitmap;
    }


//    public boolean isInside(float dx, float dy) {
//        /* TODO handle no-billboard */
//        int w, h;
//        w = mBitmap.getWidth();
//        h = mBitmap.getHeight();
//
//        float ox = -w * mOffset.x;
//        float oy = -h * (1 - mOffset.y);
//
//        return dx >= ox && dy >= oy && dx <= ox + w && dy <= oy + h;
//    }
}
