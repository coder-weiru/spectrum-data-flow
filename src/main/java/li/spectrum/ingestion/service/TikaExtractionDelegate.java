package li.spectrum.ingestion.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import li.spectrum.ingestion.dbclient.ProcessService;
import li.spectrum.ingestion.model.Proc;
import li.spectrum.ingestion.tika.TikaDocument;
import li.spectrum.ingestion.tika.TikaExtractor;
import li.spectrum.ingestion.tika.TikaParser;

/**
 * {@code TikaExtractionDelegate} is a Java task responsible for extracting tika
 * entities.
 */
public class TikaExtractionDelegate implements JavaDelegate {

	@Value("${marklogic.collection.proc}")
	private String collectionName;

	private static Logger logger = LoggerFactory.getLogger(TikaExtractionDelegate.class);

	private volatile ProcessService processService;

	private volatile TikaExtractor tikaExtractor;

	/**
	 * Will construct this instance using provided {@link ProcessService} and
	 * {@link TikaParser}
	 *
	 * @param processService
	 *            The process service.
	 * @param tikaExtractor
	 *            The tika extractor.
	 */
	@Autowired
	public TikaExtractionDelegate(ProcessService processService, TikaExtractor tikaExtractor) {
		Assert.notNull(processService, "'processService' must not be null");
		Assert.notNull(tikaExtractor, "'tikaExtractor' must not be null");
		this.processService = processService;
		this.tikaExtractor = tikaExtractor;
	}

	@Override
	public void execute(DelegateExecution execution) {
		String procId = (String) execution.getVariable("processId");
		logger.debug("Continuing process: [" + procId + "]");

		Proc proc = processService.get(procId);
		List<String> fileNames = proc.getFiles();
		File file = null;
		TikaDocument tikaDoc = null;
		for (String f : fileNames) {
			file = new File(f);
			InputStream targetStream = null;
			try {
				targetStream = new FileInputStream(file);
				tikaDoc = this.tikaExtractor.extract(targetStream);
				if (targetStream != null) {
					targetStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			logger.debug("Tika extracted: [" + tikaDoc.getMetadata() + "]");

		}


	}



}
