package li.spectrum.ingestion;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({ AppConfig.class, MessageChannels.class })
public class SimpleRequest {

	@Autowired
	private ProcessEngine processEngine;

	Map<String, String> launch() throws ParseException {
		String pName = processEngine.getName();
		String ver = ProcessEngine.VERSION;
		System.out.println("ProcessEngine [" + pName + "] Version: [" + ver + "]");

		RepositoryService repositoryService = processEngine.getRepositoryService();
		Deployment deployment = repositoryService.createDeployment()
				.addClasspathResource("processes/simple.bpmn20.xml")
				.deploy();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.deploymentId(deployment.getId()).singleResult();
		System.out.println("Found process definition [" + processDefinition.getName() + "] with id ["
				+ processDefinition.getId() + "]");

		RuntimeService runtimeService = processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simple");
		System.out.println("Simple process started with process instance id ["
				+ processInstance.getProcessInstanceId() + "] key [" + processInstance.getProcessDefinitionKey() + "]");

		TaskService taskService = processEngine.getTaskService();
		FormService formService = processEngine.getFormService();
		HistoryService historyService = processEngine.getHistoryService();

		Scanner scanner = new Scanner(System.in);
		while (processInstance != null && !processInstance.isEnded()) {
			List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
			System.out.println("Active outstanding tasks: [" + tasks.size() + "]");
			for (int i = 0; i < tasks.size(); i++) {
				Task task = tasks.get(i);
				System.out.println("Processing Task [" + task.getName() + "]");
				Map<String, Object> variables = new HashMap<String, Object>();
				FormData formData = formService.getTaskFormData(task.getId());
				for (FormProperty formProperty : formData.getFormProperties()) {
					if (StringFormType.class.isInstance(formProperty.getType())) {
						System.out.println(formProperty.getName() + "?");
						String value = scanner.nextLine();
						variables.put(formProperty.getId(), value);
					} else {
						System.out.println("<form type not supported>");
					}
				}
				taskService.complete(task.getId(), variables);

				HistoricActivityInstance endActivity = null;
				List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
						.processInstanceId(processInstance.getId()).finished().orderByHistoricActivityInstanceEndTime()
						.asc().list();
				for (HistoricActivityInstance activity : activities) {
					if (activity.getActivityType() == "startEvent") {
						System.out.println("BEGIN " + processDefinition.getName() + " ["
								+ processInstance.getProcessDefinitionKey() + "] " + activity.getStartTime());
					}
					if (activity.getActivityType() == "endEvent") {
						// Handle edge case where end step happens so fast that
						// the end step
						// and previous step(s) are sorted the same. So, cache
						// the end step
						// and display it last to represent the logical
						// sequence.
						endActivity = activity;
					} else {
						System.out.println("-- " + activity.getActivityName() + " [" + activity.getActivityId() + "] "
								+ activity.getDurationInMillis() + " ms");
					}
				}
				if (endActivity != null) {
					System.out.println("-- " + endActivity.getActivityName() + " [" + endActivity.getActivityId() + "] "
							+ endActivity.getDurationInMillis() + " ms");
					System.out.println("COMPLETE " + processDefinition.getName() + " ["
							+ processInstance.getProcessDefinitionKey() + "] " + endActivity.getEndTime());
				}
			}
			// Re-query the process instance, making sure the latest state is
			// available
			processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId())
					.singleResult();
		}
		scanner.close();
		return Collections.singletonMap("executionId", processInstance.getId());
	}

}
