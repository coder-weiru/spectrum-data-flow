package li.spectrum.ingestion.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import li.spectrum.data.dbclient.ProcessService;
import li.spectrum.data.model.Proc;
import li.spectrum.data.model.Processing;
import li.spectrum.data.model.Record;

/**
 * {@code ListFileDelegate} is a Java task responsible for “ls dir”.
 */
public class ListFileDelegate implements JavaDelegate {

	private static Logger logger = LoggerFactory.getLogger(ListFileDelegate.class);

	private volatile ProcessService processService;

	private List<Record> filesToProcess;

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
		String procId = (String) execution.getVariable("processId");
		logger.debug("Continuing process: [" + procId + "]");

		Proc proc = processService.get(procId);

		Processing processing = new Processing();
		processing.setTaskName(this.getClass().getSimpleName());

		String rootDir = proc.getRootDir();
		try {
			filesToProcess = listFile(rootDir);
			proc.setRecords(filesToProcess);
			proc.setTotalRecordCount(filesToProcess.size());

		} catch (IOException e) {
			logger.error("Error listing file {}.", e);
			processing.setException(e);
			processing.setStatus("ERROR");
		}

		proc.setProcessing(processing);

		this.processService.addOrUpdate(proc);
	}

	private List<Record> listFile(String dirPath) throws IOException {
		List<Record> list = new ArrayList<Record>();
		Record rootDir = new Record(dirPath);
		list.add(rootDir);
		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dirPath));
		for (Path path : directoryStream) {
			if (path.toFile().isDirectory()) {
				list.addAll(listFile(path.toString()));
			} else {
				Record file = new Record(path.toString());
				list.add(file);
			}
		}
		return list;
	}

	public List<Record> getFilesToProcess() {
		return filesToProcess;
	}

}
