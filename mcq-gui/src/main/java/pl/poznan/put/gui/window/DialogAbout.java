package pl.poznan.put.gui.window;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DialogAbout extends JDialog {
  private static final Logger LOGGER = LoggerFactory.getLogger(DialogAbout.class);
  private static final long serialVersionUID = 1L;

  public DialogAbout(final Frame owner) {
    super(owner, true);

    final JEditorPane editorPane = new JEditorPane();
    editorPane.setContentType("text/html");
    editorPane.setEditable(false);

    URL resource = getClass().getResource("/about.html");
    try (final InputStream stream = resource.openStream()) {
      editorPane.setText(IOUtils.toString(stream, StandardCharsets.UTF_8));
      editorPane.setCaretPosition(0);
    } catch (final IOException e) {
      DialogAbout.LOGGER.error("Failed to load 'About' text", e);
    }

    final JScrollPane scrollPane = new JScrollPane(editorPane);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    JLabel labelImage = new JLabel();

    try {
      resource = getClass().getResource("/rabit.png");
      labelImage = new JLabel(new ImageIcon(ImageIO.read(resource)));
    } catch (final IOException e) {
      DialogAbout.LOGGER.error("Failed to load RABIT logo image", e);
    }

    final JButton buttonClose = new JButton("Close");
    final JPanel panelClose = new JPanel();
    panelClose.add(buttonClose);

    final JPanel panelImage = new JPanel(new BorderLayout());
    panelImage.add(labelImage, BorderLayout.CENTER);

    setLayout(new BorderLayout());
    add(panelImage, BorderLayout.WEST);
    add(scrollPane, BorderLayout.CENTER);
    add(panelClose, BorderLayout.SOUTH);

    setTitle("MCQ4Structures: about");

    setPreferredSize(new Dimension(660, 510));
    pack();

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final int x = screenSize.width - 660;
    final int y = screenSize.height - 510;
    setLocation(x / 2, y / 2);

    panelImage.setBackground(Color.white);

    editorPane.addHyperlinkListener(
        e -> {
          if (e == null) {
            return;
          }

          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED
              && Desktop.isDesktopSupported()) {
            try {
              Desktop.getDesktop().browse(e.getURL().toURI());
            } catch (final IOException | URISyntaxException e1) {
              DialogAbout.LOGGER.error("Failed to browse URL: {}", e.getURL(), e1);
            }
          }
        });

    buttonClose.addActionListener(arg0 -> dispose());
  }
}
