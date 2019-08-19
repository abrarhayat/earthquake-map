package finalModule;

import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

/**
 * Implements a common marker for cities and earthquakes on an earthquake map
 *
 * @author UC San Diego Intermediate Software Development MOOC team
 *
 */

public abstract class CommonMarker extends SimplePointMarker {

    protected boolean clicked = false;

    public CommonMarker(Location location) {
        super(location);
    }

    public CommonMarker(Location location, java.util.HashMap<String, Object> properties) {
        super(location, properties);
    }

    public boolean getClicked() {
        return clicked;
    }

    public void setClicked(boolean state) {
        clicked = state;
    }

    public void draw(PGraphics pg, float x, float y) {
        if (!hidden) {
            drawMarker(pg, x, y);
            if (selected) {
                showTitle(pg, x, y);
            }
        }
    }

    public abstract void drawMarker(PGraphics pg, float x, float y);

    public abstract void showTitle(PGraphics pg, float x, float y);
}