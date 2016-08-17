package pl.poznan.put.gui.window;

import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.gui.component.SVGComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SVGDialog extends JDialog {
    private final JButton buttonSave = new JButton("Save");
    private final SVGComponent svgComponent;

    public SVGDialog(String title, SVGDocument document) {
        super();
        setTitle(title);
        setLayout(new BorderLayout());

        svgComponent = new SVGComponent(document, "result");

        add(buttonSave, BorderLayout.NORTH);
        add(svgComponent, BorderLayout.CENTER);
        pack();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = screenSize.width / 2 - svgComponent.getSvgWidth() / 2;
        int y = screenSize.height / 2 - svgComponent.getSvgHeight() / 2;
        setLocation(x, y);

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                svgComponent.selectFileAndExport();
            }
        });
    }
}
