package li.spectrum.ingestion.dbclient;

import li.spectrum.ingestion.model.FileModel;

public interface FileModelService {

	void add(FileModel file);

	FileModel get(String id);
}
