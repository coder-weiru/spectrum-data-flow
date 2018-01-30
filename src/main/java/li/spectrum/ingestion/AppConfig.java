package li.spectrum.ingestion;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import li.spectrum.ingestion.dbclient.DocumentService;
import li.spectrum.ingestion.dbclient.MarkLogicDocumentService;
import li.spectrum.ingestion.service.ListFileDelegate;

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
		return new ListFileDelegate(documentService());
	}

	@Bean
	DocumentService documentService() {
		return new MarkLogicDocumentService();
	}
}