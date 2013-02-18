package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AboutDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AboutDialog.class);

    public AboutDialog(Frame owner) {
        super(owner, true);

        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);

        URL resource = getClass().getResource(
                "/pl/poznan/put/cs/bioserver/gui/about.html");
        try (InputStream stream = resource.openStream()) {
            editorPane.setText(IOUtils.toString(stream, "UTF-8"));
        } catch (IOException e) {
            AboutDialog.LOGGER.error("Failed to load 'About' text", e);
        }

        BufferedImage image = null;
        JLabel labelImage = new JLabel();
        try {
            resource = getClass().getResource(
                    "/pl/poznan/put/cs/bioserver/gui/rabit.png");
            image = ImageIO.read(resource);
            labelImage = new JLabel(new ImageIcon(image));
        } catch (IOException e) {
            AboutDialog.LOGGER.error("Failed to load RABIT logo image", e);
        }

        JButton buttonClose = new JButton("Close");
        JPanel panelClose = new JPanel();
        panelClose.add(buttonClose);

        JPanel panelImage = new JPanel(new BorderLayout());
        panelImage.add(labelImage, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(panelImage, BorderLayout.WEST);
        add(editorPane, BorderLayout.CENTER);
        add(panelClose, BorderLayout.SOUTH);

        setTitle("MCQ4Structures: about");

        setPreferredSize(new Dimension(640, 500));
        pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - 640;
        int y = screenSize.height - 500;
        setLocation(x / 2, y / 2);

        panelImage.setBackground(Color.white);

        editorPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (IOException | URISyntaxException e1) {
                            AboutDialog.LOGGER.error("Failed to browse URL: "
                                    + e.getURL(), e1);
                        }
                    }
                }
            }
        });

        buttonClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });
    }
}
