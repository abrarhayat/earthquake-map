package finalModule;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/**
 * Implements a visual marker for land earthquakes on an earthquake map
 *
 * @author UC San Diego Intermediate Software Development MOOC team
 */

public class LandQuakeMarker extends EarthquakeMarker {


    public LandQuakeMarker(PointFeature quake) {
        super(quake);
        isOnLand = true;
    }

    @Override
    public void drawEarthquake(PGraphics pg, float x, float y) {
        pg.ellipse(x, y, 2 * radius, 2 * radius);

    }

    public String getCountry() {
        return (String) getProperty("country");
    }


}