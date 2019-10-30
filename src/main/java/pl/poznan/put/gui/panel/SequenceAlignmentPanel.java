package pl.poznan.put.gui.panel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.datamodel.ProcessingResult;
import pl.poznan.put.pdb.analysis.PdbCompactFragment;
import pl.poznan.put.sequence.alignment.SequenceAligner;
import pl.poznan.put.sequence.alignment.SequenceAlignment;

public class SequenceAlignmentPanel extends JPanel {
  private static final Logger LOGGER = LoggerFactory.getLogger(SequenceAlignmentPanel.class);

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

    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(labelHeader, BorderLayout.CENTER);

    add(panel, BorderLayout.NORTH);
    add(new JScrollPane(textAreaAlignment), BorderLayout.CENTER);
  }

  public final void setFragments(final List<? extends PdbCompactFragment> fragments, final boolean isGlobal) {
    this.fragments = new ArrayList<>(fragments);
    this.isGlobal = isGlobal;

    textAreaAlignment.setText("");
    updateHeader(false);
  }

  private void updateHeader(final boolean readyResults) {
    final StringBuilder builder = new StringBuilder();
    builder.append("<html>Structures selected for ");
    builder.append(isGlobal ? "global" : "local");
    builder.append(" sequence alignment: ");

    int i = 0;

    for (final PdbCompactFragment c : fragments) {
      builder.append("<span style=\"color: ").append(i % 2 == 0 ? "blue" : "green").append("\">");
      builder.append(c.getName());
      builder.append("</span>, ");
      i++;
    }

    builder.delete(builder.length() - 2, builder.length());

    if (readyResults) {
      builder.append("<br>");
      builder.append(isGlobal ? "Global" : "Local");
      builder.append(" sequence alignment results:");
    }

    builder.append("</html>");
    labelHeader.setText(builder.toString());
  }

  public final ProcessingResult alignAndDisplaySequences() {
    try {
      final SequenceAligner aligner = new SequenceAligner(fragments, isGlobal);
      final SequenceAlignment alignment = aligner.align();
      textAreaAlignment.setText(Objects.requireNonNull(alignment).toString());
      updateHeader(true);
      return new ProcessingResult(alignment);
    } catch (final CompoundNotFoundException e) {
      final String message = "Failed to align sequences";
      SequenceAlignmentPanel.LOGGER.error(message, e);
      JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    return ProcessingResult.emptyInstance();
  }
}
