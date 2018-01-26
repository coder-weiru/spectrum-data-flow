package li.spectrum.ingestion;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpectrumApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpectrumApplication.class, args);
	}

	@Bean
	InitializingBean usersAndGroupsInitializer(final IdentityService identityService) {

		return new InitializingBean() {
			public void afterPropertiesSet() throws Exception {

				Group group = identityService.newGroup("user");
				group.setName("users");
				group.setType("security-role");
				identityService.saveGroup(group);

				User admin = identityService.newUser("admin");
				admin.setPassword("admin");
				identityService.saveUser(admin);

			}
		};
	}

	@Bean
	public CommandLineRunner init(final RepositoryService repositoryService, final RuntimeService runtimeService,
			final TaskService taskService) {

		return new CommandLineRunner() {
			@Override
			public void run(String... strings) throws Exception {
				Map<String, Object> variables = new HashMap<String, Object>();
				variables.put("applicantName", "John Doe");
				variables.put("email", "john.doe@activiti.com");
				variables.put("phoneNumber", "123456789");

				Deployment deployment = repositoryService.createDeployment()
						.addClasspathResource("processes/simple.bpmn20.xml").deploy();
				ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
						.deploymentId(deployment.getId()).singleResult();
				System.out.println("Found process definition [" + processDefinition.getName() + "] with id ["
						+ processDefinition.getId() + "]");

				runtimeService.startProcessInstanceByKey("simple", variables);
			}
		};

	}
}