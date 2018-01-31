
package li.spectrum.ingestion.dbclient;

import java.util.HashMap;

import org.springframework.core.env.Environment;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;

import li.spectrum.ingestion.security.ClientRole;

/**
 * A HashMap of database client connections, with a ClientRole as key.
 * Enables a pool of clients for use by different security access levels.
 */
@SuppressWarnings("serial")
public class Clients extends HashMap<ClientRole, DatabaseClient> {

	/**
	 * Provided by Spring at startup, for accessing environment-specific variables.
	 */
	private Environment env;
	
	Clients(Environment env) {
		super();
		this.env = env;
		DatabaseClient writerClient = databaseClient(ClientRole.DOCUMENT_WRITER);
		DatabaseClient guestClient = databaseClient(ClientRole.DOCUMENT_READER);
		put(ClientRole.DOCUMENT_WRITER, writerClient);
		put(ClientRole.DOCUMENT_READER, guestClient);
	}
	
	/**
	 * Constructs a Java Client API database Client, of which
	 * this application uses two long-lived instances.
	 * @param role The security role for whom whom to construct a connection
	 * @return A DatabaseClient for accessing MarkLogic 
	 */
	private DatabaseClient databaseClient(ClientRole role) {
		String host = env.getProperty("marklogic.rest.host");
		Integer port = Integer.parseInt(env.getProperty("marklogic.rest.port"));
		String username = env.getProperty(role.getUserParam());
		String password = env.getProperty(role.getPasswordParam());

		return DatabaseClientFactory.newClient(host, port, new DigestAuthContext(username, password));
	}
}
