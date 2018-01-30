package li.spectrum.ingestion.dbclient;

import java.io.InputStream;

public interface DocumentService {

	Document add(String docId, InputStream content, String collectionName);
}
