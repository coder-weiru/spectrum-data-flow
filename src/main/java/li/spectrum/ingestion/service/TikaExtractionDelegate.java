package li.spectrum.ingestion.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import li.spectrum.data.dbclient.FileModelService;
import li.spectrum.data.dbclient.ProcessService;
import li.spectrum.data.model.File;
import li.spectrum.data.model.FileModel;
import li.spectrum.data.model.Metadata;
import li.spectrum.data.model.Proc;
import li.spectrum.data.model.Processing;
import li.spectrum.data.model.TikaModel;
import li.spectrum.ingestion.tika.TikaExtractor;
import li.spectrum.ingestion.tika.TikaParser;

/**
 * {@code TikaExtractionDelegate} is a Java task responsible for extracting tika
 * entities.
 */
public class TikaExtractionDelegate implements JavaDelegate {

	private static Logger logger = LoggerFactory.getLogger(TikaExtractionDelegate.class);

	private volatile ProcessService processService;

	private volatile FileModelService fileModelService;

	private volatile TikaExtractor tikaExtractor;

	/**
	 * Will construct this instance using provided {@link ProcessService} and
	 * {@link TikaParser}
	 *
	 * @param processService
	 *            The process service.
	 * @param fileModelService
	 *            The file model service.
	 * @param tikaExtractor
	 *            The tika extractor.
	 */
	@Autowired
	public TikaExtractionDelegate(ProcessService processService, FileModelService fileModelService,
			TikaExtractor tikaExtractor) {
		Assert.notNull(processService, "'processService' must not be null");
		Assert.notNull(fileModelService, "'fileModelService' must not be null");
		Assert.notNull(tikaExtractor, "'tikaExtractor' must not be null");
		this.processService = processService;
		this.fileModelService = fileModelService;
		this.tikaExtractor = tikaExtractor;
	}

	@Override
	public void execute(DelegateExecution execution) {
		String procId = (String) execution.getVariable("processId");
		logger.debug("Continuing process: [" + procId + "]");

		Proc proc = processService.get(procId);
		List<File> files = proc.getFiles();
		java.io.File file = null;
		TikaModel tika = null;
		Processing processing = null;
		for (File f : files) {
			processing = new Processing();
			processing.setTaskName(this.getClass().getSimpleName());

			Metadata meta = f.get_metadata();
			meta.setType(f.getClass().getSimpleName());
			meta.setUri(f.getCanonicalPath());

			FileModel fm = new FileModel();
			fm.setFile(f);
			fm.setProcessing(processing);
			fm.setFilePath(f.getCanonicalPath());

			InputStream targetStream = null;
			file = new java.io.File(f.getCanonicalPath());
			if (file.isFile()) {
				try {
					targetStream = new FileInputStream(file);
					tika = this.tikaExtractor.extract(targetStream);
					if (targetStream != null) {
						targetStream.close();
					}
					processing.setStatus("OK");
				} catch (TikaException | IOException e) {
					logger.error("Error extracting file {}, skipping it.", f);
					processing.setException(e);
					processing.setStatus("WARN");
				}
				logger.debug("Tika extracted: [" + tika.getMetadata() + "]");
				fm.setTikaModel(tika);

			}

			this.fileModelService.add(fm);

		}

	}

}
