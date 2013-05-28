package pl.poznan.put.cs.bioserver.helper;

import java.awt.Color;

import pl.poznan.put.cs.bioserver.beans.auxiliary.RGB;

public class Colors {
    public static final Color[] ALL = new Color[] { Color.WHITE,
            new Color(173, 35, 35), new Color(42, 75, 215),
            new Color(29, 105, 20), new Color(129, 74, 25),
            new Color(129, 38, 192), new Color(160, 160, 160),
            new Color(129, 197, 122), new Color(157, 175, 255),
            new Color(41, 208, 208), new Color(255, 146, 51),
            new Color(255, 238, 51), new Color(233, 222, 187),
            new Color(255, 205, 243), new Color(87, 87, 87),
            new Color(112, 219, 147), new Color(181, 166, 66),
            new Color(95, 159, 159), new Color(184, 115, 51),
            new Color(47, 79, 47), new Color(153, 50, 205),
            new Color(135, 31, 120), new Color(133, 94, 66),
            new Color(84, 84, 84), new Color(142, 35, 35),
            new Color(245, 204, 176), new Color(35, 142, 35),
            new Color(205, 127, 50), new Color(219, 219, 112),
            new Color(192, 192, 192), new Color(82, 127, 118),
            new Color(159, 159, 95), new Color(142, 35, 107),
            new Color(47, 47, 79), new Color(235, 199, 158),
            new Color(207, 181, 59), new Color(255, 127, 0),
            new Color(219, 112, 219), new Color(217, 217, 243),
            new Color(89, 89, 171), new Color(140, 23, 23),
            new Color(35, 142, 104), new Color(107, 66, 38),
            new Color(142, 107, 35), new Color(0, 127, 255),
            new Color(0, 255, 127), new Color(35, 107, 142),
            new Color(56, 176, 222), new Color(219, 147, 112),
            new Color(173, 234, 234), new Color(92, 64, 51),
            new Color(79, 47, 79), new Color(204, 50, 153),
            new Color(153, 204, 50), };

    public static RGB[] toRGB() {
        RGB[] result = new RGB[ALL.length];
        for (int i = 0; i < ALL.length; i++) {
            result[i] = RGB.newInstance(ALL[i].getRGBColorComponents(null));
        }
        return result;
    }
}
