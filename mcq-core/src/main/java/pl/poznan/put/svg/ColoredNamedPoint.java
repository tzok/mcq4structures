package pl.poznan.put.svg;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ColoredNamedPoint extends NamedPoint {
  private final Set<Color> colors;

  public ColoredNamedPoint(final Set<Color> colors, final String name, final Vector2D point) {
    super(name, point);
    this.colors = new HashSet<>(colors);
  }
}
