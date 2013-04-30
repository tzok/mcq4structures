package pl.poznan.put.cs.bioserver.external;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;

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
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Matplotlib.class);

    public enum Method {
        SINGLE, COMPLETE, AVERAGE, WEIGHTED
    }

    public static void runXsltAndPython(URL resource, File fileScript,
            File fileOutput, XMLSerializable data) throws IOException,
            JAXBException, ParserConfigurationException {
        String pythonCode = XSLT.transform(resource,
                new DOMSource(data.toXML()));
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

    private Matplotlib() {
    }

}
