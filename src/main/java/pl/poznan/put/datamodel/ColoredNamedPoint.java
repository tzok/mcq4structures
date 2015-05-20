package pl.poznan.put.datamodel;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class ColoredNamedPoint extends NamedPoint {
    private final Color color;

    public ColoredNamedPoint(Color color, String name, Vector2D point) {
        super(name, point);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "ColoredNamedPoint [color=" + color + ", name=" + name + ", point=" + point + "]";
    }
}
