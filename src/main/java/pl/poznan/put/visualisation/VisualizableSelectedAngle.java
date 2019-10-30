package pl.poznan.put.visualisation;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.NavigableMap;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.math3.util.FastMath;
import org.jcolorbrewer.ColorBrewer;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.analysis.IAnalysis;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.comparison.local.SelectedAngle;
import pl.poznan.put.comparison.mapping.AngleDeltaMapper;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.ResidueComparison;
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
import pl.poznan.put.utility.svg.SVGHelper;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class VisualizableSelectedAngle extends SelectedAngle implements Visualizable {
  public VisualizableSelectedAngle(final SelectedAngle selectedAngle) {
    super(
        selectedAngle.getAngleType(),
        selectedAngle.getTarget(),
        selectedAngle.getModels(),
        selectedAngle.getFragmentMatches());
  }

  @Override
  public final SVGDocument visualize() {
    final SVGDocument document = SVGHelper.emptyDocument();
    final SVGGraphics2D svg = new SVGGraphics2D(document);
    svg.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));

    final LineMetrics lineMetrics = SVGHelper.getLineMetrics(svg);

    final float descent = lineMetrics.getDescent();
    final float unitHeight = lineMetrics.getHeight();
    final float unitWidth = (unitHeight * 3.0f) / 4.0f;
    final float maxWidth = drawModelsNames(svg, unitHeight, descent);

    final DotBracket dotBracket = getDotBracketOrNull();
    VisualizableSelectedAngle.drawDotBracket(
        svg, dotBracket, unitWidth, maxWidth, unitHeight - descent);
    drawColorBars(svg, unitWidth, unitHeight, maxWidth);
    VisualizableSelectedAngle.drawDotBracket(
        svg, dotBracket, unitWidth, maxWidth, ((getModels().size() + 2) * unitHeight) - descent);
    finalizeSvg(document, svg, unitWidth, unitHeight, maxWidth);

    return document;
  }

  private float drawModelsNames(
      final SVGGraphics2D svg, final float unitHeight, final float descent) {
    final List<PdbCompactFragment> models = getModels();
    final FontMetrics metrics = SVGHelper.getFontMetrics(svg);
    float maxWidth = Integer.MIN_VALUE;

    for (int i = 0; i < models.size(); i++) {
      final String modelName = models.get(i).getName();
      svg.drawString(modelName, 0.0f, ((i + 2) * unitHeight) - descent);
      final float width = metrics.stringWidth(modelName);

      if (width > maxWidth) {
        maxWidth = width;
      }
    }

    return maxWidth;
  }

  private DotBracket getDotBracketOrNull() {
    final PdbCompactFragment target = getTarget();
    DotBracket dotBracket = null;

    if (target.getMoleculeType() == MoleculeType.RNA) {
      try {
        final Converter converter = new LevelByLevelConverter(new MinGain(), 1);
        final BpSeq bpSeq = CanonicalStructureExtractor.bpSeq(target);
        dotBracket = converter.convert(bpSeq);
      } catch (final InvalidStructureException e) {
        VisualizableSelectedAngle.log.warn("Failed to extract canonical secondary structure", e);
      }
    }
    return dotBracket;
  }

  private static void drawDotBracket(
      final SVGGraphics2D svg,
      final DotBracket dotBracket,
      final float unitWidth,
      final float leftShift,
      final float topShift) {
    if (dotBracket != null) {
      final FontMetrics metrics = SVGHelper.getFontMetrics(svg);

      for (int i = 0; i < dotBracket.getLength(); i++) {
        final DotBracketSymbol symbol = dotBracket.getSymbol(i);
        final String s = Character.toString(symbol.getStructure());
        final float stringWidth = metrics.stringWidth(s);
        svg.drawString(
            s, (leftShift + (i * unitWidth) + (unitWidth / 2.0F)) - (stringWidth / 2.0F), topShift);
      }
    }
  }

  private void drawColorBars(
      final SVGGraphics2D svg,
      final float unitWidth,
      final float unitHeight,
      final float leftShift) {
    final List<FragmentMatch> fragmentMatches = getFragmentMatches();

    for (int i = 0; i < fragmentMatches.size(); i++) {
      final FragmentMatch fragmentMatch = fragmentMatches.get(i);

      for (int j = 0; j < fragmentMatch.getResidueCount(); j++) {
        final ResidueComparison comparison = fragmentMatch.getResidueComparisons().get(j);
        final float x = leftShift + (j * unitWidth);
        final float y = (i + 1) * unitHeight;
        drawColorBarUnit(svg, comparison, x, y, unitHeight, unitWidth);
      }
    }
  }

  private void finalizeSvg(
      final Document document,
      final SVGGraphics2D svg,
      final float unitWidth,
      final float unitHeight,
      final float leftShift) {
    final Element root = document.getDocumentElement();
    svg.getRoot(root);

    final List<FragmentMatch> fragmentMatches = getFragmentMatches();

    if (!fragmentMatches.isEmpty()) {
      final FragmentMatch fragmentMatch = fragmentMatches.get(0);
      final float width = leftShift + (unitWidth * fragmentMatch.getResidueCount());
      final float height = unitHeight * (getModels().size() + 3);
      root.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, Float.toString(width));
      root.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, Float.toString(height));
    }
  }

  private void drawColorBarUnit(
      final SVGGraphics2D svg,
      final ResidueComparison comparison,
      final float x,
      final float y,
      final float height,
      final float width) {
    final TorsionAngleDelta angleDelta = comparison.getAngleDelta(getAngleType());

    if (angleDelta.getState() == TorsionAngleDelta.State.BOTH_VALID) {
      final double normalized = AngleDeltaMapper.map(angleDelta.getDelta().getDegrees360());
      final Color[] colors = ColorBrewer.YlOrRd.getColorPalette(4);
      final Color color = colors[(int) FastMath.floor(normalized * 4.0)];
      svg.setColor(color);
    } else {
      svg.setColor(Color.BLACK);
    }

    final Shape shape = new Rectangle2D.Float(x, y, width, height);
    svg.fill(shape);
    svg.setColor(Color.BLACK);
    svg.draw(shape);
  }

  @Override
  public final void visualize3D() {
    final PdbCompactFragment target = getTarget();
    final List<PdbCompactFragment> models = getModels();
    final MasterTorsionAngleType angleType = getAngleType();

    if (models.size() < 1) {
      JOptionPane.showMessageDialog(
          null,
          "At least one model is required for 3D visualization",
          "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      final String name = String.format("%s %s", target.getName(), angleType.getExportName());
      final double[][] matrix = prepareMatrix();
      final List<String> ticksX = prepareTicksX();
      final List<String> ticksY = prepareTicksY();
      final NavigableMap<Double, String> valueTickZ = VisualizableMCQLocalResult.prepareTicksZ();
      final String labelX = "Model";
      final String labelY = "Residue";
      final String labelZ = "Distance";
      final boolean showAllTicksX = true;
      final boolean showAllTicksY = false;

      final IAnalysis surface3d =
          new Surface3D(
              name, matrix, ticksX, ticksY, valueTickZ, labelX, labelY, labelZ, true, false);
      AnalysisLauncher.open(surface3d);
    } catch (final Exception e) {
      final String message = "Failed to visualize in 3D";
      VisualizableSelectedAngle.log.error(message, e);
      JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private double[][] prepareMatrix() {
    final PdbCompactFragment target = getTarget();
    final List<PdbCompactFragment> models = getModels();
    final List<FragmentMatch> fragmentMatches = getFragmentMatches();
    final MasterTorsionAngleType angleType = getAngleType();

    final int size = target.getResidues().size();
    final double[][] matrix = new double[models.size()][];

    for (int i = 0; i < models.size(); i++) {
      matrix[i] = new double[size];
      final FragmentMatch fragmentMatch = fragmentMatches.get(i);
      final List<ResidueComparison> residueComparisons = fragmentMatch.getResidueComparisons();

      for (int j = 0; j < size; j++) {
        final ResidueComparison residueComparison = residueComparisons.get(j);
        matrix[i][j] = residueComparison.getAngleDelta(angleType).getDelta().getRadians();
      }
    }

    return matrix;
  }

  private List<String> prepareTicksX() {
      return getModels().stream().map(PdbCompactFragment::getName).collect(Collectors.toList());
  }

  private List<String> prepareTicksY() {
      return getTarget().getResidues().stream().map(PdbResidue::toString).collect(Collectors.toList());
  }
}
