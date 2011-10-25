package mapfishapp.ws;

import java.io.File;
import java.io.FileFilter;

final class PurgeDocsRunnable implements Runnable {
	private final int maxDocAgeIn;

	public PurgeDocsRunnable(int maxDocAgeIn) {
		this.maxDocAgeIn = maxDocAgeIn;
	}

	public void run() {

		File dir = new File(A_DocService.DIR_PATH);
		if (!dir.exists()) {
			throw new RuntimeException(A_DocService.DIR_PATH + " dir not found");
		}

		// prepare filter to get old files
		FileFilter filter = new FileFilter() {
			public boolean accept(File file) {

				// has to be a geodoc file
				if (file.getName().contains(A_DocService.DOC_PREFIX)) {
					long currentTime = System.currentTimeMillis();
					long lastModified = file.lastModified();

					// has to have a time life above TIMER_MIN minutes
					if (currentTime - lastModified > maxDocAgeIn * 60 * 1000) {
						return true;
					}
				}
				return false;
			}
		};

		// get files thanks to the previous filter
		File[] fileList = dir.listFiles(filter);

		// delete them
		for (File file : fileList) {
			A_DocService.LOG.info("Deleting expired doc: " + file);
			if (!file.delete()) {
				throw new RuntimeException("Could not delete file: "
						+ file.getPath());
			}
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + maxDocAgeIn;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PurgeDocsRunnable other = (PurgeDocsRunnable) obj;
		if (maxDocAgeIn != other.maxDocAgeIn)
			return false;
		return true;
	}

}