package pl.poznan.put.visualisation;

import fr.orsay.lri.varna.models.rna.ModeleColorMap;
import org.jzy3d.colors.colormaps.AbstractColorMap;

import java.awt.Color;

public final class ColorMapWrapper {
    private static final AbstractColorMap JZY3D_COLOR_MAP =
            new AbstractColorMap() {
                private final org.jzy3d.colors.Color blue =
                        new org.jzy3d.colors.Color(0, 0, 255);
                private final org.jzy3d.colors.Color yellow =
                        new org.jzy3d.colors.Color(255, 237, 160);
                private final org.jzy3d.colors.Color orange =
                        new org.jzy3d.colors.Color(254, 178, 76);
                private final org.jzy3d.colors.Color red =
                        new org.jzy3d.colors.Color(240, 59, 32);

                @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
                @Override
                public org.jzy3d.colors.Color getColor(final double x,
                                                       final double y,
                                                       final double z,
                                                       final double zMin,
                                                       final double zMax) {
                    final double zRel = processRelativeZValue(z, zMin, zMax);
                    if (zRel < 0.15) {
                        return blue;
                    } else if (zRel < 0.3) {
                        return yellow;
                    } else if (zRel < 0.6) {
                        return orange;
                    } else {
                        return red;
                    }
                }
            };

    private static final Color BLUE = new Color(0.0f, 0.0f, 1.0f);
    private static final Color YELLOW = Color.decode("#ffeda0");
    private static final Color ORANGE = Color.decode("#feb24c");
    private static final Color RED = Color.decode("#f03b20");

    private ColorMapWrapper() {
        // empty constructor
        super();
    }

    public static AbstractColorMap getJzy3dColorMap() {
        return ColorMapWrapper.JZY3D_COLOR_MAP;
    }

    public static ModeleColorMap getVarnaColorMap() {
        final ModeleColorMap map = new ModeleColorMap();
        map.addColor(0.0 / 3.0, ColorMapWrapper.BLUE);
        map.addColor(1.0 / 3.0, ColorMapWrapper.YELLOW);
        map.addColor(2.0 / 3.0, ColorMapWrapper.ORANGE);
        map.addColor(3.0 / 3.0, ColorMapWrapper.RED);
        return map;
    }

    public static Color getColor(final double radians) {
        if (Math.toDegrees(radians) < 15.0) {
            return ColorMapWrapper.BLUE;
        }
        if (Math.toDegrees(radians) < 30.0) {
            return Color.YELLOW;
        }
        if (Math.toDegrees(radians) < 45.0) {
            return Color.ORANGE;
        }
        return ColorMapWrapper.RED;
    }
}
