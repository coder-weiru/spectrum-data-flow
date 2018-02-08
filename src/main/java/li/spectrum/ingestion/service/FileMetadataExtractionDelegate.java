package li.spectrum.ingestion.service;

import java.io.IOException;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import li.spectrum.data.dbclient.FileModelService;
import li.spectrum.data.model.File;
import li.spectrum.data.model.FileModel;
import li.spectrum.data.model.Processing;
import li.spectrum.data.model.Record;
import li.spectrum.data.model.builder.FileBuilder;
import li.spectrum.data.model.builder.FileModelBuilder;

/**
 * {@code FileMetadataExtractionDelegate} is a Java task responsible for
 * extracting metadata associated with file object.
 */
public class FileMetadataExtractionDelegate implements JavaDelegate {

	private static Logger logger = LoggerFactory.getLogger(FileMetadataExtractionDelegate.class);

	private volatile FileModelService fileModelService;

	/**
	 * Will construct this instance using provided {@link FileModelService}
	 *
	 * @param fileModelService
	 *            The file model service.
	 */
	@Autowired
	public FileMetadataExtractionDelegate(FileModelService fileModelService) {
		Assert.notNull(fileModelService, "'fileModelService' must not be null");
		this.fileModelService = fileModelService;
	}

	@Override
	public void execute(DelegateExecution execution) {
		String procId = (String) execution.getVariable("parentProcessId");
		logger.debug("Continuing file process with parent: [" + procId + "]");
		Record fileRecord = (Record) execution.getVariable("fileRecord");
		logger.debug("extracting file: " + fileRecord.getValue());

		Assert.notNull(procId, "'parentProcessId' must not be null");
		Assert.notNull(fileRecord, "'fileRecord' must not be null");

		File file;
		FileModel fm;
		try {
			Processing processing = new Processing();
			processing.setTaskName(this.getClass().getSimpleName());

			file = new FileBuilder().setFilePath(fileRecord.getValue()).build();
			fm = new FileModelBuilder().setFile(file).setProcessing(processing).build();

			this.fileModelService.addOrUpdate(fm);

		} catch (IOException e) {
			logger.error("Error building File {}", e);
		}

		execution.setVariable("status", "OK");
	}

}
