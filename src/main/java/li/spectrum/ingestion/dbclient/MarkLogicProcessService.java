package li.spectrum.ingestion.dbclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.pojo.PojoRepository;

import li.spectrum.ingestion.model.Proc;

@Component
public class MarkLogicProcessService extends MarkLogicBaseService implements ProcessService {

	@Value("${marklogic.collection.proc}")
	private String collectionName;

	@Autowired
	private PojoRepository<Proc, String> repository;

	private final Logger logger = LoggerFactory.getLogger(MarkLogicProcessService.class);



	@Override
	public Proc get(String procId) {
		try {
			return repository.read(procId);
		} catch (ResourceNotFoundException ex) {
			return null;
		}
	}

	@Override
	public void add(Proc process) {
		logger.info("Adding process with id {}", process.getId());
		repository.write(process, collectionName);
	}

}
