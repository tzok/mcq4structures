package pl.poznan.put.gui.component;

import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFileChooser;

import org.apache.batik.swing.JSVGCanvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

public abstract class SVGComponent extends JSVGCanvas implements Exportable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SVGComponent.class);

    private final JFileChooser chooser = new JFileChooser();
    private final int svgWidth;
    private final int svgHeight;

    public SVGComponent(SVGDocument svg) {
        setSVGDocument(svg);

        Dimension preferredSize = getPreferredSize();
        svgWidth = preferredSize.width;
        svgHeight = preferredSize.height;
    }

    public int getSvgWidth() {
        return svgWidth;
    }

    public int getSvgHeight() {
        return svgHeight;
    }

    @Override
    public void export(OutputStream stream) throws IOException {
        SVGHelper.export(svgDocument, stream, Format.SVG, null);
    }

    @Override
    public ExportFormat getExportFormat() {
        return ExportFormat.SVG;
    }

    public void selectFileAndExport() {
        chooser.setSelectedFile(suggestName());
        int state = chooser.showOpenDialog(getParent());

        if (state == JFileChooser.APPROVE_OPTION) {
            try (OutputStream stream = new FileOutputStream(chooser.getSelectedFile())) {
                export(stream);
            } catch (IOException e) {
                SVGComponent.LOGGER.error("Failed to export SVG to file", e);
            }
        }
    }
}
