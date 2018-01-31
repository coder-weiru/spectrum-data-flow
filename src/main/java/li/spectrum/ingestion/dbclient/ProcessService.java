package li.spectrum.ingestion.dbclient;

import li.spectrum.ingestion.model.Proc;

public interface ProcessService {

	void add(Proc process);

	Proc get(String procId);
}
