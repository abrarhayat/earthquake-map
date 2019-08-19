package Utils;

import processing.core.PGraphics;

/**
 * @author abrar
 * since 7/19/2019
 */

public class GraphicsUtils {

    public static void drawACross(PGraphics pg, float x, float y, int lineDistFromCenter) {
        pg.line(x - lineDistFromCenter, y + lineDistFromCenter, x + lineDistFromCenter,
                y - lineDistFromCenter);
        pg.line(x - lineDistFromCenter, y - lineDistFromCenter, x + lineDistFromCenter,
                y + lineDistFromCenter);
    }
}
