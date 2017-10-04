package pl.poznan.put.datamodel;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ColoredNamedPoint extends NamedPoint {
    private final Set<Color> colors;

    public ColoredNamedPoint(final Set<Color> colors, final String name,
                             final Vector2D point) {
        super(name, point);
        this.colors = new HashSet<>(colors);
    }

    public final Set<Color> getColors() {
        return Collections.unmodifiableSet(colors);
    }
}
