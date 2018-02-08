package li.spectrum.ingestion.service;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import li.spectrum.data.dbclient.FileModelService;
import li.spectrum.data.model.File;
import li.spectrum.data.model.FileModel;
import li.spectrum.data.model.Folder;
import li.spectrum.data.model.Metadata;
import li.spectrum.data.model.Processing;
import li.spectrum.data.model.Record;

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

		Path path = Paths.get(fileRecord.getValue());
		File file = null;
		if (path.toFile().isDirectory()) {
			file = new Folder(path.toString());
		} else {
			file = new File(path.toString());
		}
		file.setName(path.getFileName().toString());
		Metadata meta = file.get_metadata();
		meta.setType(file.getClass().getSimpleName());
		meta.setUri(file.getCanonicalPath());

		Processing processing = new Processing();
		processing.setTaskName(this.getClass().getSimpleName());

		FileModel fm = new FileModel();
		fm.setFile(file);
		fm.setProcessing(processing);
		fm.setFilePath(file.getCanonicalPath());
		this.fileModelService.addOrUpdate(fm);

		execution.setVariable("status", "OK");
	}

}
