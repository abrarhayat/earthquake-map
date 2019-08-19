package finalModule;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/**
 * Implements a visual marker for earthquakes on an earthquake map
 *
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author abrar hayat
 */

public abstract class EarthquakeMarker extends CommonMarker implements Comparable<EarthquakeMarker> {

    protected boolean isOnLand;
    protected float radius;


    protected static final float kmPerMile = 1.6f;
    public static final float THRESHOLD_MODERATE = 5;
    public static final float THRESHOLD_LIGHT = 4;
    public static final float THRESHOLD_INTERMEDIATE = 70;
    public static final float THRESHOLD_DEEP = 300;

    private final int PAST_HOUR = 1;
    private final int PAST_DAY = 2;
    private final int PAST_WEEK = 3;
    private final int PAST_MONTH = 4;

    public abstract void drawEarthquake(PGraphics pg, float x, float y);

    public EarthquakeMarker(PointFeature feature) {
        super(feature.getLocation());
        java.util.HashMap<String, Object> properties = feature.getProperties();
        float magnitude = Float.parseFloat(properties.get("magnitude").toString());
        properties.put("radius", 2 * magnitude);
        setProperties(properties);
        this.radius = 1.75f * getMagnitude();
    }

    public int compareTo(EarthquakeMarker otherEarthquakeMarker) {
        //-1 is multiplied so that the default order is Descending order
        return -1 * Float.compare(this.getMagnitude(), otherEarthquakeMarker.getMagnitude());
    }

    @Override
    public void drawMarker(PGraphics pg, float x, float y) {
        pg.pushStyle();
        colorDetermine(pg);
        drawEarthquake(pg, x, y);
        String age = getStringProperty("age");
        if ("Past Hour".equals(age) || "Past Day".equals(age)) {

            pg.strokeWeight(2);
            int buffer = 2;
            pg.line(x - (radius + buffer),
                    y - (radius + buffer),
                    x + radius + buffer,
                    y + radius + buffer);
            pg.line(x - (radius + buffer),
                    y + (radius + buffer),
                    x + radius + buffer,
                    y - (radius + buffer));

        }
        pg.popStyle();

    }

    @Override
    public void showTitle(PGraphics pg, float x, float y) {
        String title = this.getTitle();
        int textSize = 12;
        float rectX = x + 10;
        float rectY = y - textSize - 5;
        pg.textSize(textSize);
        pg.fill(pg.color(255, 255, 255));
        pg.rect(rectX, rectY, pg.textWidth(title) + 15, 20, 2);
        pg.fill(0);
        pg.text(title, rectX + 5, rectY + 5);
    }

    public double threatCircle() {
        double miles = 20.0f * Math.pow(1.8, 2 * getMagnitude() - 5);
        double km = (miles * kmPerMile);
        return km;
    }

    private void colorDetermine(PGraphics pg) {
        float depth = getDepth();

        if (depth < THRESHOLD_INTERMEDIATE) {
            pg.fill(255, 255, 0);
        } else if (depth < THRESHOLD_DEEP) {
            pg.fill(0, 0, 255);
        } else {
            pg.fill(255, 0, 0);
        }
    }

    public float getMagnitude() {
        return Float.parseFloat(getProperty("magnitude").toString());
    }

    public float getDepth() {
        return Float.parseFloat(getProperty("depth").toString());
    }

    public String getTitle() {
        return (String) getProperty("title");

    }

    public float getRadius() {
        return Float.parseFloat(getProperty("radius").toString());
    }

    public int getAgeInt() {
        String age = ((String) (this.getProperty("age"))).toLowerCase();
        if(age.contains("hour")) {
            return PAST_HOUR;
        } else if(age.contains("day")) {
            return PAST_DAY;
        } else if(age.contains("week")) {
            return PAST_WEEK;
        } else {
            return PAST_MONTH;
        }
    }

    public boolean isOnLand() {
        return isOnLand;
    }
}
