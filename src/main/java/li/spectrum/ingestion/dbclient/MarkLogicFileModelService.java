package li.spectrum.ingestion.dbclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.pojo.PojoRepository;

import li.spectrum.ingestion.model.FileModel;

@Component
public class MarkLogicFileModelService extends MarkLogicBaseService implements FileModelService {

	@Value("${marklogic.collection.file}")
	private String collectionName;

	@Autowired
	private PojoRepository<FileModel, String> repository;

	private final Logger logger = LoggerFactory.getLogger(MarkLogicFileModelService.class);

	@Override
	public FileModel get(String id) {
		try {
			return repository.read(id);
		} catch (ResourceNotFoundException ex) {
			return null;
		}
	}

	@Override
	public void add(FileModel file) {
		logger.info("Adding FileModel with id {}", file.getFilePath());
		repository.write(file, collectionName);
	}

}
