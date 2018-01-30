package li.spectrum.ingestion;

import java.util.Collections;
import java.util.HashMap;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import li.spectrum.ingestion.model.Proc;

@RestController
class ProcessStartController {

	private static Logger logger = LoggerFactory.getLogger(ProcessStartController.class);

	@Autowired
	private ProcessEngine processEngine;

	@RequestMapping(value = "/start", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	Map<String, String> launch(@RequestBody Proc proc) {
		String pName = processEngine.getName();
		String ver = ProcessEngine.VERSION;
		logger.info("ProcessEngine [" + pName + "] Version: [" + ver + "]");

		RepositoryService repositoryService = processEngine.getRepositoryService();
		Deployment deployment = repositoryService.createDeployment().addClasspathResource("processes/simple.bpmn20.xml")
				.deploy();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.deploymentId(deployment.getId()).singleResult();
		logger.info("Found process definition [" + processDefinition.getName() + "] with id ["
				+ processDefinition.getId() + "]");

		RuntimeService runtimeService = processEngine.getRuntimeService();

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("rootDir", proc.getRootDir());

		ProcessInstance asyncProcess = runtimeService.startProcessInstanceByKey("simple", variables);
		return Collections.singletonMap("executionId", asyncProcess.getId());
	}
}