package li.spectrum.ingestion;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import li.spectrum.ingestion.dbclient.DocumentService;
import li.spectrum.ingestion.dbclient.MarkLogicDocumentService;
import li.spectrum.ingestion.dbclient.MarkLogicProcessService;
import li.spectrum.ingestion.dbclient.ProcessService;
import li.spectrum.ingestion.service.ListFileDelegate;
import li.spectrum.ingestion.service.TikaExtractionDelegate;
import li.spectrum.ingestion.tika.TikaExtractor;
import li.spectrum.ingestion.tika.TikaParser;

@Configuration
public class AppConfig {
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
	ListFileDelegate listFile() {
		return new ListFileDelegate(processService());
	}

	@Bean
	TikaExtractionDelegate tika() throws TikaException {
		return new TikaExtractionDelegate(processService(), tikaExtractor());
	}

	@Bean
	DocumentService documentService() {
		return new MarkLogicDocumentService();
	}

	@Bean
	ProcessService processService() {
		return new MarkLogicProcessService();
	}

	@Bean
	TikaExtractor tikaExtractor() throws TikaException {
		return new TikaExtractor(tikaParser());
	}

	@Bean
	TikaParser tikaParser() throws TikaException {
		return new TikaParser(null);
	}
}