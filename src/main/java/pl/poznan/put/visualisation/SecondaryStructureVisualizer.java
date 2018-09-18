package pl.poznan.put.visualisation;

import fr.orsay.lri.varna.exceptions.ExceptionFileFormatOrSyntax;
import fr.orsay.lri.varna.exceptions.ExceptionNAViewAlgorithm;
import fr.orsay.lri.varna.exceptions.ExceptionUnmatchedClosingParentheses;
import fr.orsay.lri.varna.exceptions.ExceptionWritingForbidden;
import fr.orsay.lri.varna.models.VARNAConfig;
import fr.orsay.lri.varna.models.rna.ModeleColorMap;
import fr.orsay.lri.varna.models.rna.RNA;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
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
import pl.poznan.put.structure.secondary.formats.Converter;
import pl.poznan.put.structure.secondary.formats.DotBracket;
import pl.poznan.put.structure.secondary.formats.InvalidStructureException;
import pl.poznan.put.structure.secondary.formats.LevelByLevelConverter;
import pl.poznan.put.structure.secondary.pseudoknots.elimination.MinGain;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.utility.svg.SVGHelper;

public final class SecondaryStructureVisualizer {
  private static final Logger LOGGER = LoggerFactory.getLogger(SecondaryStructureVisualizer.class);
  private static final Converter CONVERTER = new LevelByLevelConverter(new MinGain(), 1);

  private SecondaryStructureVisualizer() {
    super();
  }

  public static SVGDocument visualize(
      final FragmentMatch fragmentMatch, final ComparisonMapper mapper) {
    try {
      final List<ResidueComparison> residueComparisons = fragmentMatch.getResidueComparisons();
      final List<MasterTorsionAngleType> angleTypes = fragmentMatch.getAngleTypes();
      final Double[] mapped = mapper.map(residueComparisons, angleTypes);

      final DotBracket dotBracket = SecondaryStructureVisualizer.getTargetDotBracket(fragmentMatch);
      return SecondaryStructureVisualizer.visualize(dotBracket, mapped);
    } catch (final InvalidStructureException e) {
      SecondaryStructureVisualizer.LOGGER.error(
          "Failed to extract canonical secondary structure", e);
      return SVGHelper.emptyDocument();
    }
  }

  private static DotBracket getTargetDotBracket(final FragmentMatch fragmentMatch)
      throws InvalidStructureException {
    final PdbCompactFragment target = fragmentMatch.getTargetFragment();
    final BpSeq bpSeq = CanonicalStructureExtractor.getCanonicalSecondaryStructure(target);
    return SecondaryStructureVisualizer.CONVERTER.convert(bpSeq);
  }

  public static SVGDocument visualize(final DotBracket dotBracket, final Double[] mapped) {
    File tempFile = null;

    try {
      tempFile = File.createTempFile("mcq4structures", ".svg");

      final ModeleColorMap modelColorMap = ColorMaps.getVarnaColorMap();

      final VARNAConfig config = new VARNAConfig();
      config._cm = modelColorMap;
      config._drawColorMap = true;

      final RNA rna = new RNA();
      rna.setRNA(dotBracket.getSequence(), dotBracket.getStructure());
      rna.setColorMapValues(mapped, modelColorMap);
      rna.drawRNANAView(config);
      rna.saveRNASVG(tempFile.getAbsolutePath(), config);

      final SVGDocument svgDocument = SVGHelper.fromFile(tempFile);
      final SVGSVGElement root = svgDocument.getRootElement();

      final Rectangle2D boundingBox = SVGHelper.calculateBoundingBox(svgDocument);
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

      return svgDocument;
    } catch (ExceptionUnmatchedClosingParentheses
        | ExceptionFileFormatOrSyntax
        | ExceptionWritingForbidden
        | ExceptionNAViewAlgorithm
        | IOException e) {
      SecondaryStructureVisualizer.LOGGER.error(
          "Failed to visualize secondary structure:\n{}", dotBracket, e);
    } finally {
      FileUtils.deleteQuietly(tempFile);
    }

    return SVGHelper.emptyDocument();
  }
}
