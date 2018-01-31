package li.spectrum.ingestion.service;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import li.spectrum.ingestion.dbclient.ProcessService;
import li.spectrum.ingestion.model.Proc;
import li.spectrum.ingestion.utils.IdGenerator;

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

		proc.setFiles(listFile(rootDir));

		this.processService.add(proc);

		execution.setVariable("processId", id);
	}

	private List<String> listFile(String dirPath) {
		File f = new File(dirPath);
		File[] files = f.listFiles();
		List<String> pathes = new ArrayList<String>();
		if (files != null)
			for (int i = 0; i < files.length; i++) {
				File file = files[i];

				if (file.isDirectory()) {
					pathes.addAll(listFile(file.getAbsolutePath()));
				} else {
					pathes.add(file.getAbsolutePath());
				}
			}
		return pathes;
	}

}
