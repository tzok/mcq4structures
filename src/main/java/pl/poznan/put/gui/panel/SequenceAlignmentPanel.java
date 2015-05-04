package pl.poznan.put.gui.panel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import pl.poznan.put.alignment.SequenceAligner;
import pl.poznan.put.alignment.SequenceAlignment;
import pl.poznan.put.gui.ProcessingResult;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;

public class SequenceAlignmentPanel extends JPanel {
    private final JTextPane labelHeader = new JTextPane();
    private final JTextArea textAreaAlignment = new JTextArea();

    private List<PdbCompactFragment> fragments = Collections.emptyList();
    private boolean isGlobal;

    public SequenceAlignmentPanel() {
        super(new BorderLayout());

        labelHeader.setBorder(new EmptyBorder(10, 10, 10, 0));
        labelHeader.setContentType("text/html");
        labelHeader.setEditable(false);
        labelHeader.setFont(UIManager.getFont("Label.font"));
        labelHeader.setOpaque(false);
        textAreaAlignment.setEditable(false);
        textAreaAlignment.setFont(new Font("Monospaced", Font.PLAIN, 20));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(labelHeader, BorderLayout.CENTER);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(textAreaAlignment), BorderLayout.CENTER);
    }

    public void setFragments(List<PdbCompactFragment> fragments,
            boolean isGlobal) {
        this.fragments = fragments;
        this.isGlobal = isGlobal;

        textAreaAlignment.setText("");
        updateHeader(false);
    }

    private void updateHeader(boolean readyResults) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html>Structures selected for ");
        builder.append((isGlobal ? "global" : "local"));
        builder.append(" sequence alignment: ");

        int i = 0;

        for (PdbCompactFragment c : fragments) {
            builder.append("<span style=\"color: " + (i % 2 == 0 ? "blue" : "green") + "\">");
            builder.append(c.toString());
            builder.append("</span>, ");
            i++;
        }

        builder.delete(builder.length() - 2, builder.length());

        if (readyResults) {
            builder.append("<br>");
            builder.append(isGlobal ? "Global" : "Local");
            builder.append("sequence alignment results:");
        }

        builder.append("</html>");
        labelHeader.setText(builder.toString());
    }

    public ProcessingResult alignAndDisplaySequences() {
        SequenceAligner aligner = new SequenceAligner(fragments, isGlobal);
        SequenceAlignment alignment = aligner.align();

        textAreaAlignment.setText(alignment.toString());
        updateHeader(true);

        return new ProcessingResult(alignment);
    }
}
