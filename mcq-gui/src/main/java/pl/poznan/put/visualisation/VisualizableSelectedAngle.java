package pl.poznan.put.visualisation;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.math3.util.FastMath;
import org.jcolorbrewer.ColorBrewer;
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
import pl.poznan.put.structure.CanonicalStructureExtractor;
import pl.poznan.put.structure.DotBracketSymbol;
import pl.poznan.put.structure.formats.BpSeq;
import pl.poznan.put.structure.formats.Converter;
import pl.poznan.put.structure.formats.DotBracket;
import pl.poznan.put.structure.formats.ImmutableDefaultConverter;
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

  private static void drawDotBracket(
      final SVGGraphics2D svg,
      final DotBracket dotBracket,
      final float unitWidth,
      final float leftShift,
      final float topShift) {
    if (dotBracket != null) {
      final FontMetrics metrics = svg.getFontMetrics();

      for (int i = 0; i < dotBracket.length(); i++) {
        final DotBracketSymbol symbol = dotBracket.symbols().get(i);
        final String s = Character.toString(symbol.structure());
        final float stringWidth = metrics.stringWidth(s);
        svg.drawString(
            s, (leftShift + (i * unitWidth) + (unitWidth / 2.0F)) - (stringWidth / 2.0F), topShift);
      }
    }
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

  @Override
  public void visualize3D() {}

  private float drawModelsNames(
      final SVGGraphics2D svg, final float unitHeight, final float descent) {
    final List<PdbCompactFragment> models = getModels();
    final FontMetrics metrics = svg.getFontMetrics();
    float maxWidth = Integer.MIN_VALUE;

    for (int i = 0; i < models.size(); i++) {
      final String modelName = models.get(i).name();
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

    if (target.moleculeType() == MoleculeType.RNA) {
      final Converter converter = ImmutableDefaultConverter.of();
      final BpSeq bpSeq = CanonicalStructureExtractor.bpSeq(target);
      dotBracket = converter.convert(bpSeq);
    }
    return dotBracket;
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
    final TorsionAngleDelta angleDelta = comparison.angleDelta(getAngleType());

    if (angleDelta.state() == TorsionAngleDelta.State.BOTH_VALID) {
      final double normalized = AngleDeltaMapper.map(angleDelta.delta().degrees360());
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

  private double[][] prepareMatrix() {
    final PdbCompactFragment target = getTarget();
    final List<PdbCompactFragment> models = getModels();
    final List<FragmentMatch> fragmentMatches = getFragmentMatches();
    final MasterTorsionAngleType angleType = getAngleType();

    final int size = target.residues().size();
    final double[][] matrix = new double[models.size()][];

    for (int i = 0; i < models.size(); i++) {
      matrix[i] = new double[size];
      final FragmentMatch fragmentMatch = fragmentMatches.get(i);
      final List<ResidueComparison> residueComparisons = fragmentMatch.getResidueComparisons();

      for (int j = 0; j < size; j++) {
        final ResidueComparison residueComparison = residueComparisons.get(j);
        matrix[i][j] = residueComparison.angleDelta(angleType).delta().radians();
      }
    }

    return matrix;
  }

  private List<String> prepareTicksX() {
    return getModels().stream().map(PdbCompactFragment::name).collect(Collectors.toList());
  }

  private List<String> prepareTicksY() {
    return getTarget().residues().stream().map(PdbResidue::toString).collect(Collectors.toList());
  }
}
