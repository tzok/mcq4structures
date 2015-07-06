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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.constant.Colors;
import pl.poznan.put.interfaces.Visualizable;
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

    public List<String> generateLabels() throws InvalidCircularValueException {
        try {
            return generateLabelsWithDotBracket();
        } catch (InvalidSecondaryStructureException e) {
            FragmentMatch.LOGGER.warn("Failed to extract canonical secondary structure", e);
        }
        return generateLabelsWithNameOnly();
    }

    public List<String> generateLabelsWithNameOnly() {
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

        List<String> ticks = generateLabels();
        ValueAxis domainAxis = new TorsionAxis(ticks);
        domainAxis.setLabel("Secondary structure");

        NumberAxis rangeAxis = new NumberAxis();
        rangeAxis.setLabel("Angular distance");
        rangeAxis.setRange(0, Math.PI);
        rangeAxis.setTickUnit(new NumberTickUnit(Math.PI / 12.0));
        rangeAxis.setNumberFormatOverride(AngleFormat.createInstance());

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
}
