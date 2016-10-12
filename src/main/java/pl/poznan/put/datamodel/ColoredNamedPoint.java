package pl.poznan.put.datamodel;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;

public class ColoredNamedPoint extends NamedPoint {
    private final Set<Color> colors;

    public ColoredNamedPoint(Set<Color> colors, String name, Vector2D point) {
        super(name, point);
        this.colors = colors;
    }

    public Set<Color> getColors() {
        return Collections.unmodifiableSet(colors);
    }
}
