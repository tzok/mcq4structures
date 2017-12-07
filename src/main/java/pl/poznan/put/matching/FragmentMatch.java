package pl.poznan.put.matching;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.constant.Colors;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.stats.SingleMatchStatistics;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.structure.secondary.CanonicalStructureExtractor;
import pl.poznan.put.structure.secondary.DotBracketSymbol;
import pl.poznan.put.structure.secondary.formats.BpSeq;
import pl.poznan.put.structure.secondary.formats.Converter;
import pl.poznan.put.structure.secondary.formats.DotBracket;
import pl.poznan.put.structure.secondary.formats.InvalidStructureException;
import pl.poznan.put.structure.secondary.formats.LevelByLevelConverter;
import pl.poznan.put.structure.secondary.pseudoknots.elimination.MinGain;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.svg.SVGHelper;
import pl.poznan.put.visualisation.TorsionAxis;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class FragmentMatch implements Visualizable {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(FragmentMatch.class);
    private final PdbCompactFragment targetFragment;
    private final PdbCompactFragment modelFragment;
    private final boolean isTargetSmaller;
    private final int shift;
    private final FragmentComparison fragmentComparison;

    public FragmentMatch(final PdbCompactFragment targetFragment,
                         final PdbCompactFragment modelFragment,
                         final boolean isTargetSmaller, final int shift,
                         final FragmentComparison comparison) {
        super();
        this.targetFragment = targetFragment;
        this.modelFragment = modelFragment;
        this.isTargetSmaller = isTargetSmaller;
        this.shift = shift;
        fragmentComparison = comparison;
    }

    public static FragmentMatch invalidInstance(
            final PdbCompactFragment targetFragment,
            final PdbCompactFragment modelFragment) {
        return new FragmentMatch(targetFragment, modelFragment, false, 0,
                                 FragmentComparison.invalidInstance());
    }

    public final PdbCompactFragment getTargetFragment() {
        return targetFragment;
    }

    public final PdbCompactFragment getModelFragment() {
        return modelFragment;
    }

    public final int getShift() {
        return shift;
    }

    public final List<ResidueComparison> getResidueComparisons() {
        return fragmentComparison.getResidueComparisons();
    }

    public final List<MasterTorsionAngleType> getAngleTypes() {
        return fragmentComparison.getAngleTypes();
    }

    public final int getTargetInvalidCount() {
        return fragmentComparison.getTargetInvalidCount();
    }

    public final int getModelInvalidCount() {
        return fragmentComparison.getModelInvalidCount();
    }

    public final int getBothInvalidCount() {
        return fragmentComparison.getBothInvalidCount();
    }

    public final int getValidCount() {
        return fragmentComparison.getValidCount();
    }

    public final Angle getMeanDelta() {
        return fragmentComparison.getMeanDelta();
    }

    public final int getMismatchCount() {
        return fragmentComparison.getMismatchCount();
    }

    public final int getResidueCount() {
        return fragmentComparison.getResidueCount();
    }

    public final boolean isValid() {
        return fragmentComparison.isValid();
    }

    @Override
    public final String toString() {
        final PdbCompactFragment target;
        final PdbCompactFragment model;

        if (isTargetSmaller) {
            target = targetFragment;
            model = modelFragment
                    .shift(shift, targetFragment.getResidues().size());
        } else {
            target = targetFragment
                    .shift(shift, modelFragment.getResidues().size());
            model = modelFragment;
        }

        return target.getName() + " & " + model.getName();
    }

    public final MoleculeType moleculeType() {
        assert targetFragment.getMoleculeType() ==
               modelFragment.getMoleculeType();
        return targetFragment.getMoleculeType();
    }

    @Override
    public final SVGDocument visualize() {
        return visualize(640, 480);
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
        rangeAxis.setNumberFormatOverride(AngleFormat.createInstance());

        return FragmentMatch
                .plotAsSvg(width, height, dataset, renderer, domainAxis,
                           rangeAxis);
    }

    private void prepareDataset(final DefaultXYDataset dataset,
                                final XYItemRenderer renderer) {
        int i = 0;
        for (final MasterTorsionAngleType angle : fragmentComparison
                .getAngleTypes()) {
            final double[][] data = new double[2][];
            data[0] = new double[fragmentComparison.getResidueCount()];
            data[1] = new double[fragmentComparison.getResidueCount()];

            int j = 0;
            for (final ResidueComparison residue : fragmentComparison
                    .getResidueComparisons()) {
                final TorsionAngleDelta delta = residue.getAngleDelta(angle);
                data[0][j] = j;

                data[1][j] =
                        (delta.getState() == TorsionAngleDelta.State.BOTH_VALID)
                        ? delta.getDelta().getRadians() : Double.NaN;

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

        if (targetFragment.getMoleculeType() == MoleculeType.RNA) {
            try {
                final List<String> ticks = generateLabelsWithDotBracket();
                domainAxis = new TorsionAxis(ticks, 0, 12);
                domainAxis.setLabel("Secondary structure");
            } catch (final InvalidStructureException e) {
                FragmentMatch.LOGGER
                        .warn("Failed to extract canonical secondary structure",
                              e);
            }
        }

        if (domainAxis == null) {
            final List<String> ticks = generateLabelsWithResidueNames();
            domainAxis = new TorsionAxis(ticks, -Math.PI / 4, 6);
            domainAxis.setLabel("ResID");
        }

        return domainAxis;
    }

    private static SVGDocument plotAsSvg(final int width, final int height,
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

        final Rectangle2D boundingBox =
                SVGHelper.calculateBoundingBox(document);
        root.setAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE,
                            String.format("%s %s %s %s", boundingBox.getMinX(),
                                          boundingBox.getMinY(),
                                          boundingBox.getWidth(),
                                          boundingBox.getHeight()));
        root.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE,
                            Double.toString(boundingBox.getWidth()));
        root.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE,
                            Double.toString(boundingBox.getHeight()));
        return document;
    }

    public final List<String> generateLabelsWithDotBracket()
            throws InvalidStructureException {
        final PdbCompactFragment target = isTargetSmaller ? targetFragment
                                                          : targetFragment
                                                  .shift(shift, modelFragment
                                                          .getResidues()
                                                          .size());
        final List<String> result = new ArrayList<>();
        final List<PdbResidue> targetResidues = target.getResidues();
        final BpSeq bpSeq = CanonicalStructureExtractor
                .getCanonicalSecondaryStructure(target);

        final Converter converter = new LevelByLevelConverter(new MinGain(), 0);
        final DotBracket dotBracket = converter.convert(bpSeq);

        for (int i = 0; i < targetResidues.size(); i++) {
            final DotBracketSymbol symbol = dotBracket.getSymbol(i);
            result.add(Character.toString(symbol.getStructure()));
        }

        return result;
    }

    public final List<String> generateLabelsWithResidueNames() {
        final PdbCompactFragment target = isTargetSmaller ? targetFragment
                                                          : targetFragment
                                                  .shift(shift, modelFragment
                                                          .getResidues()
                                                          .size());
        final List<String> result = new ArrayList<>();
        for (final PdbResidue lname : target.getResidues()) {
            result.add(lname.toString());
        }
        return result;
    }

    @Override
    public void visualize3D() {
        // do nothing
    }

    public final SVGDocument visualizePercentiles(final int width,
                                                  final int height) {
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
        rangeAxis.setNumberFormatOverride(AngleFormat.createInstance());

        return FragmentMatch
                .plotAsSvg(width, height, dataset, renderer, domainAxis,
                           rangeAxis);
    }

    private void preparePercentilesDataset(final DefaultXYDataset dataset,
                                           final XYItemRenderer renderer) {
        final String name = modelFragment.getName();
        final double[] percents = SingleMatchStatistics.PERCENTS_FROM_1_TO_100;
        final List<MasterTorsionAngleType> angleTypes =
                fragmentComparison.getAngleTypes();

        for (int j = 0; j < angleTypes.size(); j++) {
            final MasterTorsionAngleType masterType = angleTypes.get(j);
            final AngleDeltaIterator angleDeltaIterator =
                    new TypedDeltaIterator(this, masterType);
            final SingleMatchStatistics statistics = SingleMatchStatistics
                    .calculate(name, angleDeltaIterator, new double[0],
                               percents);

            final double[][] data = new double[2][];
            data[0] = new double[percents.length];
            data[1] = new double[percents.length];

            for (int i = 0; i < percents.length; i++) {
                data[0][i] = percents[i];
                data[1][i] = statistics
                        .getAngleThresholdForGivenPercentile(percents[i]);
            }

            final String displayName = masterType.getLongDisplayName();
            dataset.addSeries(displayName, data);
            renderer.setSeriesPaint(j, Colors.getDistinctColors()[j]);
        }
    }
}
