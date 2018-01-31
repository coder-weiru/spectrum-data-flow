package li.spectrum.ingestion.tika;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

public class TikaExtractor {

	private static Logger logger = LoggerFactory.getLogger(TikaExtractor.class);

	private TikaParser tikaParser;

	@Autowired
	public TikaExtractor(TikaParser tikaParser) {
		super();
		this.tikaParser = tikaParser;
	}

	public TikaDocument extract(InputStream payload) {
		logger.debug("Extracting payload: " + payload.toString());

		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();

		try {
			tikaParser.parse(payload, metadata, handler);
		} catch (IOException | SAXException | TikaException e) {
			e.printStackTrace();
		}

		// getting the content of the document
		String doc = "{" + "   \"doc\":\"" + handler.toString() + "\"" + "}";

		// logger.debug("Contents of the document :" + doc);

		TikaDocument document = new TikaDocument();

		// getting metadata of the document
		// logger.info("Metadata of the document:");
		String[] metadataNames = metadata.names();

		for (String name : metadataNames) {
			document.addMetadata(name, metadata.getValues(name));
		}

		return document;
	}
}