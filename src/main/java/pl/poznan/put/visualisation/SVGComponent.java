package pl.poznan.put.visualisation;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JFileChooser;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.types.ExportFormat;

public abstract class SVGComponent extends JSVGCanvas implements Exportable {
    private final JFileChooser chooser = new JFileChooser();
    private final int svgWidth;
    private final int svgHeight;

    public SVGComponent(SVGDocument svg) {
        setSVGDocument(svg);

        Element rootElement = svg.getDocumentElement();
        svgWidth = (int) Math.ceil(Double.parseDouble(rootElement.getAttributeNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "width")));
        svgHeight = (int) Math.ceil(Double.parseDouble(rootElement.getAttributeNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "height")));
        setPreferredSize(new Dimension(svgWidth, svgHeight));
    }

    public int getSvgWidth() {
        return svgWidth;
    }

    public int getSvgHeight() {
        return svgHeight;
    }

    @Override
    public void export(File file) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            TranscoderInput input = new TranscoderInput(svgDocument);
            TranscoderOutput output = new TranscoderOutput(writer);
            Transcoder transcoder = new SVGTranscoder();
            transcoder.transcode(input, output);
        } catch (TranscoderException e) {
            throw new IOException("Failed to save SVG image", e);
        }
    }

    @Override
    public ExportFormat getExportFormat() {
        return ExportFormat.SVG;
    }

    public void selectFileAndExport() {
        chooser.setSelectedFile(suggestName());
        int state = chooser.showOpenDialog(getParent());

        if (state == JFileChooser.APPROVE_OPTION) {
            try {
                export(chooser.getSelectedFile());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

    }
}
