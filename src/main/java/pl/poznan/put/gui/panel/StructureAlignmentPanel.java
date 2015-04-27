package pl.poznan.put.gui.panel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.tuple.Pair;
import org.biojava.bio.structure.align.gui.jmol.JmolPanel;

import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.tertiary.StructureManager;

public class StructureAlignmentPanel extends JPanel {
    private final JTextPane labelInfoAlignStruc = new JTextPane();
    private final JLabel labelAlignmentStatus = new JLabel("", SwingConstants.CENTER);
    private final JmolPanel panelJmolLeft = new JmolPanel();
    private final JmolPanel panelJmolRight = new JmolPanel();

    private Pair<PdbModel, PdbModel> structures;
    private Pair<List<PdbChain>, List<PdbChain>> chains;

    public StructureAlignmentPanel() {
        super();

        labelInfoAlignStruc.setBorder(new EmptyBorder(10, 10, 10, 0));
        labelInfoAlignStruc.setContentType("text/html");
        labelInfoAlignStruc.setEditable(false);
        labelInfoAlignStruc.setFont(UIManager.getFont("Label.font"));
        labelInfoAlignStruc.setOpaque(false);

        panelJmolLeft.executeCmd("background lightgrey; save state state_init");
        panelJmolRight.executeCmd("background darkgray; save state state_init");

        JPanel panelInfo = new JPanel(new GridLayout(1, 3));
        panelInfo.add(new JLabel("Whole structures (Jmol view)", SwingConstants.CENTER));
        panelInfo.add(labelAlignmentStatus);
        panelInfo.add(new JLabel("Aligned fragments (Jmol view)", SwingConstants.CENTER));

        JPanel panelMain = new JPanel(new BorderLayout());
        panelMain.add(labelInfoAlignStruc, BorderLayout.NORTH);
        panelMain.add(panelInfo, BorderLayout.CENTER);

        JPanel panelJmols = new JPanel(new GridLayout(1, 2));
        panelJmols.add(panelJmolLeft);
        panelJmols.add(panelJmolRight);

        add(panelMain, BorderLayout.NORTH);
        add(panelJmols, BorderLayout.CENTER);
    }

    public void setStructuresAndChains(Pair<PdbModel, PdbModel> structures,
            Pair<List<PdbChain>, List<PdbChain>> chains) {
        this.structures = structures;
        this.chains = chains;

        panelJmolLeft.executeCmd("restore state state_init");
        panelJmolRight.executeCmd("restore state state_init");
        updateHeader();
    }

    public void updateHeader() {
        PdbModel left = structures.getLeft();
        PdbModel right = structures.getRight();

        StringBuilder builder = new StringBuilder();
        builder.append("<span style=\"color: blue\">");
        builder.append(StructureManager.getName(left));
        builder.append('.');

        for (PdbChain chain : chains.getLeft()) {
            builder.append(chain.getIdentifier());
        }

        builder.append("</span>, <span style=\"color: green\">");
        builder.append(StructureManager.getName(right));
        builder.append('.');

        for (PdbChain chain : chains.getRight()) {
            builder.append(chain.getIdentifier());
        }

        builder.append("</span>");
        labelInfoAlignStruc.setText("<html>Structures selected for 3D structure alignment: " + builder.toString() + "</html>");
    }
}
