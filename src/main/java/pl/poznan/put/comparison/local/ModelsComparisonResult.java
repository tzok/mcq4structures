package pl.poznan.put.comparison.local;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.jumpmind.symmetric.csv.CsvWriter;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.analysis.IAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.gui.component.NonEditableDefaultTableModel;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MatchCollection;
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
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.svg.SVGHelper;
import pl.poznan.put.visualisation.AngleDeltaMapper;
import pl.poznan.put.visualisation.ColorMaps;
import pl.poznan.put.visualisation.Surface3D;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;

public class ModelsComparisonResult {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ModelsComparisonResult.class);
    private final PdbCompactFragment target;
    private final List<PdbCompactFragment> models;
    private final List<FragmentMatch> fragmentMatches;

    public ModelsComparisonResult(final PdbCompactFragment target,
                                  final List<PdbCompactFragment> models,
                                  final List<FragmentMatch> fragmentMatches) {
        super();
        this.target = target;
        this.models = new ArrayList<>(models);
        this.fragmentMatches = new ArrayList<>(fragmentMatches);
    }

    public final PdbCompactFragment getTarget() {
        return target;
    }

    public final PdbCompactFragment getModel(final int index) {
        return models.get(index);
    }

    public final int getModelCount() {
        return fragmentMatches.size();
    }

    public final SelectedAngle selectAngle(
            final MasterTorsionAngleType torsionAngle) {
        return new SelectedAngle(torsionAngle);
    }

    public final class SelectedAngle
            implements Exportable, Tabular, Visualizable, MatchCollection {
        private final MasterTorsionAngleType angleType;

        private SelectedAngle(final MasterTorsionAngleType torsionAngle) {
            super();
            angleType = torsionAngle;
        }

        public MasterTorsionAngleType getAngleType() {
            return angleType;
        }

        @Override
        public List<FragmentMatch> getFragmentMatches() {
            return fragmentMatches;
        }

        @Override
        public void export(final OutputStream stream) throws IOException {
            final CsvWriter csvWriter =
                    new CsvWriter(stream, ',', Charset.forName("UTF-8"));
            csvWriter.write(null);

            for (final PdbCompactFragment model : models) {
                csvWriter.write(model.toString());
            }

            csvWriter.endRecord();

            for (int i = 0; i < target.getResidues().size(); i++) {
                final PdbResidue residue = target.getResidues().get(i);
                csvWriter.write(residue.toString());

                for (int j = 0; j < models.size(); j++) {
                    final FragmentMatch fragmentMatch = fragmentMatches.get(j);
                    final ResidueComparison residueComparison =
                            fragmentMatch.getResidueComparisons().get(i);
                    final TorsionAngleDelta delta =
                            residueComparison.getAngleDelta(angleType);
                    csvWriter.write(delta.toExportString());
                }

                csvWriter.endRecord();
            }

            csvWriter.close();
        }

        @Override
        public ExportFormat getExportFormat() {
            return ExportFormat.CSV;
        }

        @Override
        public File suggestName() {
            final SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            final StringBuilder builder =
                    new StringBuilder(sdf.format(new Date()));
            builder.append("-Local-Distance-Multi");

            for (final PdbCompactFragment model : models) {
                builder.append('-');
                builder.append(model);
            }

            builder.append(".csv");
            return new File(builder.toString());
        }

        @Override
        public TableModel asExportableTableModel() {
            return asTableModel(false);
        }

        @Override
        public TableModel asDisplayableTableModel() {
            return asTableModel(true);
        }

        private TableModel asTableModel(final boolean isDisplay) {
            final String[] columnNames = new String[models.size() + 1];
            columnNames[0] = isDisplay ? "" : null;
            for (int i = 0; i < models.size(); i++) {
                columnNames[i + 1] = models.get(i).getName();
            }

            final String[][] data = new String[target.getResidues().size()][];

            for (int i = 0; i < target.getResidues().size(); i++) {
                data[i] = new String[models.size() + 1];
                data[i][0] = target.getResidues().get(i).toString();

                for (int j = 0; j < models.size(); j++) {
                    final FragmentMatch fragmentMatch = fragmentMatches.get(j);
                    final ResidueComparison residueComparison =
                            fragmentMatch.getResidueComparisons().get(i);
                    final TorsionAngleDelta delta =
                            residueComparison.getAngleDelta(angleType);

                    if (delta == null) {
                        data[i][j + 1] = null;
                    } else {
                        data[i][j + 1] = isDisplay ? delta.toDisplayString()
                                                   : delta.toExportString();
                    }
                }
            }

            return new NonEditableDefaultTableModel(data, columnNames);
        }

        public Pair<Double, Double> getMinMax() {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;

            for (final FragmentMatch match : fragmentMatches) {
                for (final ResidueComparison result : match
                        .getResidueComparisons()) {
                    final double delta =
                            result.getAngleDelta(angleType).getDelta()
                                  .getRadians();

                    if (delta < min) {
                        min = delta;
                    }

                    if (delta > max) {
                        max = delta;
                    }
                }
            }

            return Pair.of(min, max);
        }

        @Override
        public SVGDocument visualize() {
            final SVGDocument document = SVGHelper.emptyDocument();
            final SVGGraphics2D svg = new SVGGraphics2D(document);
            svg.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));

            final LineMetrics lineMetrics = SVGHelper.getLineMetrics(svg);

            final float descent = lineMetrics.getDescent();
            final float unitHeight = lineMetrics.getHeight();
            final float unitWidth = (unitHeight * 3.0f) / 4.0f;
            final float maxWidth = drawModelsNames(svg, unitHeight, descent);

            final DotBracket dotBracket = getDotBracketOrNull();
            drawDotBracket(svg, dotBracket, unitWidth, maxWidth,
                           unitHeight - descent);
            drawColorBars(svg, unitWidth, unitHeight, maxWidth);
            drawDotBracket(svg, dotBracket, unitWidth, maxWidth,
                           ((models.size() + 2) * unitHeight) - descent);
            finalizeSvg(document, svg, unitWidth, unitHeight, maxWidth);

            return document;
        }

        private float drawModelsNames(final SVGGraphics2D svg,
                                      final float unitHeight,
                                      final float descent) {
            final FontMetrics metrics = SVGHelper.getFontMetrics(svg);
            float maxWidth = Integer.MIN_VALUE;

            for (int i = 0; i < models.size(); i++) {
                final String modelName = models.get(i).getName();
                svg.drawString(modelName, 0.0f,
                               ((i + 2) * unitHeight) - descent);
                final float width = metrics.stringWidth(modelName);

                if (width > maxWidth) {
                    maxWidth = width;
                }
            }

            return maxWidth;
        }

        private DotBracket getDotBracketOrNull() {
            DotBracket dotBracket = null;
            if (target.getMoleculeType() == MoleculeType.RNA) {
                try {
                    final Converter converter =
                            new LevelByLevelConverter(new MinGain(), 1);
                    final BpSeq bpSeq = CanonicalStructureExtractor
                            .getCanonicalSecondaryStructure(target);
                    dotBracket = converter.convert(bpSeq);
                } catch (final InvalidStructureException e) {
                    ModelsComparisonResult.LOGGER
                            .warn("Failed to extract canonical secondary " +
                                  "structure", e);
                }
            }
            return dotBracket;
        }

        private void drawDotBracket(final SVGGraphics2D svg,
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
                    svg.drawString(s, (leftShift + (i * unitWidth) +
                                       (unitWidth / 2)) - (stringWidth / 2),
                                   topShift);
                }
            }
        }

        private void drawColorBars(final SVGGraphics2D svg,
                                   final float unitWidth,
                                   final float unitHeight,
                                   final float leftShift) {
            for (int i = 0; i < fragmentMatches.size(); i++) {
                final FragmentMatch fragmentMatch = fragmentMatches.get(i);

                for (int j = 0; j < fragmentMatch.getResidueCount(); j++) {
                    final ResidueComparison comparison =
                            fragmentMatch.getResidueComparisons().get(j);
                    final float x = leftShift + (j * unitWidth);
                    final float y = (i + 1) * unitHeight;
                    drawColorBarUnit(svg, comparison, x, y, unitHeight,
                                     unitWidth);
                }
            }
        }

        private void finalizeSvg(final Document document,
                                 final SVGGraphics2D svg, final float unitWidth,
                                 final float unitHeight,
                                 final float leftShift) {
            final Element root = document.getDocumentElement();
            svg.getRoot(root);

            if (!fragmentMatches.isEmpty()) {
                final FragmentMatch fragmentMatch = fragmentMatches.get(0);
                final float width = leftShift + (unitWidth * fragmentMatch
                        .getResidueCount());
                final float height = unitHeight * (models.size() + 3);
                root.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE,
                                    Float.toString(width));
                root.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE,
                                    Float.toString(height));
            }
        }

        private void drawColorBarUnit(final SVGGraphics2D svg,
                                      final ResidueComparison comparison,
                                      final float x, final float y,
                                      final float height, final float width) {
            final TorsionAngleDelta angleDelta =
                    comparison.getAngleDelta(angleType);

            if (angleDelta.getState() == TorsionAngleDelta.State.BOTH_VALID) {
                final double normalized = AngleDeltaMapper
                        .map(angleDelta.getDelta().getDegrees360());
                final Color color = ColorMaps.getVarnaColorMap()
                                             .getColorForValue(normalized);
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
        public void visualize3D() {
            if (models.size() < 1) {
                JOptionPane.showMessageDialog(null,
                                              "At least one model is required" +
                                              " for 3D visualization", "Error",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                final String name = String.format("%s %s", target.getName(),
                                                  angleType.getExportName());
                final double[][] matrix = prepareMatrix();
                final List<String> ticksX = prepareTicksX();
                final List<String> ticksY = prepareTicksY();
                final NavigableMap<Double, String> valueTickZ =
                        MCQLocalResult.prepareTicksZ();
                final String labelX = "Model";
                final String labelY = "Residue";
                final String labelZ = "Distance";
                final boolean showAllTicksX = true;
                final boolean showAllTicksY = false;

                final IAnalysis surface3d =
                        new Surface3D(name, matrix, ticksX, ticksY, valueTickZ,
                                      labelX, labelY, labelZ, true, false);
                AnalysisLauncher.open(surface3d);
            } catch (final Exception e) {
                final String message = "Failed to visualize in 3D";
                ModelsComparisonResult.LOGGER.error(message, e);
                JOptionPane.showMessageDialog(null, message, "Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }

        private double[][] prepareMatrix() {
            final int size = target.getResidues().size();
            final double[][] matrix = new double[models.size()][];

            for (int i = 0; i < models.size(); i++) {
                matrix[i] = new double[size];
                final FragmentMatch fragmentMatch = fragmentMatches.get(i);
                final List<ResidueComparison> residueComparisons =
                        fragmentMatch.getResidueComparisons();

                for (int j = 0; j < size; j++) {
                    final ResidueComparison residueComparison =
                            residueComparisons.get(j);
                    matrix[i][j] = residueComparison.getAngleDelta(angleType)
                                                    .getDelta().getRadians();
                }
            }

            return matrix;
        }

        private List<String> prepareTicksX() {
            final List<String> ticksX = new ArrayList<>();
            for (final PdbCompactFragment model : models) {
                ticksX.add(model.getName());
            }
            return ticksX;
        }

        private List<String> prepareTicksY() {
            final List<String> ticksY = new ArrayList<>();
            for (final PdbResidue residue : target.getResidues()) {
                ticksY.add(residue.toString());
            }
            return ticksY;
        }
    }
}
