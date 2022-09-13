package pl.poznan.put.visualisation;

import java.util.Collection;
import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.Histogram;
import pl.poznan.put.circular.ImmutableHistogram;
import pl.poznan.put.circular.enums.AngleTransformation;

public class AngularHistogram extends RawDataPlot {
  private final double binRadians;
  private double scalingFactor;

  public AngularHistogram(
      final Collection<? extends Angle> data,
      final double binRadians,
      final double diameter,
      final double majorTickSpread,
      final double minorTickSpread,
      final AngleTransformation angleTransformation) {
    super(data, diameter, majorTickSpread, minorTickSpread, angleTransformation);
    this.binRadians = binRadians;
  }

  public AngularHistogram(
      final Collection<? extends Angle> data, final double binRadians, final double diameter) {
    super(data, diameter);
    this.binRadians = binRadians;
  }

  public AngularHistogram(final Collection<? extends Angle> data, final double binRadians) {
    super(data);
    this.binRadians = binRadians;
  }

  public AngularHistogram(final Collection<? extends Angle> data) {
    super(data);
    binRadians = FastMath.PI / 12;
  }

  @Override
  public final void draw() {
    super.draw();

    final Histogram histogram = ImmutableHistogram.of(getData(), binRadians);
    double maxFrequency = Double.NEGATIVE_INFINITY;

    for (double d = 0; d < MathUtils.TWO_PI; d += binRadians) {
      final double frequency = (double) histogram.findBin(d).size() / getData().size();
      maxFrequency = FastMath.max(frequency, maxFrequency);
    }

    // the 0.8 is here because up to 0.85 the majorTick can be drawn and we
    // do not want overlaps
    scalingFactor = 0.8 / FastMath.sqrt(maxFrequency);

    for (double d = 0; d < MathUtils.TWO_PI; d += binRadians) {
      final double frequency = (double) histogram.findBin(d).size() / getData().size();
      if (frequency > 0) {
        drawHistogramTriangle(d, frequency);
      }
    }
  }

  private void drawHistogramTriangle(final double angleValue, final double frequency) {
    final double sectorRadius = FastMath.sqrt(frequency) * getRadius() * scalingFactor;

    // angle as in XY coordinate system
    final double t1 = transform(angleValue);
    final double x1 = getCenterX() + (sectorRadius * FastMath.cos(t1));
    final double y1 = getCenterY() + (sectorRadius * FastMath.sin(t1));

    final double t2 = transform(angleValue + binRadians);
    final double x2 = getCenterX() + (sectorRadius * FastMath.cos(t2));
    final double y2 = getCenterY() + (sectorRadius * FastMath.sin(t2));

    final float[] xs = {(float) x1, (float) x2, (float) getCenterX()};
    final float[] ys = {
      (float) (getDiameter() - y1),
      (float) (getDiameter() - y2),
      (float) (getDiameter() - getCenterY())
    };
    svgGraphics.draw(new Polygon2D(xs, ys, 3));
  }
}
