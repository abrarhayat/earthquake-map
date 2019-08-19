package map;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import markers.*;
import parsing.ParseFeed;
import processing.core.PApplet;
import processing.core.PShape;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 *
 * @author abrar hayat
 * Date: July 17, 2019
 */

public class EarthquakeCityMap extends PApplet {

    private static final long serialVersionUID = 1L;

    private static final boolean offline = false;

    private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

    private String cityFile = "city-data.json";
    private String countryFile = "countries.geo.json";

    private UnfoldingMap map;
    private List<Marker> cityMarkers;
    private List<Marker> quakeMarkers;
    private List<Marker> countryMarkers;
    private CommonMarker lastSelected;
    private CommonMarker lastClicked;
    private int numbersOfQuakeToPrint = 20;

    private int KEY_X_BASE = 25;
    private int KEY_Y_BASE = 50;
    private final int TEXT_SIZE = 12;
    private final int WINDOW_WIDTH = 1920;
    private final int WINDOW_HEIGHT = 1080;
    private final int MAP_WIDTH = 1280;
    private final int MAP_HEIGHT = 720;

    public void setup() {
        size(WINDOW_WIDTH, WINDOW_HEIGHT, OPENGL);
        if (offline) {
            String mbTilesString = "blankLight-1-3.mbtiles";
            map = new UnfoldingMap(this, 400, KEY_Y_BASE, MAP_WIDTH, MAP_HEIGHT, new MBTilesMapProvider(mbTilesString));
            earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
        } else {
            map = new UnfoldingMap(this, 400, KEY_Y_BASE, MAP_WIDTH, MAP_HEIGHT, new Microsoft.RoadProvider());
        }
        MapUtils.createDefaultEventDispatcher(this, map);

        List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
        countryMarkers = MapUtils.createSimpleMarkers(countries);
        List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
        cityMarkers = new ArrayList<Marker>();
        for (Feature city : cities) {
            cityMarkers.add(new CityMarker(city));
        }
        List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
        quakeMarkers = new ArrayList<Marker>();

        for (PointFeature feature : earthquakes) {
            if (isLand(feature)) {
                quakeMarkers.add(new LandQuakeMarker(feature));
            } else {
                quakeMarkers.add(new OceanQuakeMarker(feature));
            }
        }
        map.addMarkers(quakeMarkers);
        map.addMarkers(cityMarkers);
        map.zoomLevel(0);
        printQuakes();
        sortAndPrint(numbersOfQuakeToPrint);
    }


    public void draw() {
        background(255);
        map.draw();
        addKey();
        drawClickedCityInfo();
    }

    private void sortAndPrint(int numToPrint) {
        ArrayList<EarthquakeMarker> allQuakeMarkers = new ArrayList<>();
        for (Marker currentMarker : quakeMarkers) {
            allQuakeMarkers.add((EarthquakeMarker) currentMarker);
        }
        Collections.sort(allQuakeMarkers);
        System.out.println("\n" + String.format("The top %s Earthquakes(depending on availability) are shown below: ",
                numToPrint));
        for (int index = 0; index < Math.min(numToPrint + 1, quakeMarkers.size()); index++) {
            System.out.println(allQuakeMarkers.get(index).getTitle());
        }
    }

    @Override
    public void mouseMoved() {
        if (lastSelected != null) {
            lastSelected.setSelected(false);
            lastSelected = null;

        }
        selectMarkerIfHover(quakeMarkers);
        selectMarkerIfHover(cityMarkers);
    }

    private void selectMarkerIfHover(List<Marker> markers) {
        for (Marker currentMarker : markers) {
            if (currentMarker.isInside(map, mouseX, mouseY) && lastSelected == null) {
                currentMarker.setSelected(true);
                lastSelected = (CommonMarker) currentMarker;
                break;
            }
        }
    }

    @Override
    public void mouseClicked() {
        if (lastClicked != null) {
            lastClicked.setClicked(false);
            lastClicked = null;
            unhideMarkers();
        }
        setMarkerAsClicked(quakeMarkers);
        setMarkerAsClicked(cityMarkers);
    }

    private void setMarkerAsClicked(List<Marker> markers) {
        for (Marker currentMarker : markers) {
            if (currentMarker.isInside(map, mouseX, mouseY) && lastClicked == null) {
                hideMarkers();
                CommonMarker marker = (CommonMarker) currentMarker;
                lastClicked = marker;
                lastClicked.setClicked(true);
                lastClicked.setHidden(false);
                checkIfMarkerIsCityOrEarthQuake(marker);
            }
        }
    }

    private void checkIfMarkerIsCityOrEarthQuake(CommonMarker marker) {
        if (marker instanceof EarthquakeMarker) {
            System.out.println("EarthQuake Clicked!");
            for (Marker currentCity : cityMarkers) {
                System.out.println("Distance: " + marker.getDistanceTo(currentCity.getLocation()));
                System.out.println("Threat circle radius: " + ((EarthquakeMarker) marker).threatCircle());
                if (marker.getDistanceTo(currentCity.getLocation())
                        <= ((EarthquakeMarker) marker).threatCircle()) {
                    currentCity.setHidden(false);
                    if (marker instanceof OceanQuakeMarker) {
                        ((CityMarker) currentCity).setOceanMarkerSelected(true);
                    }
                }
            }
        } else {
            marker.setClicked(true);
            System.out.println("City Clicked!");
            for (Marker currentQuake : quakeMarkers) {
                System.out.println("Distance: " + marker.getDistanceTo(currentQuake.getLocation()));
                System.out.println("Threat circle radius: " + ((EarthquakeMarker) currentQuake).threatCircle());
                if (marker.getDistanceTo(currentQuake.getLocation())
                        <= ((EarthquakeMarker) currentQuake).threatCircle()) {
                    currentQuake.setHidden(false);
                }
            }
        }
    }

    private void unhideMarkers() {
        for (Marker marker : quakeMarkers) {
            marker.setHidden(false);
        }

        for (Marker marker : cityMarkers) {
            marker.setHidden(false);
            ((CityMarker) marker).setOceanMarkerSelected(false);
        }
    }

    private void hideMarkers() {
        System.out.println("Hiding all markers");
        for (Marker marker : quakeMarkers) {
            marker.setHidden(true);
        }

        for (Marker marker : cityMarkers) {
            marker.setHidden(true);
        }
    }

    private void addKey() {
        fill(255, 250, 240);

        rect(KEY_X_BASE, KEY_Y_BASE, 150, 250);

        fill(0);
        textAlign(LEFT, CENTER);
        textSize(TEXT_SIZE);
        text("Earthquake Key", KEY_X_BASE + 25, KEY_Y_BASE + 25);

        fill(150, 30, 30);
        int tri_xbase = KEY_X_BASE + 35;
        int tri_ybase = KEY_Y_BASE + 50;
        triangle(tri_xbase, tri_ybase - CityMarker.TRI_SIZE, tri_xbase - CityMarker.TRI_SIZE,
                tri_ybase + CityMarker.TRI_SIZE, tri_xbase + CityMarker.TRI_SIZE,
                tri_ybase + CityMarker.TRI_SIZE);

        fill(0, 0, 0);
        textAlign(LEFT, CENTER);
        text("City Marker", tri_xbase + 15, tri_ybase);

        text("Land Quake", KEY_X_BASE + 50, KEY_Y_BASE + 70);
        text("Ocean Quake", KEY_X_BASE + 50, KEY_Y_BASE + 90);
        text("Size ~ Magnitude", KEY_X_BASE + 25, KEY_Y_BASE + 110);

        fill(255, 255, 255);
        ellipse(KEY_X_BASE + 35,
                KEY_Y_BASE + 70,
                10,
                10);
        rect(KEY_X_BASE + 35 - 5, KEY_Y_BASE + 90 - 5, 10, 10);

        fill(color(255, 255, 0));
        ellipse(KEY_X_BASE + 35, KEY_Y_BASE + 140, 12, 12);
        fill(color(0, 0, 255));
        ellipse(KEY_X_BASE + 35, KEY_Y_BASE + 160, 12, 12);
        fill(color(255, 0, 0));
        ellipse(KEY_X_BASE + 35, KEY_Y_BASE + 180, 12, 12);

        textAlign(LEFT, CENTER);
        fill(0, 0, 0);
        text("Shallow", KEY_X_BASE + 50, KEY_Y_BASE + 140);
        text("Intermediate", KEY_X_BASE + 50, KEY_Y_BASE + 160);
        text("Deep", KEY_X_BASE + 50, KEY_Y_BASE + 180);

        text("Past hour", KEY_X_BASE + 50, KEY_Y_BASE + 200);

        fill(255, 255, 255);
        int centerx = KEY_X_BASE + 35;
        int centery = KEY_Y_BASE + 200;
        ellipse(centerx, centery, 12, 12);

        strokeWeight(2);
        line(centerx - 8, centery - 8, centerx + 8, centery + 8);
        line(centerx - 8, centery + 8, centerx + 8, centery - 8);

    }

    private boolean isLand(PointFeature earthquake) {
        for (Marker country : countryMarkers) {
            if (isInCountry(earthquake, country)) {
                return true;
            }
        }
        return false;
    }

    private void printQuakes() {
        System.out.println("\n" + "The number of earthquakes by country and by ocean are as follows: ");
        int totalWaterQuakes = quakeMarkers.size();
        for (Marker country : countryMarkers) {
            String countryName = country.getStringProperty("name");
            int numQuakes = 0;
            for (Marker marker : quakeMarkers) {
                EarthquakeMarker eqMarker = (EarthquakeMarker) marker;
                if (eqMarker.isOnLand()) {
                    if (countryName.equals(eqMarker.getStringProperty("country"))) {
                        numQuakes++;
                    }
                }
            }
            if (numQuakes > 0) {
                totalWaterQuakes -= numQuakes;
                System.out.println(countryName + ": " + numQuakes);
            }
        }
        System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
    }

    private boolean isInCountry(PointFeature earthquake, Marker country) {
        Location checkLoc = earthquake.getLocation();
        if (country.getClass() == MultiMarker.class) {
            for (Marker marker : ((MultiMarker) country).getMarkers()) {
                if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {
                    earthquake.addProperty("country", country.getProperty("name"));
                    return true;
                }
            }
        } else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc)) {
            earthquake.addProperty("country", country.getProperty("name"));

            return true;
        }
        return false;
    }

    private void showCityInfo(float x, float y, int numQuakes, float avgMag, String mostRecentEarthQuakeTitle,
                              String cityInfo) {
        PShape popUp = createShape(RECT, 0, 0, max((textWidth(mostRecentEarthQuakeTitle)),
                textWidth("No. Of Threatening Nearby Quakes: " + numQuakes)) + 20, 160);
        popUp.setFill(color(255, 250, 240));
        shape(popUp, x, y);
        fill(0);
        textSize(TEXT_SIZE);
        final float TEXT_X = x + 15;
        final float LINE_Y = y + 30;
        textAlign(LEFT, CENTER);
        text("Selected City Info: ", TEXT_X, y + 20);
        line(TEXT_X, LINE_Y, TEXT_X + textWidth("Selected City Info: ") - 5, LINE_Y);
        text("City : " + cityInfo, TEXT_X, y + 45);
        text("No. Of Threatening Nearby Quakes: " + numQuakes, TEXT_X, y + 70);
        text("Average Quake Magnitude: " + avgMag, TEXT_X, y + 95);
        text("Most Recent Earthquake: ", TEXT_X, y + 120);
        text(mostRecentEarthQuakeTitle, TEXT_X, y + 140);
    }

    private ArrayList<EarthquakeMarker> getAllNearbyQuakes(CityMarker cityMarker) {
        ArrayList<EarthquakeMarker> allNearbyQuakeMarkers = new ArrayList<>();
        for (Marker currentQuake : quakeMarkers) {
            if (cityMarker.getDistanceTo(currentQuake.getLocation())
                    <= ((EarthquakeMarker) currentQuake).threatCircle()) {
                currentQuake.setHidden(false);
                allNearbyQuakeMarkers.add((EarthquakeMarker) currentQuake);
            }
        }
        return allNearbyQuakeMarkers;
    }

    private boolean isLastClickedCityMarker() {
        return (lastClicked != null && lastClicked instanceof CityMarker);
    }

    private void drawClickedCityInfo() {
        if (isLastClickedCityMarker()) {
            ArrayList<EarthquakeMarker> allQuakesNearby = getAllNearbyQuakes((CityMarker) lastClicked);
            if (allQuakesNearby.size() > 0) {
                float totalMag = 0;
                EarthquakeMarker mostRecent = null;
                for (EarthquakeMarker current : allQuakesNearby) {
                    totalMag += current.getMagnitude();
                }
                mostRecent = allQuakesNearby.get(0);
                for (EarthquakeMarker current : allQuakesNearby) {
                    if (current.getAgeInt() < mostRecent.getAgeInt()) {
                        mostRecent = current;
                    }
                }
                float avg = (float) totalMag / allQuakesNearby.size();
                DecimalFormat decimalFormat = new DecimalFormat();
                decimalFormat.setMaximumFractionDigits(2);
                avg = Float.parseFloat(decimalFormat.format(avg));
                CityMarker city = (CityMarker) lastClicked;
                showCityInfo(KEY_X_BASE, KEY_Y_BASE + 300, allQuakesNearby.size(), avg, mostRecent.getTitle(),
                        city.getCity() + ", " + city.getCountry());
            }
        }
    }
}
