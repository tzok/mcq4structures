package pl.poznan.put.datamodel;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class NamedPoint {
  protected final String name;
  protected final Vector2D point;

  public NamedPoint(String name, Vector2D point) {
    super();
    this.name = name;
    this.point = point;
  }

  public String getName() {
    return name;
  }

  public double getX() {
    return point.getX();
  }

  public double getY() {
    return point.getY();
  }

  public double distance(NamedPoint other) {
    return point.distance(other.point);
  }

  public NamedPoint scalarMultiply(double a) {
    return new NamedPoint(name, point.scalarMultiply(a));
  }

  @Override
  public String toString() {
    return "NamedPoint [name=" + name + ", point=" + point + "]";
  }
}
