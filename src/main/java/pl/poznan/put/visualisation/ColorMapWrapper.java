package pl.poznan.put.visualisation;

import java.awt.Color;
import java.awt.color.ColorSpace;

import org.jzy3d.colors.colormaps.AbstractColorMap;
import org.jzy3d.colors.colormaps.ColorMapWhiteRed;

import fr.orsay.lri.varna.models.rna.ModeleColorMap;

public class ColorMapWrapper {
    private static final AbstractColorMap JZY3D_COLOR_MAP = new ColorMapWhiteRed();

    static {
        ColorMapWrapper.JZY3D_COLOR_MAP.setDirection(false);
    }

    public static AbstractColorMap getJzy3dColorMap() {
        return ColorMapWrapper.JZY3D_COLOR_MAP;
    }

    public static ModeleColorMap getVarnaColorMap(double min, double max) {
        ModeleColorMap colorMap = ModeleColorMap.redColorMap();
        colorMap.rescale(min, max);
        return colorMap;
    }

    public static ModeleColorMap transformJzy3dToVarnaColorMap() {
        ModeleColorMap modelColorMap = new ModeleColorMap();

        for (double d = 0; d <= Math.PI; d += Math.PI / 36) {
            Color color = ColorMapWrapper.getColor(d, 0, Math.PI);
            modelColorMap.addColor(d, color);
        }

        return modelColorMap;
    }

    public static Color getColor(double value, double min, double max) {
        float[] rgba = ColorMapWrapper.JZY3D_COLOR_MAP.getColor(0, 0, value, min, max).toArray();
        return new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), new float[] { rgba[0], rgba[1], rgba[2] }, rgba[3]);
    }

    private ColorMapWrapper() {
        // empty constructor
    }
}
