package li.spectrum.ingestion.model;

import java.util.ArrayList;
import java.util.List;

import com.marklogic.client.pojo.annotation.Id;

public class Proc {
	@Id
	public String id;
	private String rootDir;
	private String timestamp;
	private List<File> files = new ArrayList<File>();
	private int totalFileCount;
	private Processing processing;

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

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public boolean addFile(File file) {
		return this.files.add(file);
	}

	public boolean addDir(Dir dir) {
		return this.files.add(dir);
	}

	public String getRootDir() {
		return rootDir;
	}

	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}

	public Processing getProcessing() {
		return processing;
	}

	public void setProcessing(Processing processing) {
		this.processing = processing;
	}

	public long getTotalFileCount() {
		return totalFileCount;
	}

	public void setTotalFileCount(int totalFileCount) {
		this.totalFileCount = totalFileCount;
	}

}
