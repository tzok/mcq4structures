package pl.poznan.put.visualisation;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.util.SVGConstants;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.structure.secondary.CanonicalStructureExtractor;
import pl.poznan.put.structure.secondary.formats.BpSeq;
import pl.poznan.put.structure.secondary.formats.DotBracket;
import pl.poznan.put.structure.secondary.formats.InvalidSecondaryStructureException;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.utility.svg.SVGHelper;
import fr.orsay.lri.varna.exceptions.ExceptionFileFormatOrSyntax;
import fr.orsay.lri.varna.exceptions.ExceptionUnmatchedClosingParentheses;
import fr.orsay.lri.varna.exceptions.ExceptionWritingForbidden;
import fr.orsay.lri.varna.models.VARNAConfig;
import fr.orsay.lri.varna.models.rna.ModeleColorMap;
import fr.orsay.lri.varna.models.rna.RNA;

public class SecondaryStructureVisualizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecondaryStructureVisualizer.class);

    public static SVGDocument visualize(FragmentMatch fragmentMatch) {
        DotBracket dotBracket;
        try {
            PdbCompactFragment target = fragmentMatch.getTargetFragment();
            BpSeq bpSeq = CanonicalStructureExtractor.getCanonicalSecondaryStructure(target);
            dotBracket = DotBracket.fromBpSeq(bpSeq);
        } catch (InvalidSecondaryStructureException e) {
            SecondaryStructureVisualizer.LOGGER.error("Failed to extract canonical secondary structure", e);
            return SVGHelper.emptyDocument();
        }

        List<Double> angleDeltas = new ArrayList<>();
        List<ResidueComparison> residueComparisons = fragmentMatch.getResidueComparisons();

        for (ResidueComparison residueComparison : residueComparisons) {
            angleDeltas.add(residueComparison.getMeanDirection().getRadians());
        }

        Double[] angleDeltasArray = angleDeltas.toArray(new Double[angleDeltas.size()]);
        return SecondaryStructureVisualizer.visualize(dotBracket, angleDeltasArray);
    }

    public static SVGDocument visualize(FragmentMatch fragmentMatch,
            MasterTorsionAngleType masterType) {
        DotBracket dotBracket;
        try {
            PdbCompactFragment target = fragmentMatch.getTargetFragment();
            BpSeq bpSeq = CanonicalStructureExtractor.getCanonicalSecondaryStructure(target);
            dotBracket = DotBracket.fromBpSeq(bpSeq);
        } catch (InvalidSecondaryStructureException e) {
            SecondaryStructureVisualizer.LOGGER.error("Failed to extract canonical secondary structure", e);
            return SVGHelper.emptyDocument();
        }

        List<Double> angleDeltas = new ArrayList<>();
        List<ResidueComparison> residueComparisons = fragmentMatch.getResidueComparisons();

        for (ResidueComparison residueComparison : residueComparisons) {
            TorsionAngleDelta angleDelta = residueComparison.getAngleDelta(masterType);
            angleDeltas.add(angleDelta.getDelta().getRadians());
        }

        Double[] angleDeltasArray = angleDeltas.toArray(new Double[angleDeltas.size()]);
        return SecondaryStructureVisualizer.visualize(dotBracket, angleDeltasArray);
    }

    public static SVGDocument visualize(DotBracket dotBracket,
            Double[] angleDeltas) {
        File tempFile = null;

        try {
            tempFile = File.createTempFile("mcq4structures", ".svg");

            ModeleColorMap modelColorMap = ColorMapWrapper.getVarnaColorMap(0, Math.PI);

            VARNAConfig config = new VARNAConfig();
            config._cm = modelColorMap;
            config._drawColorMap = true;

            RNA rna = new RNA();
            rna.setRNA(dotBracket.getSequence(), dotBracket.getStructure());
            rna.setColorMapValues(angleDeltas, modelColorMap);
            rna.drawRNARadiate(config);
            rna.saveRNASVG(tempFile.getAbsolutePath(), config);

            SVGDocument svgDocument = SVGHelper.fromFile(tempFile);
            SVGSVGElement root = svgDocument.getRootElement();

            Rectangle2D boundingBox = SVGHelper.calculateBoundingBox(svgDocument);
            root.setAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, boundingBox.getMinX() + " " + boundingBox.getMinY() + " " + boundingBox.getWidth() + " " + boundingBox.getHeight());
            root.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, Double.toString(boundingBox.getWidth()));
            root.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, Double.toString(boundingBox.getHeight()));

            return svgDocument;
        } catch (ExceptionUnmatchedClosingParentheses | ExceptionFileFormatOrSyntax | ExceptionWritingForbidden | IOException e) {
            SecondaryStructureVisualizer.LOGGER.error("Failed to visualize secondary structure:\n" + dotBracket, e);
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }

        return SVGHelper.emptyDocument();
    }

    private SecondaryStructureVisualizer() {
        // empty constructor
    }
}
