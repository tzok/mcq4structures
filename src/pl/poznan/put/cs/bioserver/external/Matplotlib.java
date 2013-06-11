package pl.poznan.put.cs.bioserver.external;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.beans.XMLSerializable;

public final class Matplotlib {
    private static final Logger LOGGER = LoggerFactory.getLogger(Matplotlib.class);

    public static void runXsltAndPython(URL resource, File fileScript, File fileOutput,
            XMLSerializable data) throws IOException, JAXBException, ParserConfigurationException {
        Matplotlib.runXsltAndPython(resource, fileScript, fileOutput, data, null);
    }

    public static void runXsltAndPython(URL resource, File fileScript, File fileOutput,
            XMLSerializable data, @Nullable Map<String, Object> parameters) throws IOException,
            JAXBException, ParserConfigurationException {
        String pythonCode = XSLT.transform(resource, new DOMSource(data.toXML()), parameters);
        Matplotlib.LOGGER.trace("Generated script:\n" + pythonCode);

        try (Writer writer = new FileWriterWithEncoding(fileScript, Charset.forName("UTF-8"))) {
            writer.write(pythonCode);
        }

        CommandLine commandLine = new CommandLine("python");
        commandLine.addArgument(fileScript.getAbsolutePath());
        commandLine.addArgument(fileOutput.getAbsolutePath());
        Matplotlib.LOGGER.trace("Executing: " + commandLine);

        Executor executor = new DefaultExecutor();
        executor.execute(commandLine);
    }

    public static void runXsltAndPython(URL resource, XMLSerializable data) {
        Matplotlib.runXsltAndPython(resource, data, null);
    }

    public static void runXsltAndPython(URL resource, XMLSerializable data,
            @Nullable Map<String, Object> parameters) {
        File fileScript = null;
        File fileOutput = null;

        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Python script", "py");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File("script.py"));
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        fileScript = chooser.getSelectedFile();

        chooser.setFileFilter(new FileNameExtensionFilter("Portable Network Graphics (PNG)", "png"));
        for (String[] pair : new String[][] {
                new String[] { "Portable Document Format (PDF)", "pdf" },
                new String[] { "Encapsulated Postscript (EPS)", "eps" },
                new String[] { "Scalable Vector Graphics (SVG)", "svg" } }) {
            chooser.addChoosableFileFilter(new FileNameExtensionFilter(pair[0], pair[1]));
        }

        String originalName = fileScript.getName();
        String newExtension = ".png";
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot != -1) {
            originalName = originalName.substring(0, lastDot) + newExtension;
        } else {
            originalName = originalName + newExtension;
        }
        chooser.setSelectedFile(new File(originalName));
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        fileOutput = chooser.getSelectedFile();

        try {
            Matplotlib.runXsltAndPython(resource, fileScript, fileOutput, data, parameters);
            JOptionPane.showMessageDialog(null, "Successfully created the "
                    + "image via Matplotlib", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | JAXBException | ParserConfigurationException e) {
            Matplotlib.LOGGER.error("Failed to run Python with Matplotlib", e);
            JOptionPane.showMessageDialog(null,
                    "Failed " + "to invoke external tool to draw the plot with " + "Matplotlib\n\n"
                            + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Matplotlib() {
    }
}
