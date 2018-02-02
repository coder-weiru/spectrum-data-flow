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

import li.spectrum.data.model.TikaModel;

public class TikaExtractor {

	private static Logger logger = LoggerFactory.getLogger(TikaExtractor.class);

	private TikaParser tikaParser;

	@Autowired
	public TikaExtractor(TikaParser tikaParser) {
		super();
		this.tikaParser = tikaParser;
	}

	public TikaModel extract(InputStream payload) throws TikaException {
		logger.debug("Extracting payload: " + payload.toString());

		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();

		try {
			tikaParser.parse(payload, metadata, handler);
		} catch (TikaException e) {
			throw e;
		} catch (IOException | SAXException e) {
			logger.error("Error extracting file metadata using Tika, nested exception: ", e);
			throw new TikaException(e.getMessage(), e);
		}

		// getting the content of the document
		String doc = "{" + "   \"doc\":\"" + handler.toString() + "\"" + "}";

		// logger.debug("Contents of the document :" + doc);

		TikaModel document = new TikaModel();

		// getting metadata of the document
		// logger.info("Metadata of the document:");
		String[] metadataNames = metadata.names();

		for (String name : metadataNames) {
			document.addMetadata(name, metadata.getValues(name));
		}

		return document;
	}


}
