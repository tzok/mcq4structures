package pl.poznan.put.gui.component;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SVGComponent extends JSVGCanvas implements Exportable {
  private static final Logger LOGGER = LoggerFactory.getLogger(SVGComponent.class);

  private final JPopupMenu popup = new JPopupMenu();
  private final JMenuItem saveAsSvg = new JMenuItem("Save as SVG");
  private final JFileChooser chooser = new JFileChooser();

  private final int svgWidth;
  private final int svgHeight;
  private final String name;

  public SVGComponent(SVGDocument svg, String name) {
    this.name = name;
    setSVGDocument(svg);

    Dimension preferredSize = getPreferredSize();
    svgWidth = preferredSize.width;
    svgHeight = preferredSize.height;

    popup.add(saveAsSvg);

    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
          }

          private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
              Component component = e.getComponent();
              int x = e.getX();
              int y = e.getY();
              popup.show(component, x, y);
            }
          }
        });

    saveAsSvg.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            selectFileAndExport();
          }
        });
  }

  public void selectFileAndExport() {
    chooser.setSelectedFile(suggestName());
    int state = chooser.showSaveDialog(getParent());

    if (state == JFileChooser.APPROVE_OPTION) {
      try (OutputStream stream = new FileOutputStream(chooser.getSelectedFile())) {
        export(stream);
      } catch (IOException e) {
        SVGComponent.LOGGER.error("Failed to export SVG to file", e);
      }
    }
  }

  @Override
  public void export(OutputStream stream) throws IOException {
    byte[] bytes = SVGHelper.export(svgDocument, Format.SVG);
    IOUtils.write(bytes, stream);
  }

  @Override
  public ExportFormat getExportFormat() {
    return ExportFormat.SVG;
  }

  @Override
  public File suggestName() {
    return new File(name + ".svg");
  }

  public int getSvgWidth() {
    return svgWidth;
  }

  public int getSvgHeight() {
    return svgHeight;
  }
}
