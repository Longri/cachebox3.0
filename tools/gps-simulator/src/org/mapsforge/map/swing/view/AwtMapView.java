package org.mapsforge.map.swing.view;

import org.mapsforge.map.awt.view.MapView;

import java.awt.*;

public class AwtMapView extends MapView {

	private static final long serialVersionUID = 1L;

	public AwtMapView() {
		super();
			}

	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);

		int xc = this.getWidth() / 2;
		int yc = this.getHeight() / 2;

		graphics.drawLine(xc - 50, yc - 50, xc + 50, yc + 50);
		graphics.drawLine(xc - 50, yc + 50, xc + 50, yc - 50);
		graphics.drawOval(xc - 25, yc - 25, 50, 50);
	}

}
