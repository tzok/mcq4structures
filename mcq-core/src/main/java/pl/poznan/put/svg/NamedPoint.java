package pl.poznan.put.svg;

import lombok.Data;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

@Data
public class NamedPoint {
  private final String name;
  private final Vector2D point;

  public final double getX() {
    return point.getX();
  }

  public final double getY() {
    return point.getY();
  }

  public final double distance(final NamedPoint other) {
    return point.distance(other.point);
  }

  public final NamedPoint scalarMultiply(final double a) {
    return new NamedPoint(name, point.scalarMultiply(a));
  }
}
