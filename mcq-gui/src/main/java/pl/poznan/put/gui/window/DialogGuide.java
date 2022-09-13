package pl.poznan.put.gui.window;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.swing.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("ALL")
public class DialogGuide extends JDialog {
  private static final Logger LOGGER = LoggerFactory.getLogger(DialogGuide.class);

  public DialogGuide(final Frame owner) {
    super(owner, true);

    final JEditorPane editorPane = new JEditorPane();
    editorPane.setContentType("text/html");
    editorPane.setEditable(false);

    final URL resource = getClass().getResource("/guide.html");
    try (final InputStream stream = resource.openStream()) {
      editorPane.setText(IOUtils.toString(stream, StandardCharsets.UTF_8));
      editorPane.setCaretPosition(0);
    } catch (final IOException e) {
      DialogGuide.LOGGER.error("Failed to load quick guide text", e);
    }

    final JButton buttonClose = new JButton("Close");
    final JPanel panelButton = new JPanel();
    panelButton.add(buttonClose);

    setLayout(new BorderLayout());
    add(new JScrollPane(editorPane), BorderLayout.CENTER);
    add(panelButton, BorderLayout.SOUTH);

    setSize(640, 480);
    setTitle("MCQ4Structures: quick guide");

    buttonClose.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent arg0) {
            dispose();
          }
        });
  }
}
