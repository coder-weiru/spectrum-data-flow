package li.spectrum.ingestion.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import li.spectrum.data.dbclient.FileModelService;
import li.spectrum.data.model.FileModel;
import li.spectrum.data.model.Processing;
import li.spectrum.data.model.Record;
import li.spectrum.data.model.TikaModel;
import li.spectrum.ingestion.tika.TikaExtractor;

/**
 * {@code TikaExtractionDelegate} is a Java task responsible for extracting tika
 * entities.
 */
public class TikaExtractionDelegate implements JavaDelegate {

	private static Logger logger = LoggerFactory.getLogger(TikaExtractionDelegate.class);

	private volatile FileModelService fileModelService;

	private volatile TikaExtractor tikaExtractor;

	/**
	 * Will construct this instance using provided {@link FileModelService} and
	 * {@link TikaExtractor}
	 *
	 * @param fileModelService
	 *            The file model service.
	 * @param tikaExtractor
	 *            The tika extractor.
	 */
	@Autowired
	public TikaExtractionDelegate(FileModelService fileModelService,
			TikaExtractor tikaExtractor) {
		Assert.notNull(fileModelService, "'fileModelService' must not be null");
		Assert.notNull(tikaExtractor, "'tikaExtractor' must not be null");
		this.fileModelService = fileModelService;
		this.tikaExtractor = tikaExtractor;
	}

	@Override
	public void execute(DelegateExecution execution) {
		String procId = (String) execution.getVariable("parentProcessId");
		logger.debug("Continuing file process with parent: [" + procId + "]");

		Record fileRecord = (Record) execution.getVariable("fileRecord");
		logger.debug("extracting file: " + fileRecord.getValue());

		Assert.notNull(procId, "'parentProcessId' must not be null");
		Assert.notNull(fileRecord, "'fileRecord' must not be null");

		String filePath = fileRecord.getValue();
		FileModel fm = this.fileModelService.get(filePath);
		Processing processing = new Processing();
		processing.setTaskName(this.getClass().getSimpleName());
		fm.setProcessing(processing);

		TikaModel tika = null;
		InputStream targetStream = null;
		File file = Paths.get(fileRecord.getValue()).toFile();
		if (file.isFile()) {
			try {
				targetStream = new FileInputStream(file);
				tika = this.tikaExtractor.extract(targetStream);
				if (targetStream != null) {
					targetStream.close();
				}
				logger.debug("Tika extracted: [" + tika.getMetadata() + "]");
				processing.setStatus("OK");
			} catch (TikaException | IOException e) {
				logger.error("Error extracting file {}, skipping it.", filePath);
				processing.setException(e);
				processing.setStatus("WARN");
			}
			fm.setTikaModel(tika);
		}
		this.fileModelService.addOrUpdate(fm);
	}

}
