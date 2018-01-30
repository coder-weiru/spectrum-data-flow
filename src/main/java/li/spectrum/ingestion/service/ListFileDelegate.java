package li.spectrum.ingestion.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import li.spectrum.ingestion.dbclient.DocumentService;

/**
 * {@code ListFileDelegate} is a Java task responsible for “ls dir”.
 */
public class ListFileDelegate implements JavaDelegate {

	@Value("${marklogic.collection.proc}")
	private String collectionName;

	private static Logger logger = LoggerFactory.getLogger(ListFileDelegate.class);

	private volatile DocumentService documentService;

	/**
	 * Will construct this instance using provided {@link DocumentService}
	 *
	 * @param documentService
	 *            The document service.
	 */
	@Autowired
	public ListFileDelegate(DocumentService documentService) {
		Assert.notNull(documentService, "'documentService' must not be null");

		this.documentService = documentService;
	}

	@Override
	public void execute(DelegateExecution execution) {
		String rootDir = (String) execution.getVariable("rootDir");
		logger.debug("Listing root directory: " + rootDir);

		String[] fileNames = listFile(rootDir);

		InputStream stream = new ByteArrayInputStream(createFileListXml(rootDir, fileNames));
		String id = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Timestamp(System.currentTimeMillis())) + "_"
				+ UUID.randomUUID().toString();
		this.documentService.add(id, stream, collectionName);
	}

	private byte[] createFileListXml(String rootDir, String[] filePathes) {
		byte[] content = null;

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("process");

			Attr attrTimestamp = doc.createAttribute("timestamp");
			String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
					.format(new Timestamp(System.currentTimeMillis()));
			attrTimestamp.setValue(timestamp);

			Attr attrRootDir = doc.createAttribute("rootDir");
			attrRootDir.setValue(rootDir);

			rootElement.setAttributeNode(attrTimestamp);
			rootElement.setAttributeNode(attrRootDir);

			doc.appendChild(rootElement);

			Element files = doc.createElement("files");
			rootElement.appendChild(files);

			Element file = null;
			for (String s : filePathes) {
				file = doc.createElement("file");
				file.appendChild(doc.createTextNode(s));
				files.appendChild(file);
			}
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(outputStream);
			transformer.transform(source, result);
			content = outputStream.toByteArray();

		} catch (ParserConfigurationException | TransformerException pce) {
			pce.printStackTrace();
		}

		return content;
	}

	private String[] listFile(String dirPath) {
		File f = new File(dirPath);
		File[] files = f.listFiles();
		List<String> pathes = new ArrayList<String>();
		if (files != null)
			for (int i = 0; i < files.length; i++) {
				File file = files[i];

				if (file.isDirectory()) {
					pathes.addAll(Arrays.asList(listFile(file.getAbsolutePath())));
				} else {
					pathes.add(file.getAbsolutePath());
				}
			}
		return (String[]) pathes.toArray(new String[pathes.size()]);
	}

}
