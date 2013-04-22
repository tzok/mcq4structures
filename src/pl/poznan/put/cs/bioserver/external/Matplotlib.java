package pl.poznan.put.cs.bioserver.external;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Matplotlib {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Matplotlib.class);

    public static void hierarchicalClustering(File outfile, double[][] matrix,
            String[] labels, String method) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element root = document.createElement("hierarchical");
        document.appendChild(root);

        Element element = document.createElement("outfile");
        element.setTextContent(outfile.getAbsolutePath());
        root.appendChild(element);

        element = document.createElement("method");
        element.setTextContent(method);
        root.appendChild(element);

        element = document.createElement("labels");
        for (String label : labels) {
            Element subElement = document.createElement("label");
            subElement.setTextContent(label);
            element.appendChild(subElement);
        }
        root.appendChild(element);

        element = document.createElement("data");
        for (int i = 0; i < matrix.length; i++) {
            assert matrix.length == matrix[i].length;
            for (int j = i + 1; j < matrix.length; j++) {
                Element subElement = document.createElement("value");
                subElement.setTextContent(Double.toString(matrix[i][j]));
                element.appendChild(subElement);
            }
        }
        root.appendChild(element);

        URL resource = Matplotlib.class.getResource("/pl/poznan/put/cs/"
                + "bioserver/external/MatplotlibHierarchical.xsl");
        File file = null;
        try (InputStream stream = resource.openStream()) {
            String script = XSLT.transform(new StreamSource(stream),
                    new DOMSource(document));
            Matplotlib.LOGGER.trace(script);

            file = File.createTempFile("hierarchical", ".py");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(script);
            }

            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[] { "python",
                    file.getAbsolutePath() });
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            Matplotlib.LOGGER.error("XSLT transformation and external tool "
                    + "running failed", e);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    private Matplotlib() {
    }
}
