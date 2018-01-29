package li.spectrum.ingestion;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpectrumApplication {

	private static Logger logger = LoggerFactory.getLogger(SpectrumApplication.class);

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

	/*
	@Bean
	public CommandLineRunner init(final RepositoryService repositoryService, final RuntimeService runtimeService,
			final TaskService taskService) {

		return new CommandLineRunner() {
			@Override
			public void run(String... strings) throws Exception {
				Map<String, Object> variables = new HashMap<String, Object>();
				variables.put("rootDirectory", "C:/Data");

				Deployment deployment = repositoryService.createDeployment()
						.addClasspathResource("processes/simple.bpmn20.xml").deploy();
				ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
						.deploymentId(deployment.getId()).singleResult();
				logger.info("Found process definition [" + processDefinition.getName() + "] with id ["
						+ processDefinition.getId() + "]");

				runtimeService.startProcessInstanceByKey("simple", variables);
			}
		};

	}
	*/

}