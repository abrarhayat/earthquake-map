package markers;

import utils.GraphicsUtils;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Implements a visual marker for cities on an earthquake map
 *
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author abrar hayat
 */

public class CityMarker extends CommonMarker {

    public static int TRI_SIZE = 5;  // The size of the triangle marker

    private boolean oceanMarkerSelected = false;

    public CityMarker(Location location) {
        super(location);
    }


    public CityMarker(Feature city) {
        super(((PointFeature) city).getLocation(), city.getProperties());
    }

    public void drawMarker(PGraphics pg, float x, float y) {
        pg.pushStyle();
        pg.fill(150, 30, 30);
        //drawing a cross if a threatening ocean quake is selected
        pg.triangle(x, y - TRI_SIZE, x - TRI_SIZE, y + TRI_SIZE, x + TRI_SIZE, y + TRI_SIZE);
        if (oceanMarkerSelected) {
            GraphicsUtils.drawACross(pg, x, y, 10);
        }
        pg.popStyle();
    }

    public void showTitle(PGraphics pg, float x, float y) {
        String name = this.getCity() + ", " + this.getCountry();
        String population = "Population: " + getPopulation() + " Million";
        int textSize = 12;
        float rectX = x + 10;
        float rectY = y - textSize - 5;
        pg.textSize(textSize);
        pg.fill(pg.color(255, 255, 255));
        pg.rect(rectX, rectY, Math.max(pg.textWidth(name), pg.textWidth(population)) + 15, 39);
        pg.fill(0, 0 , 0);
        pg.textAlign(PConstants.LEFT, PConstants.TOP);
        pg.text(name , rectX + 5, rectY + 5);
        pg.text(population, rectX + 5, rectY + 20);
    }

    public String getCity() {
        return getStringProperty("name");
    }

    public String getCountry() {
        return getStringProperty("country");
    }

    public float getPopulation() {
        return Float.parseFloat(getStringProperty("population"));
    }

    public void setOceanMarkerSelected(boolean oceanMarkerSelected) {
        this.oceanMarkerSelected = oceanMarkerSelected;
    }
}
