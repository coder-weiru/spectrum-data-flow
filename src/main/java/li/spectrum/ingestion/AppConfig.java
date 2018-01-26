package li.spectrum.ingestion;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@Configuration
public class AppConfig {

	private Log log = LogFactory.getLog(getClass());

	@Autowired
	Environment environment;

	@Bean
	ProcessEngine processEngine() {
		ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
				.setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000").setJdbcUsername("sa").setJdbcPassword("")
				.setJdbcDriver("org.h2.Driver")
				.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		return cfg.buildProcessEngine();
	}

	@Bean
	ActivityBehavior gateway(MessageChannels channels) {
		return new ReceiveTaskActivityBehavior() {

			private static final long serialVersionUID = 1L;

			@Override
			public void execute(DelegateExecution execution) {

				Message<?> executionMessage = MessageBuilder.withPayload(execution)
						.setHeader("executionId", execution.getId()).build();

				channels.requests().send(executionMessage);
			}
		};
	}

	@Bean
	IntegrationFlow requestsFlow(MessageChannels channels) {
		return IntegrationFlows.from(channels.requests())
				.handle(msg -> msg.getHeaders().entrySet().forEach(e -> log.info(e.getKey() + '=' + e.getValue())))
				.get();
	}

	@Bean
	IntegrationFlow repliesFlow(MessageChannels channels, ProcessEngine engine) {
		return IntegrationFlows.from(channels.replies()).handle(msg -> engine.getRuntimeService()
				.signalEventReceived(String.class.cast(msg.getHeaders().get("executionId")))).get();
	}

}