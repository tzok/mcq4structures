package pl.poznan.put.gui.component;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SVGComponent extends JSVGCanvas {
  private static final Logger LOGGER = LoggerFactory.getLogger(SVGComponent.class);

  private final JPopupMenu popup = new JPopupMenu();
  private final JFileChooser chooser = new JFileChooser();

  private final int svgWidth;
  private final int svgHeight;
  private final String name;

  public SVGComponent(final SVGDocument svg, final String name) {
    super();
    this.name = name;
    setSVGDocument(svg);

    final Dimension preferredSize = getPreferredSize();
    svgWidth = preferredSize.width;
    svgHeight = preferredSize.height;

    final JMenuItem saveAsSvg = new JMenuItem("Save as SVG");
    popup.add(saveAsSvg);
    final JMenuItem saveAsPng = new JMenuItem("Save as PNG");
    popup.add(saveAsPng);

    addMouseListener(
            new MouseAdapter() {
              @Override
              public void mousePressed(final MouseEvent mouseEvent) {
                maybeShowPopup(mouseEvent);
              }

              @Override
              public void mouseReleased(final MouseEvent mouseEvent) {
                maybeShowPopup(mouseEvent);
              }

              private void maybeShowPopup(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                  final Component component = e.getComponent();
                  final int x = e.getX();
                  final int y = e.getY();
                  popup.show(component, x, y);
                }
              }
            });

    saveAsSvg.addActionListener(event -> selectFileAndExport(Format.SVG));
    saveAsPng.addActionListener(event -> selectFileAndExport(Format.PNG));
  }

  public final void selectFileAndExport(final Format format) {
    chooser.setSelectedFile(new File(String.format("%s.%s", name, format.getExtension())));
    final int state = chooser.showSaveDialog(getParent());

    if (state == JFileChooser.APPROVE_OPTION) {
      try (final OutputStream stream = new FileOutputStream(chooser.getSelectedFile())) {
        final byte[] bytes = SVGHelper.export(svgDocument, format);
        IOUtils.write(bytes, stream);
      } catch (final IOException e) {
        SVGComponent.LOGGER.error("Failed to export image to file", e);
      }
    }
  }

  public final int getSvgWidth() {
    return svgWidth;
  }

  public final int getSvgHeight() {
    return svgHeight;
  }
}
