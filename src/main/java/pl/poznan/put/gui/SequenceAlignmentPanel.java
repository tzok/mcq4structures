package pl.poznan.put.gui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class SequenceAlignmentPanel extends JPanel {
    private final JTextPane labelInfoAlignSeq = new JTextPane();
    private final JTextArea textAreaAlignSeq = new JTextArea();

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
}
