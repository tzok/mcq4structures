package pl.poznan.put.cs.bioserver.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DialogGuide extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DialogGuide.class);

    DialogGuide(Frame owner) {
        super(owner, true);

        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);

        URL resource = getClass().getResource(
                "/pl/poznan/put/cs/bioserver/gui/guide.html");
        try (InputStream stream = resource.openStream()) {
            editorPane.setText(IOUtils.toString(stream, "UTF-8"));
            editorPane.setCaretPosition(0);
        } catch (IOException e) {
            DialogGuide.LOGGER.error("Failed to load quick guide text", e);
        }

        JButton buttonClose = new JButton("Close");
        JPanel panelButton = new JPanel();
        panelButton.add(buttonClose);

        setLayout(new BorderLayout());
        add(new JScrollPane(editorPane), BorderLayout.CENTER);
        add(panelButton, BorderLayout.SOUTH);

        setSize(640, 480);
        setTitle("MCQ4Structures: quick guide");

        buttonClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });
    }
}
