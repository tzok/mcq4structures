package pl.poznan.put.comparison.local;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.font.LineMetrics;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NavigableMap;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.jumpmind.symmetric.csv.CsvWriter;
import org.jzy3d.analysis.AnalysisLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import pl.poznan.put.structure.secondary.formats.DotBracket;
import pl.poznan.put.structure.secondary.formats.InvalidSecondaryStructureException;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.torsion.TorsionAngleDelta;
import pl.poznan.put.torsion.TorsionAngleDelta.State;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.svg.SVGHelper;
import pl.poznan.put.visualisation.ColorMapWrapper;
import pl.poznan.put.visualisation.Surface3D;

public class ModelsComparisonResult {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelsComparisonResult.class);

    public class SelectedAngle implements Exportable, Tabular, Visualizable, MatchCollection {
        private final MasterTorsionAngleType angleType;

        private SelectedAngle(MasterTorsionAngleType torsionAngle) {
            super();
            this.angleType = torsionAngle;
        }

        public MasterTorsionAngleType getAngleType() {
            return angleType;
        }

        @Override
        public List<FragmentMatch> getFragmentMatches() {
            return fragmentMatches;
        }

        @Override
        public void export(OutputStream stream) throws IOException {
            CsvWriter csvWriter = new CsvWriter(stream, ',', Charset.forName("UTF-8"));
            csvWriter.write(null);

            for (PdbCompactFragment model : models) {
                csvWriter.write(model.toString());
            }

            csvWriter.endRecord();

            for (int i = 0; i < target.size(); i++) {
                PdbResidue residue = target.getResidues().get(i);
                csvWriter.write(residue.toString());

                for (int j = 0; j < models.size(); j++) {
                    FragmentMatch fragmentMatch = fragmentMatches.get(j);
                    ResidueComparison residueComparison = fragmentMatch.getResidueComparisons().get(i);
                    TorsionAngleDelta delta = residueComparison.getAngleDelta(angleType);
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            StringBuilder builder = new StringBuilder(sdf.format(new Date()));
            builder.append("-Local-Distance-Multi");

            for (PdbCompactFragment model : models) {
                builder.append('-');
                builder.append(model.toString());
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

        private TableModel asTableModel(boolean isDisplay) {
            String[] columnNames = new String[models.size() + 1];
            columnNames[0] = isDisplay ? "" : null;
            for (int i = 0; i < models.size(); i++) {
                columnNames[i + 1] = models.get(i).getName();
            }

            String[][] data = new String[target.size()][];

            for (int i = 0; i < target.size(); i++) {
                data[i] = new String[models.size() + 1];
                data[i][0] = target.getResidues().get(i).toString();

                for (int j = 0; j < models.size(); j++) {
                    FragmentMatch fragmentMatch = fragmentMatches.get(j);
                    ResidueComparison residueComparison = fragmentMatch.getResidueComparisons().get(i);
                    TorsionAngleDelta delta = residueComparison.getAngleDelta(angleType);

                    if (delta == null) {
                        data[i][j + 1] = null;
                    } else {
                        data[i][j + 1] = isDisplay ? delta.toDisplayString() : delta.toExportString();
                    }
                }
            }

            return new NonEditableDefaultTableModel(data, columnNames);
        }

        public Pair<Double, Double> getMinMax() {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;

            for (FragmentMatch match : fragmentMatches) {
                for (ResidueComparison result : match.getResidueComparisons()) {
                    double delta = result.getAngleDelta(angleType).getDelta().getRadians();

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
            SVGDocument document = SVGHelper.emptyDocument();
            SVGGraphics2D svg = new SVGGraphics2D(document);
            LineMetrics lineMetrics = SVGHelper.getLineMetrics(svg);

            int unitHeight = (int) Math.ceil(lineMetrics.getHeight());
            int unitWidth = unitHeight * 4 / 3;
            int maxWidth = drawModelsNames(svg, unitHeight);

            DotBracket dotBracket = getDotBracketOrNull();
            drawDotBracket(svg, dotBracket, unitWidth, maxWidth, unitHeight - unitHeight / 6);
            drawColorBars(svg, unitHeight, unitWidth, maxWidth);
            drawDotBracket(svg, dotBracket, unitWidth, maxWidth, (models.size() + 2) * unitHeight);
            finalizeSvg(document, svg, unitHeight, unitWidth, maxWidth);

            return document;
        }

        private void finalizeSvg(SVGDocument document, SVGGraphics2D svg,
                int unitHeight, int unitWidth, int maxWidth) {
            Element root = document.getDocumentElement();
            svg.getRoot(root);

            if (fragmentMatches.size() > 0) {
                int width = maxWidth + unitWidth * fragmentMatches.get(0).getTotalCount();
                int height = unitHeight * (models.size() + 3);
                root.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, Integer.toString(width));
                root.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, Integer.toString(height));
            }
        }

        private void drawColorBars(SVGGraphics2D svg, int unitHeight,
                int unitWidth, int maxWidth) {
            for (int i = 0; i < fragmentMatches.size(); i++) {
                FragmentMatch fragmentMatch = fragmentMatches.get(i);

                for (int j = 0; j < fragmentMatch.getTotalCount(); j++) {
                    ResidueComparison comparison = fragmentMatch.getResidueComparisons().get(j);
                    int x = maxWidth + j * unitWidth;
                    int y = (i + 1) * unitHeight;
                    drawColorBarUnit(svg, comparison, x, y, unitHeight, unitWidth);
                }
            }
        }

        private void drawColorBarUnit(SVGGraphics2D svg,
                ResidueComparison comparison, int x, int y, int height,
                int width) {
            TorsionAngleDelta angleDelta = comparison.getAngleDelta(angleType);

            if (angleDelta.getState() == State.BOTH_VALID) {
                Color color = ColorMapWrapper.getColor(angleDelta.getDelta().getRadians(), 0, Math.PI);
                svg.setColor(color);
            } else {
                svg.setColor(Color.BLACK);
            }

            svg.fillRect(x, y, width, height);
            svg.setColor(Color.BLACK);
            svg.drawRect(x, y, width, height);
        }

        private DotBracket getDotBracketOrNull() {
            DotBracket dotBracket = null;
            if (target.getMoleculeType() == MoleculeType.RNA) {
                try {
                    BpSeq bpSeq = CanonicalStructureExtractor.getCanonicalSecondaryStructure(target);
                    dotBracket = DotBracket.fromBpSeq(bpSeq);
                } catch (InvalidSecondaryStructureException e) {
                    ModelsComparisonResult.LOGGER.warn("Failed to extract canonical secondary structure", e);
                }
            }
            return dotBracket;
        }

        private void drawDotBracket(SVGGraphics2D svg, DotBracket dotBracket,
                int blockWidth, int leftMargin, int topPosition) {
            if (dotBracket != null) {
                for (int i = 0; i < dotBracket.getLength(); i++) {
                    DotBracketSymbol symbol = dotBracket.getSymbol(i);
                    svg.drawString(Character.toString(symbol.getStructure()), leftMargin + i * blockWidth + blockWidth / 2, topPosition);
                }
            }
        }

        private int drawModelsNames(SVGGraphics2D svg, int unitHeight) {
            FontMetrics metrics = SVGHelper.getFontMetrics(svg);
            int maxWidth = Integer.MIN_VALUE;

            for (int i = 0; i < models.size(); i++) {
                String modelName = models.get(i).getName();
                svg.drawString(modelName, 0.0f, (i + 2) * unitHeight);
                int width = metrics.stringWidth(modelName);

                if (width > maxWidth) {
                    maxWidth = width;
                }
            }

            return maxWidth;
        }

        @Override
        public void visualize3D() {
            if (models.size() < 1) {
                JOptionPane.showMessageDialog(null, "At least one model is required for 3D visualization", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String name = target.getName() + " " + angleType.getExportName();
                double[][] matrix = prepareMatrix();
                List<String> ticksX = prepareTicksX();
                List<String> ticksY = prepareTicksY();
                NavigableMap<Double, String> valueTickZ = MCQLocalResult.prepareTicksZ();
                String labelX = "Model";
                String labelY = "Residue";
                String labelZ = "Distance";
                boolean showAllTicksX = true;
                boolean showAllTicksY = false;

                Surface3D surface3d = new Surface3D(name, matrix, ticksX, ticksY, valueTickZ, labelX, labelY, labelZ, showAllTicksX, showAllTicksY);
                AnalysisLauncher.open(surface3d);
            } catch (Exception e) {
                String message = "Failed to visualize in 3D";
                ModelsComparisonResult.LOGGER.error(message, e);
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private double[][] prepareMatrix() {
            int size = getTargetSize();
            double[][] matrix = new double[models.size()][];

            for (int i = 0; i < models.size(); i++) {
                matrix[i] = new double[size];
                FragmentMatch fragmentMatch = fragmentMatches.get(i);
                List<ResidueComparison> residueComparisons = fragmentMatch.getResidueComparisons();

                for (int j = 0; j < size; j++) {
                    ResidueComparison residueComparison = residueComparisons.get(j);
                    matrix[i][j] = residueComparison.getAngleDelta(angleType).getDelta().getRadians();
                }
            }

            return matrix;
        }

        private List<String> prepareTicksX() {
            List<String> ticksX = new ArrayList<>();
            for (PdbCompactFragment model : models) {
                ticksX.add(model.getName());
            }
            return ticksX;
        }

        private List<String> prepareTicksY() {
            List<String> ticksY = new ArrayList<>();
            for (PdbResidue residue : target.getResidues()) {
                ticksY.add(residue.toString());
            }
            return ticksY;
        }
    }

    private final PdbCompactFragment target;
    private final List<PdbCompactFragment> models;
    private final List<FragmentMatch> fragmentMatches;

    public ModelsComparisonResult(PdbCompactFragment target,
            List<PdbCompactFragment> models, List<FragmentMatch> matches) {
        super();
        this.target = target;
        this.models = models;
        this.fragmentMatches = matches;
    }

    public PdbCompactFragment getTarget() {
        return target;
    }

    public PdbCompactFragment getModel(int index) {
        return models.get(index);
    }

    public int getTargetSize() {
        return target.size();
    }

    public int getModelCount() {
        return fragmentMatches.size();
    }

    public ModelsComparisonResult.SelectedAngle selectAngle(
            MasterTorsionAngleType torsionAngle) {
        return new ModelsComparisonResult.SelectedAngle(torsionAngle);
    }
}
