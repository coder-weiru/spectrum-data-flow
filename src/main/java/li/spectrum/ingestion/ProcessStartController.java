package li.spectrum.ingestion;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import li.spectrum.data.dbclient.ProcessService;
import li.spectrum.data.model.Proc;
import li.spectrum.data.model.Processing;
import li.spectrum.data.utils.IdGenerator;

@RestController
class ProcessStartController {

	private static Logger logger = LoggerFactory.getLogger(ProcessStartController.class);

	@Autowired
	private ProcessEngine processEngine;

	@Autowired
	private volatile ProcessService processService;
	
	/**
	 * Will construct this instance using provided {@link ProcessService}
	 *
	 * @param processService
	 *            The process service.
	 */
	@Autowired
	public ProcessStartController(ProcessService processService) {
		Assert.notNull(processService, "'processService' must not be null");
		this.processService = processService;
	}

	@RequestMapping(value = "/start", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	Map<String, Object> launch(@RequestBody Proc proc) {
		String pName = processEngine.getName();
		String ver = ProcessEngine.VERSION;
		logger.info("ProcessEngine [" + pName + "] Version: [" + ver + "]");

		RepositoryService repositoryService = processEngine.getRepositoryService();
		Deployment deployment = repositoryService.createDeployment()
				.addClasspathResource("processes/main.bpmn20.xml")
				.addClasspathResource("processes/fileprocess.bpmn20.xml")
				.deploy();
		List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
				.deploymentId(deployment.getId()).list();

		printProcessDefinitions(processDefinitions);
		startProc(proc);

		RuntimeService runtimeService = processEngine.getRuntimeService();

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("rootDir", proc.getRootDir());
		variables.put("processId", proc.getId());

		ProcessInstance asyncProcess = runtimeService.startProcessInstanceByKey("main", variables);


		Map<String, Object> map = new HashMap<String, Object>();
		map.put("executionId", asyncProcess.getId());
		map.put("processId", proc.getId());
		return map;
	}

	private void startProc(Proc proc) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String timestampId = IdGenerator.generateTimestampId(timestamp);
		String id = timestampId + "_" + IdGenerator.generateDeterministicId(this.getClass().getName(), timestampId);

		proc.setId(id);
		proc.setTimestamp(timestampId);

		Processing processing = new Processing();
		processing.setTaskName(this.getClass().getSimpleName());

		proc.setProcessing(processing);

		this.processService.addOrUpdate(proc);
	}
	
	private void printProcessDefinitions(List<ProcessDefinition> processDefinitions) {
		StringBuilder sb = new StringBuilder();
		for (ProcessDefinition pd : processDefinitions) {
			sb.append(String.format("[%s] with id [%s]  ", pd.getName(), pd.getId()));
		}
		logger.info("Found process definition : " + sb.toString());
	}
}