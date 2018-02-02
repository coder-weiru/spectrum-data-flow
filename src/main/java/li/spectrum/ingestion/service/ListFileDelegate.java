package li.spectrum.ingestion.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import li.spectrum.data.dbclient.ProcessService;
import li.spectrum.data.model.File;
import li.spectrum.data.model.Folder;
import li.spectrum.data.model.Proc;
import li.spectrum.data.model.Processing;
import li.spectrum.data.utils.IdGenerator;

/**
 * {@code ListFileDelegate} is a Java task responsible for “ls dir”.
 */
public class ListFileDelegate implements JavaDelegate {

	private static Logger logger = LoggerFactory.getLogger(ListFileDelegate.class);

	private volatile ProcessService processService;

	/**
	 * Will construct this instance using provided {@link ProcessService}
	 *
	 * @param processService
	 *            The process service.
	 */
	@Autowired
	public ListFileDelegate(ProcessService processService) {
		Assert.notNull(processService, "'processService' must not be null");

		this.processService = processService;
	}

	@Override
	public void execute(DelegateExecution execution) {
		String rootDir = (String) execution.getVariable("rootDir");
		logger.debug("Listing root directory: " + rootDir);

		Proc proc = new Proc();

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String timestampId = IdGenerator.generateTimestampId(timestamp);
		String id = timestampId + "_" + IdGenerator.generateDeterministicId(this.getClass().getName(), timestampId);

		proc.setId(id);
		proc.setRootDir(rootDir);
		proc.setTimestamp(timestampId);

		Processing processing = new Processing();
		processing.setTaskName(this.getClass().getSimpleName());

		try {
			List<File> toProcess = listFile(rootDir);
			proc.setFiles(toProcess);
			proc.setTotalFileCount(toProcess.size());

		} catch (IOException e) {
			logger.error("Error listing file {}.", e);
			processing.setException(e);
			processing.setStatus("ERROR");
		}

		proc.setProcessing(processing);

		this.processService.add(proc);

		execution.setVariable("processId", id);
	}

	private List<File> listFile(String dirPath) throws IOException {
		List<File> list = new ArrayList<File>();
		Folder rootDir = new Folder(dirPath);
		list.add(rootDir);
		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dirPath));
		for (Path path : directoryStream) {
			if (path.toFile().isDirectory()) {
				rootDir.setFolderCount(rootDir.getFolderCount() + 1);
				list.addAll(listFile(path.toString()));
			} else {
				rootDir.setFileCount(rootDir.getFileCount() + 1);
				File file = new File(path.toString());
				list.add(file);
			}
		}
		return list;
	}

}
