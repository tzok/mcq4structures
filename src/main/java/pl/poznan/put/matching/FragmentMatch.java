package pl.poznan.put.matching;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

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
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.constant.Colors;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.stats.MatchStatistics;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.structure.secondary.CanonicalStructureExtractor;
import pl.poznan.put.structure.secondary.DotBracketSymbol;
import pl.poznan.put.structure.secondary.formats.BpSeq;
import pl.poznan.put.structure.secondary.formats.DotBracket;
import pl.poznan.put.structure.secondary.formats.InvalidSecondaryStructureException;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.AngleFormat;
import pl.poznan.put.utility.svg.SVGHelper;
import pl.poznan.put.visualisation.TorsionAxis;

public class FragmentMatch implements Visualizable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentMatch.class);

    public static FragmentMatch invalidInstance(
            PdbCompactFragment targetFragment, PdbCompactFragment modelFragment) {
        return new FragmentMatch(targetFragment, modelFragment, false, 0, FragmentComparison.invalidInstance());
    }

    private final PdbCompactFragment targetFragment;
    private final PdbCompactFragment modelFragment;
    private final boolean isTargetSmaller;
    private final int shift;
    private final FragmentComparison fragmentComparison;

    public FragmentMatch(PdbCompactFragment targetFragment,
            PdbCompactFragment modelFragment, boolean isTargetSmaller,
            int shift, FragmentComparison comparison) {
        super();
        this.targetFragment = targetFragment;
        this.modelFragment = modelFragment;
        this.isTargetSmaller = isTargetSmaller;
        this.shift = shift;
        fragmentComparison = comparison;
    }

    public PdbCompactFragment getTargetFragment() {
        return targetFragment;
    }

    public PdbCompactFragment getModelFragment() {
        return modelFragment;
    }

    public boolean isTargetSmaller() {
        return isTargetSmaller;
    }

    public int getShift() {
        return shift;
    }

    public List<ResidueComparison> getResidueComparisons() {
        return fragmentComparison.getResidueComparisons();
    }

    public List<MasterTorsionAngleType> getAngleTypes() {
        return fragmentComparison.getAngleTypes();
    }

    public int getTargetInvalidCount() {
        return fragmentComparison.getTargetInvalidCount();
    }

    public int getModelInvalidCount() {
        return fragmentComparison.getModelInvalidCount();
    }

    public int getBothInvalidCount() {
        return fragmentComparison.getBothInvalidCount();
    }

    public int getValidCount() {
        return fragmentComparison.getValidCount();
    }

    public Angle getMeanDelta() {
        return fragmentComparison.getMeanDelta();
    }

    public int getMismatchCount() {
        return fragmentComparison.getMismatchCount();
    }

    public int size() {
        return fragmentComparison.size();
    }

    public boolean isValid() {
        return fragmentComparison.isValid();
    }

    @Override
    public String toString() {
        PdbCompactFragment target;
        PdbCompactFragment model;

        if (isTargetSmaller) {
            target = targetFragment;
            model = modelFragment.shift(shift, targetFragment.size());
        } else {
            target = targetFragment.shift(shift, modelFragment.size());
            model = modelFragment;
        }

        return target.getName() + " & " + model.getName();
    }

    public MoleculeType moleculeType() {
        assert targetFragment.getMoleculeType() == modelFragment.getMoleculeType();
        return targetFragment.getMoleculeType();
    }

    public List<String> generateLabelsWithResidueNames() {
        PdbCompactFragment target = isTargetSmaller ? targetFragment : targetFragment.shift(shift, modelFragment.size());
        List<String> result = new ArrayList<>();
        List<PdbResidue> targetResidues = target.getResidues();

        for (int i = 0; i < targetResidues.size(); i++) {
            PdbResidue lname = targetResidues.get(i);
            result.add(lname.toString());
        }

        return result;
    }

    public List<String> generateLabelsWithDotBracket() throws InvalidSecondaryStructureException {
        PdbCompactFragment target = isTargetSmaller ? targetFragment : targetFragment.shift(shift, modelFragment.size());
        List<String> result = new ArrayList<>();
        List<PdbResidue> targetResidues = target.getResidues();
        BpSeq bpSeq = CanonicalStructureExtractor.getCanonicalSecondaryStructure(target);
        DotBracket dotBracket = DotBracket.fromBpSeq(bpSeq);

        for (int i = 0; i < targetResidues.size(); i++) {
            DotBracketSymbol symbol = dotBracket.getSymbol(i);
            result.add(Character.toString(symbol.getStructure()));
        }

        return result;
    }

    @Override
    public SVGDocument visualize() {
        return visualize(640, 480);
    }

    public SVGDocument visualize(int width, int height) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        XYItemRenderer renderer = new DefaultXYItemRenderer();

        prepareDataset(dataset, renderer);

        ValueAxis domainAxis = prepareDomainAxis();
        NumberAxis rangeAxis = new NumberAxis();
        rangeAxis.setLabel("Angular distance");
        rangeAxis.setRange(0, Math.PI);
        rangeAxis.setTickUnit(new NumberTickUnit(Math.PI / 12.0));
        rangeAxis.setNumberFormatOverride(AngleFormat.createInstance());

        return plotAsSvg(width, height, dataset, renderer, domainAxis, rangeAxis);
    }

    private ValueAxis prepareDomainAxis() {
        ValueAxis domainAxis = null;

        if (targetFragment.getMoleculeType() == MoleculeType.RNA) {
            try {
                List<String> ticks = generateLabelsWithDotBracket();
                domainAxis = new TorsionAxis(ticks, 0, 12);
                domainAxis.setLabel("Secondary structure");
            } catch (InvalidSecondaryStructureException e) {
                FragmentMatch.LOGGER.warn("Failed to extract canonical secondary structure", e);
            }
        }

        if (domainAxis == null) {
            List<String> ticks = generateLabelsWithResidueNames();
            domainAxis = new TorsionAxis(ticks, -Math.PI / 4, 6);
            domainAxis.setLabel("ResID");
        }

        return domainAxis;
    }

    private void prepareDataset(DefaultXYDataset dataset,
            XYItemRenderer renderer) {
        int i = 0;
        for (MasterTorsionAngleType angle : fragmentComparison.getAngleTypes()) {
            double[][] data = new double[2][];
            data[0] = new double[fragmentComparison.size()];
            data[1] = new double[fragmentComparison.size()];

            int j = 0;
            for (ResidueComparison residue : fragmentComparison.getResidueComparisons()) {
                TorsionAngleDelta delta = residue.getAngleDelta(angle);
                data[0][j] = j;

                if (delta.getState() == TorsionAngleDelta.State.BOTH_VALID) {
                    data[1][j] = delta.getDelta().getRadians();
                } else {
                    data[1][j] = Double.NaN;
                }

                j++;
            }

            String displayName = angle.getLongDisplayName();
            dataset.addSeries(displayName, data);
            renderer.setSeriesPaint(i, Colors.DISTINCT_COLORS[i]);
            i++;
        }
    }

    private static SVGDocument plotAsSvg(int width, int height,
            XYDataset dataset, XYItemRenderer renderer, ValueAxis domainAxis,
            ValueAxis rangeAxis) {
        Plot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        JFreeChart chart = new JFreeChart(plot);

        SVGDocument document = SVGHelper.emptyDocument();
        SVGGraphics2D graphics = new SVGGraphics2D(document);
        graphics.setSVGCanvasSize(new Dimension(width, height));
        chart.draw(graphics, new Rectangle(width, height));

        SVGSVGElement root = document.getRootElement();
        graphics.getRoot(root);

        Rectangle2D boundingBox = SVGHelper.calculateBoundingBox(document);
        root.setAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, boundingBox.getMinX() + " " + boundingBox.getMinY() + " " + boundingBox.getWidth() + " " + boundingBox.getHeight());
        root.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, Double.toString(boundingBox.getWidth()));
        root.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, Double.toString(boundingBox.getHeight()));
        return document;
    }

    @Override
    public void visualize3D() {
        // do nothing
    }

    public SVGDocument visualizePercentiles(int width, int height) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        XYItemRenderer renderer = new DefaultXYItemRenderer();
        preparePercentilesDataset(dataset, renderer);

        NumberAxis domainAxis = new NumberAxis();
        domainAxis.setLabel("Percentile");
        domainAxis.setRange(0, 100);

        NumberAxis rangeAxis = new NumberAxis();
        rangeAxis.setLabel("Angular distance");
        rangeAxis.setRange(0, Math.PI);
        rangeAxis.setTickUnit(new NumberTickUnit(Math.PI / 12.0));
        rangeAxis.setNumberFormatOverride(AngleFormat.createInstance());

        return plotAsSvg(width, height, dataset, renderer, domainAxis, rangeAxis);
    }

    private void preparePercentilesDataset(DefaultXYDataset dataset,
            XYItemRenderer renderer) {
        double[] percents = MatchStatistics.PERCENTS_FROM_1_TO_100;
        List<MasterTorsionAngleType> angleTypes = fragmentComparison.getAngleTypes();

        for (int j = 0; j < angleTypes.size(); j++) {
            MasterTorsionAngleType masterType = angleTypes.get(j);
            MatchStatistics statistics = MatchStatistics.calculate(this, masterType, new double[0], percents);

            double[][] data = new double[2][];
            data[0] = new double[percents.length];
            data[1] = new double[percents.length];

            for (int i = 0; i < percents.length; i++) {
                data[0][i] = percents[i];
                data[1][i] = statistics.getAngleThresholdForGivenPercentile(percents[i]);
            }

            String displayName = masterType.getLongDisplayName();
            dataset.addSeries(displayName, data);
            renderer.setSeriesPaint(j, Colors.DISTINCT_COLORS[j]);
        }
    }
}
