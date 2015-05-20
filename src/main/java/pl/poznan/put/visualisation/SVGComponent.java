package pl.poznan.put.visualisation;

import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFileChooser;

import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.types.ExportFormat;
import pl.poznan.put.utility.svg.Format;
import pl.poznan.put.utility.svg.SVGHelper;

public abstract class SVGComponent extends JSVGCanvas implements Exportable {
    private final JFileChooser chooser = new JFileChooser();
    private final int svgWidth;
    private final int svgHeight;

    public SVGComponent(SVGDocument svg) {
        setSVGDocument(svg);

        Element rootElement = svg.getDocumentElement();
        svgWidth = (int) Math.ceil(Double.parseDouble(rootElement.getAttribute("width")));
        svgHeight = (int) Math.ceil(Double.parseDouble(rootElement.getAttribute("height")));
        setPreferredSize(new Dimension(svgWidth, svgHeight));
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
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

    }
}
