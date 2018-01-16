package pl.poznan.put.visualisation;

import fr.orsay.lri.varna.models.rna.ModeleColorMap;
import org.jzy3d.colors.colormaps.AbstractColorMap;

import java.awt.Color;

public final class ColorMaps {
    // colors taken from YlOrRd palette on: http://colorbrewer2.org
    private static final Color IDENTICAL = Color.decode("#fef0d9");
    private static final Color SIMILAR = Color.decode("#fdcc8a");
    private static final Color DIFFERENT = Color.decode("#fc8d59");
    private static final Color OPPOSITE = Color.decode("#d7301f");

    // colors taken from 12-class Paired paletter on: http://colorbrewer2.org
    private static final Color[] DISTINCT_COLORS_PAIRED =
            {Color.decode("#a6cee3"), Color.decode("#1f78b4"),
             Color.decode("#b2df8a"), Color.decode("#33a02c"),
             Color.decode("#fb9a99"), Color.decode("#e31a1c"),
             Color.decode("#fdbf6f"), Color.decode("#ff7f00"),
             Color.decode("#cab2d6"), Color.decode("#6a3d9a"),
             Color.decode("#ffff99"), Color.decode("#b15928")};

    // colors taken from 12-class Set3 paletter on: http://colorbrewer2.org
    private static final Color[] DISTINCT_COLORS_SET3 =
            {Color.decode("#8dd3c7"), Color.decode("#ffffb3"),
             Color.decode("#bebada"), Color.decode("#fb8072"),
             Color.decode("#80b1d3"), Color.decode("#fdb462"),
             Color.decode("#b3de69"), Color.decode("#fccde5"),
             Color.decode("#d9d9d9"), Color.decode("#bc80bd"),
             Color.decode("#ccebc5"), Color.decode("#ffed6f")};

    public static Color getDistinctColorPaired(final int index) {
        return ColorMaps.DISTINCT_COLORS_PAIRED[index];
    }

    public static Color getDistinctColorSet3(final int index) {
        return ColorMaps.DISTINCT_COLORS_SET3[index];
    }

    public static ModeleColorMap getVarnaColorMap() {
        final ModeleColorMap map = new ModeleColorMap();
        map.addColor(0.0 / 3.0, ColorMaps.IDENTICAL);
        map.addColor(1.0 / 3.0, ColorMaps.SIMILAR);
        map.addColor(2.0 / 3.0, ColorMaps.DIFFERENT);
        map.addColor(3.0 / 3.0, ColorMaps.OPPOSITE);
        return map;
    }

    private static final AbstractColorMap JZY3D_COLOR_MAP =
            new AbstractColorMap() {
                private final org.jzy3d.colors.Color identical =
                        new org.jzy3d.colors.Color(ColorMaps.IDENTICAL.getRed(),
                                                   ColorMaps.IDENTICAL
                                                           .getGreen(),
                                                   ColorMaps.IDENTICAL
                                                           .getBlue());
                private final org.jzy3d.colors.Color similar =
                        new org.jzy3d.colors.Color(ColorMaps.SIMILAR.getRed(),
                                                   ColorMaps.SIMILAR.getGreen(),
                                                   ColorMaps.SIMILAR.getBlue());
                private final org.jzy3d.colors.Color different =
                        new org.jzy3d.colors.Color(ColorMaps.DIFFERENT.getRed(),
                                                   ColorMaps.DIFFERENT
                                                           .getGreen(),
                                                   ColorMaps.DIFFERENT
                                                           .getBlue());
                private final org.jzy3d.colors.Color opposite =
                        new org.jzy3d.colors.Color(ColorMaps.OPPOSITE.getRed(),
                                                   ColorMaps.OPPOSITE
                                                           .getGreen(),
                                                   ColorMaps.OPPOSITE
                                                           .getBlue());

                @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
                @Override
                public org.jzy3d.colors.Color getColor(final double x,
                                                       final double y,
                                                       final double z,
                                                       final double zMin,
                                                       final double zMax) {
                    final double zRel = processRelativeZValue(z, zMin, zMax);
                    if (zRel < 0.15) {
                        return identical;
                    } else if (zRel < 0.3) {
                        return similar;
                    } else if (zRel < 0.6) {
                        return different;
                    } else {
                        return opposite;
                    }
                }
            };

    public static AbstractColorMap getJzy3dColorMap() {
        return ColorMaps.JZY3D_COLOR_MAP;
    }

    private ColorMaps() {
        // empty constructor
        super();
    }
}
