package pl.poznan.put.cs.bioserver.external;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.beans.XMLSerializable;

public class Matplotlib {
    public enum Method {
        SINGLE, COMPLETE, AVERAGE, WEIGHTED
    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(Matplotlib.class);

    public static void runXsltAndPython(URL resource, File fileScript,
            File fileOutput, XMLSerializable data) throws IOException,
            JAXBException, ParserConfigurationException {
        Matplotlib.runXsltAndPython(resource, fileScript, fileOutput, data,
                null);
    }

    public static void runXsltAndPython(URL resource, File fileScript,
            File fileOutput, XMLSerializable data,
            Map<String, Object> parameters) throws IOException, JAXBException,
            ParserConfigurationException {
        String pythonCode = XSLT.transform(resource,
                new DOMSource(data.toXML()), parameters);
        Matplotlib.LOGGER.trace("Generated script:\n" + pythonCode);

        try (Writer writer = new FileWriterWithEncoding(fileScript,
                Charset.forName("UTF-8"))) {
            writer.write(pythonCode);
        }

        CommandLine commandLine = new CommandLine("python");
        commandLine.addArgument(fileScript.getAbsolutePath());
        commandLine.addArgument(fileOutput.getAbsolutePath());
        Matplotlib.LOGGER.trace("Executing: " + commandLine);

        Executor executor = new DefaultExecutor();
        executor.execute(commandLine);
    }

    public static void runXsltAndPython(URL resource, XMLSerializable data)
            throws IOException, JAXBException, ParserConfigurationException {
        Matplotlib.runXsltAndPython(resource, data, null);
    }

    public static void runXsltAndPython(URL resource, XMLSerializable data,
            Map<String, Object> parameters) throws IOException, JAXBException,
            ParserConfigurationException {
        File fileScript = null;
        File fileOutput = null;

        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Python script", "py");
        chooser.setFileFilter(filter);
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        fileScript = chooser.getSelectedFile();

        chooser.setFileFilter(new FileNameExtensionFilter(
                "Portable Network Graphics (PNG)", "png"));
        for (String[] pair : new String[][] {
                new String[] { "Portable Document Format (PDF)", "pdf" },
                new String[] { "Encapsulated Postscript (EPS)", "eps" },
                new String[] { "Scalable Vector Graphics (SVG)", "svg" } }) {
            chooser.addChoosableFileFilter(new FileNameExtensionFilter(pair[0],
                    pair[1]));
        }
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        fileOutput = chooser.getSelectedFile();

        Matplotlib.runXsltAndPython(resource, fileScript, fileOutput, data,
                parameters);
    }

    private Matplotlib() {
    }
}
