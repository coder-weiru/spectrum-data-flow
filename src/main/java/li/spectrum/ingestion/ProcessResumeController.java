package li.spectrum.ingestion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessResumeController {

	@Autowired
	private MessageChannels messageChannels;

	@RequestMapping(method = RequestMethod.GET, value = "/resume/{executionId}")
	void resume(@PathVariable String executionId) {
		Message<String> build = MessageBuilder.withPayload(executionId).setHeader("executionId", executionId).build();
		this.messageChannels.replies().send(build);
	}
}