package pl.poznan.put.gui.panel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.exception.InvalidCircularOperationException;
import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.circular.graphics.AngularHistogram;
import pl.poznan.put.datamodel.ProcessingResult;
import pl.poznan.put.gui.component.SVGComponent;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;
import pl.poznan.put.torsion.MasterTorsionAngleType;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.util.List;

public class TorsionAngleValuesMatrixPanel extends JPanel {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(TorsionAngleValuesMatrixPanel.class);
    private static final long serialVersionUID = -59286774381464603L;

    private final JTextPane labelInfoMatrix = new JTextPane();
    private final JTable tableMatrix = new JTable();
    private final JTabbedPane tabbedPane = new JTabbedPane();

    public TorsionAngleValuesMatrixPanel() {
        super(new BorderLayout());

        labelInfoMatrix.setBorder(new EmptyBorder(10, 10, 10, 0));
        labelInfoMatrix.setContentType("text/html");
        labelInfoMatrix.setEditable(false);
        labelInfoMatrix.setFont(UIManager.getFont("Label.font"));
        labelInfoMatrix.setOpaque(false);

        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.add(labelInfoMatrix, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(tableMatrix);
        tabbedPane.add("Torsion angles", scrollPane);

        add(panelInfo, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public final ProcessingResult calculateTorsionAngles(
            final PdbModel structure) {
        removeAllButFirstTab();
        updateHeader(structure);

        StructureSelection selection = SelectionFactory
                .create(StructureManager.getName(structure), structure);
        tableMatrix.setModel(selection.asDisplayableTableModel());

        for (final MasterTorsionAngleType masterType : selection
                .getCommonTorsionAngleTypes()) {
            List<Angle> angles =
                    selection.getValidTorsionAngleValues(masterType);

            if (angles.isEmpty()) {
                continue;
            }

            try {
                String title = masterType.getLongDisplayName();
                AngularHistogram histogram = new AngularHistogram(angles);
                histogram.draw();
                SVGDocument svgDocument = histogram.finalizeDrawing();
                SVGComponent component = new SVGComponent(svgDocument,
                                                          masterType
                                                                  .getExportName());
                tabbedPane.add(title, component);
            } catch (InvalidCircularValueException |
                    InvalidCircularOperationException e) {
                TorsionAngleValuesMatrixPanel.LOGGER
                        .warn("Failed to visualize torsion angles of type: {}",
                              masterType, e);
            }
        }

        return new ProcessingResult(selection);
    }

    private void removeAllButFirstTab() {
        while (tabbedPane.getComponentCount() > 1) {
            tabbedPane.remove(1);
        }
    }

    public final void updateHeader(final PdbModel structure) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                "<html>Structure selected for torsion angles calculation: "
                + "<span style=\"color: blue\">");
        builder.append(StructureManager.getName(structure));
        builder.append("</span></html>");
        labelInfoMatrix.setText(builder.toString());
    }
}
