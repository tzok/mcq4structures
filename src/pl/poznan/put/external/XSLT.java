package pl.poznan.put.external;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public final class XSLT {
    private static final Logger LOGGER = LoggerFactory.getLogger(XSLT.class);

    public static void printDocument(Document doc, OutputStream out)
            throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc), new StreamResult(
                new OutputStreamWriter(out, "UTF-8")));
    }

    private static String transform(Source stylesheet, Source xml,
            Map<String, Object> parameters) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Result result =
                    new StreamResult(new OutputStreamWriter(stream,
                            Charset.forName("UTF-8")));

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(stylesheet);
            if (parameters != null) {
                for (Entry<String, Object> entry : parameters.entrySet()) {
                    transformer.setParameter(entry.getKey(), entry.getValue());
                }
            }
            transformer.transform(xml, result);

            return stream.toString("UTF-8");
        } catch (UnsupportedEncodingException
                | TransformerFactoryConfigurationError | TransformerException e) {
            XSLT.LOGGER.error("Failed to transform data via XSLT processor", e);
            return "";
        }
    }

    static String transform(URL resource, Source xml,
            Map<String, Object> parameters) throws IOException {
        try (InputStream stream = resource.openStream()) {
            return XSLT.transform(new StreamSource(stream), xml, parameters);
        }
    }

    private XSLT() {
    }
}
