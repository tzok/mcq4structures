package pl.poznan.put.gui.window;

import java.awt.*;
import javax.swing.*;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.gui.component.SVGComponent;
import pl.poznan.put.utility.svg.Format;

class SVGDialog extends JDialog {
  private final SVGComponent svgComponent;

  SVGDialog(final String title, final SVGDocument document) {
    super();
    setTitle(title);
    setLayout(new BorderLayout());

    svgComponent = new SVGComponent(document, "result");

    final JButton buttonSave = new JButton("Save");
    add(buttonSave, BorderLayout.NORTH);
    add(svgComponent, BorderLayout.CENTER);
    pack();

    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    final Dimension screenSize = toolkit.getScreenSize();
    final int x = screenSize.width / 2 - svgComponent.getSvgWidth() / 2;
    final int y = screenSize.height / 2 - svgComponent.getSvgHeight() / 2;
    setLocation(x, y);

    buttonSave.addActionListener(e -> svgComponent.selectFileAndExport(Format.SVG));
  }
}
