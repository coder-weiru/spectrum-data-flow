package li.spectrum.ingestion.integration;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@Configuration
public class IntegrationContext {

	private static Logger logger = LoggerFactory.getLogger(IntegrationContext.class);

	@Autowired
	Environment environment;

	@Bean
	IntegrationFlow requestsFlow(MessageChannels channels) {
		logger.info("IntegrationFlow : [requestsFlow]");

		return IntegrationFlows.from(channels.requests())
				.handle(msg -> msg.getHeaders().entrySet().forEach(e -> logger.info(e.getKey() + '=' + e.getValue())))
				.get();
	}

	@Bean
	IntegrationFlow repliesFlow(MessageChannels channels, ProcessEngine engine) {
		logger.info("IntegrationFlow : [repliesFlow]");

		return IntegrationFlows.from(channels.replies()).handle(msg -> engine.getRuntimeService()
				.signalEventReceived(String.class.cast(msg.getHeaders().get("executionId")))).get();
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
}
