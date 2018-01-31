package li.spectrum.ingestion.dbclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.pojo.PojoRepository;

import li.spectrum.ingestion.model.FileModel;
import li.spectrum.ingestion.model.Proc;
import li.spectrum.ingestion.security.ClientRole;


@Component
@ComponentScan
@PropertySource("classpath:application.properties")
public class DatabaseContext {

	/** Spring provides this object at startup, for access to environment configuration
	 */
	@Autowired
	private Environment env;
	
	/**
	 * Makes a HashMap of Client objects available to the application.
	 * @return A Clients class, which extends HashMap<ClientRole, DatabaseClient>;
	 */
	@Bean
	public Clients clients() {
		Clients clients = new Clients(env);
		return clients;
	}
	
	/**
	 * This repository object manages operations for the Document POJO Class.
	 * Generally accessed through calls to the DocumentService, which mediates
	 * and limits some of the access.
	 * 
	 * @return A PojoRepository object to manage Documents.
	 */
	@Bean
	public PojoRepository<Document, String> genericDocumentRepository() {
		return clients().get(ClientRole.DOCUMENT_WRITER)
				.newPojoRepository(Document.class, String.class);
	}
	
	/**
	 * This repository object manages operations for the Proc POJO Class.
	 * Generally accessed through calls to the ProcessService, which mediates
	 * and limits some of the access.
	 * 
	 * @return A PojoRepository object to manage processes.
	 */
	@Bean
	public PojoRepository<Proc, String> processRepository() {
		return clients().get(ClientRole.DOCUMENT_WRITER).newPojoRepository(Proc.class, String.class);
	}

	/**
	 * This repository object manages operations for the FileModel POJO Class.
	 * Generally accessed through calls to the FileModelService, which mediates
	 * and limits some of the access.
	 * 
	 * @return A PojoRepository object to manage file models.
	 */
	@Bean
	public PojoRepository<FileModel, String> fileModelRepository() {
		return clients().get(ClientRole.DOCUMENT_WRITER).newPojoRepository(FileModel.class, String.class);
	}

	/**
	 * Initializes a singleton ObjectMapper.
	 * 
	 * @return A Jackson ObjectMapper implementation for the Spring IoC
	 *         container
	 */
	@Bean
	public ObjectMapper mapper() {
		return new CustomObjectMapper();
	}
	
}
