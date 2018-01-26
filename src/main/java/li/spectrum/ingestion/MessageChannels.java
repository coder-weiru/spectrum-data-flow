package li.spectrum.ingestion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;

@Configuration
public class MessageChannels {
	@Bean
	DirectChannel requests() {
		return new DirectChannel();
	}

	@Bean
	DirectChannel replies() {
		return new DirectChannel();
	}

}
