package li.spectrum.ingestion.model;

public class Dir extends File {

	public Dir() {
		super();
	}

	public Dir(String path) {
		super(path);
	}

	private int fileCount;
	private int dirCount;


	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public int getDirCount() {
		return dirCount;
	}

	public void setDirCount(int dirCount) {
		this.dirCount = dirCount;
	}

}
