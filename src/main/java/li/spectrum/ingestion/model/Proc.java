package li.spectrum.ingestion.model;

import java.util.ArrayList;
import java.util.List;

import com.marklogic.client.pojo.annotation.Id;

public class Proc {
	@Id
	public String id;
	private String rootDir;
	private String timestamp;
	private List<String> files = new ArrayList<String>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	public boolean addFile(String filePath) {
		return this.files.add(filePath);
	}

	public String getRootDir() {
		return rootDir;
	}

	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}

}
