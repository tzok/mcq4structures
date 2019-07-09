package pl.poznan.put.visualisation;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.Drawable;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
import pl.poznan.put.constant.Colors;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.AngleDeltaIterator;
import pl.poznan.put.matching.FragmentComparison;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.TypedDeltaIterator;
import pl.poznan.put.matching.stats.SingleMatchStatistics;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.structure.secondary.formats.DotBracket;
import pl.poznan.put.structure.secondary.formats.InvalidStructureException;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.svg.SVGHelper;

@Slf4j
public class VisualizableFragmentMatch extends FragmentMatch implements Visualizable {
  private static final NumberFormat NUMBER_FORMAT =
      new NumberFormat() {
        private static final long serialVersionUID = -5555343582625013384L;

        @Override
        public StringBuffer format(
            final double v, final StringBuffer stringBuffer, final FieldPosition fieldPosition) {
          return stringBuffer.append(AngleFormat.degreesRoundedToOne(v));
        }

        @Override
        public StringBuffer format(
            final long l, final StringBuffer stringBuffer, final FieldPosition fieldPosition) {
          return stringBuffer.append(AngleFormat.degreesRoundedToOne(l));
        }

        @Override
        public Number parse(final String s, final ParsePosition parsePosition) {
          throw new UnsupportedOperationException("Unsupported");
        }
      };

  public VisualizableFragmentMatch(final FragmentMatch fragmentMatch) {
    super(
        fragmentMatch.getTargetFragment(),
        fragmentMatch.getModelFragment(),
        fragmentMatch.isTargetSmaller(),
        fragmentMatch.getShift(),
        fragmentMatch.getFragmentComparison());
  }

  @Override
  public final SVGDocument visualize() {
    return visualize(640, 480);
  }

  @Override
  public void visualize3D() {
    // do nothing
  }

  public final SVGDocument visualize(final int width, final int height) {
    final DefaultXYDataset dataset = new DefaultXYDataset();
    final XYItemRenderer renderer = new DefaultXYItemRenderer();

    prepareDataset(dataset, renderer);

    final ValueAxis domainAxis = prepareDomainAxis();
    final NumberAxis rangeAxis = new NumberAxis();
    rangeAxis.setLabel("Angular distance");
    rangeAxis.setRange(0, Math.PI);
    rangeAxis.setTickUnit(new NumberTickUnit(Math.PI / 12.0));
    rangeAxis.setNumberFormatOverride(VisualizableFragmentMatch.NUMBER_FORMAT);

    return VisualizableFragmentMatch.plotAsSvg(
        width, height, dataset, renderer, domainAxis, rangeAxis);
  }

  private void prepareDataset(final DefaultXYDataset dataset, final XYItemRenderer renderer) {
    final FragmentComparison fragmentComparison = getFragmentComparison();
    int i = 0;
    for (final MasterTorsionAngleType angle : fragmentComparison.getAngleTypes()) {
      final double[][] data = new double[2][];
      data[0] = new double[fragmentComparison.getResidueCount()];
      data[1] = new double[fragmentComparison.getResidueCount()];

      int j = 0;
      for (final ResidueComparison residue : fragmentComparison.getResidueComparisons()) {
        final TorsionAngleDelta delta = residue.getAngleDelta(angle);
        data[0][j] = j;

        data[1][j] =
            (delta.getState() == TorsionAngleDelta.State.BOTH_VALID)
                ? delta.getDelta().getRadians()
                : Double.NaN;

        j++;
      }

      final String displayName = angle.getLongDisplayName();
      dataset.addSeries(displayName, data);
      renderer.setSeriesPaint(i, Colors.getDistinctColors()[i]);
      i++;
    }
  }

  private ValueAxis prepareDomainAxis() {
    ValueAxis domainAxis = null;

    if (moleculeType() == MoleculeType.RNA) {
      try {
        final DotBracket dotBracket = matchedSecondaryStructure();
        final List<String> ticks =
            dotBracket
                .getStructure()
                .chars()
                .mapToObj(i -> String.valueOf((char) i))
                .collect(Collectors.toList());
        domainAxis = new TorsionAxis(ticks, 0, 12);
        domainAxis.setLabel("Secondary structure");
      } catch (final InvalidStructureException e) {
        VisualizableFragmentMatch.log.warn("Failed to extract canonical secondary structure", e);
      }
    }

    if (domainAxis == null) {
      final List<String> ticks = matchedResidueNames();
      domainAxis = new TorsionAxis(ticks, -Math.PI / 4, 6);
      domainAxis.setLabel("ResID");
    }

    return domainAxis;
  }

  private static SVGDocument plotAsSvg(
      final int width,
      final int height,
      final XYDataset dataset,
      final XYItemRenderer renderer,
      final ValueAxis domainAxis,
      final ValueAxis rangeAxis) {
    final Plot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
    final Drawable chart = new JFreeChart(plot);

    final SVGDocument document = SVGHelper.emptyDocument();
    final SVGGraphics2D graphics = new SVGGraphics2D(document);
    graphics.setSVGCanvasSize(new Dimension(width, height));
    chart.draw(graphics, new Rectangle(width, height));

    final SVGSVGElement root = document.getRootElement();
    graphics.getRoot(root);

    final Rectangle2D boundingBox = SVGHelper.calculateBoundingBox(document);
    root.setAttributeNS(
        null,
        SVGConstants.SVG_VIEW_BOX_ATTRIBUTE,
        String.format(
            "%s %s %s %s",
            boundingBox.getMinX(),
            boundingBox.getMinY(),
            boundingBox.getWidth(),
            boundingBox.getHeight()));
    root.setAttributeNS(
        null, SVGConstants.SVG_WIDTH_ATTRIBUTE, Double.toString(boundingBox.getWidth()));
    root.setAttributeNS(
        null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, Double.toString(boundingBox.getHeight()));
    return document;
  }

  public final SVGDocument visualizePercentiles(final int width, final int height) {
    final DefaultXYDataset dataset = new DefaultXYDataset();
    final XYItemRenderer renderer = new DefaultXYItemRenderer();
    preparePercentilesDataset(dataset, renderer);

    final NumberAxis domainAxis = new NumberAxis();
    domainAxis.setLabel("Percentile");
    domainAxis.setRange(0, 100);

    final NumberAxis rangeAxis = new NumberAxis();
    rangeAxis.setLabel("Angular distance");
    rangeAxis.setRange(0, Math.PI);
    rangeAxis.setTickUnit(new NumberTickUnit(Math.PI / 12.0));
    rangeAxis.setNumberFormatOverride(VisualizableFragmentMatch.NUMBER_FORMAT);

    return VisualizableFragmentMatch.plotAsSvg(
        width, height, dataset, renderer, domainAxis, rangeAxis);
  }

  private void preparePercentilesDataset(
      final DefaultXYDataset dataset, final XYItemRenderer renderer) {
    final String name = getModelFragment().getName();
    final double[] percents = SingleMatchStatistics.PERCENTS_FROM_1_TO_100;
    final List<MasterTorsionAngleType> angleTypes = getFragmentComparison().getAngleTypes();

    for (int j = 0; j < angleTypes.size(); j++) {
      final MasterTorsionAngleType masterType = angleTypes.get(j);
      final AngleDeltaIterator angleDeltaIterator = new TypedDeltaIterator(this, masterType);
      final SingleMatchStatistics statistics =
          SingleMatchStatistics.calculate(name, angleDeltaIterator, new double[0], percents);

      final double[][] data = new double[2][];
      data[0] = new double[percents.length];
      data[1] = new double[percents.length];

      for (int i = 0; i < percents.length; i++) {
        data[0][i] = percents[i];
        data[1][i] = statistics.getAngleThresholdForGivenPercentile(percents[i]);
      }

      final String displayName = masterType.getLongDisplayName();
      dataset.addSeries(displayName, data);
      renderer.setSeriesPaint(j, Colors.getDistinctColors()[j]);
    }
  }
}
