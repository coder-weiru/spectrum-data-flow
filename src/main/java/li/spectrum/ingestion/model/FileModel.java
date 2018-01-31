package li.spectrum.ingestion.model;

import com.marklogic.client.pojo.annotation.Id;

import li.spectrum.ingestion.tika.TikaDocument;

public class FileModel {

	@Id
	public String filePath;

	private TikaDocument tikaDocument;

	private Processing processing;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public TikaDocument getTikaDocument() {
		return tikaDocument;
	}

	public void setTikaDocument(TikaDocument tikaDocument) {
		this.tikaDocument = tikaDocument;
	}

	public Processing getProcessing() {
		return processing;
	}

	public void setProcessing(Processing processing) {
		this.processing = processing;
	}

}
