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

import pl.poznan.put.pdb.analysis.PdbCompactFragment;

public class SequenceAlignmentPanel extends JPanel {
    private final JTextPane labelInfoAlignSeq = new JTextPane();
    private final JTextArea textAreaAlignSeq = new JTextArea();

    private List<PdbCompactFragment> fragments = Collections.emptyList();

    public SequenceAlignmentPanel() {
        super(new BorderLayout());

        labelInfoAlignSeq.setBorder(new EmptyBorder(10, 10, 10, 0));
        labelInfoAlignSeq.setContentType("text/html");
        labelInfoAlignSeq.setEditable(false);
        labelInfoAlignSeq.setFont(UIManager.getFont("Label.font"));
        labelInfoAlignSeq.setOpaque(false);
        textAreaAlignSeq.setEditable(false);
        textAreaAlignSeq.setFont(new Font("Monospaced", Font.PLAIN, 20));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(labelInfoAlignSeq, BorderLayout.CENTER);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(textAreaAlignSeq), BorderLayout.CENTER);
    }

    public void setFragments(List<PdbCompactFragment> fragments, boolean isGlobal) {
        this.fragments = fragments;
        updateHeaderAndResetTextArea(isGlobal);
    }

    private void updateHeaderAndResetTextArea(boolean isGlobal) {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        for (PdbCompactFragment c : fragments) {
            builder.append("<span style=\"color: " + (i % 2 == 0 ? "blue" : "green") + "\">");
            builder.append(c.toString());
            builder.append("</span>, ");
            i++;
        }

        builder.delete(builder.length() - 2, builder.length());
        labelInfoAlignSeq.setText("<html>Structures selected for " + (isGlobal ? "global" : "local") + " sequence alignment: " + builder.toString() + "</html>");
        textAreaAlignSeq.setText("");
    }
}
