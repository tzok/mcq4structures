package pl.poznan.put.datamodel;

import java.awt.Color;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

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
