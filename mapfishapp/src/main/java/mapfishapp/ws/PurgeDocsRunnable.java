package mapfishapp.ws;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

final class PurgeDocsRunnable implements Runnable {
	
	private static final Log LOG = LogFactory.getLog(PurgeDocsRunnable.class.getPackage().getName());

	
	private final int maxDocAgeInMinutes;

	public PurgeDocsRunnable(int maxDocAgeIn) {
		this.maxDocAgeInMinutes = maxDocAgeIn;
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
					int maxMillis= safeConvertToMillis(maxDocAgeInMinutes) ;
					if (currentTime - lastModified > maxMillis ) {
						return true;
					}
				}
				return false;
			}

			/**
			 * Converts the value to milliseconds taking into account a possible overflow.
			 * If an overflow exception is found the returned value will be the <code> Integer.MAX_VALUE </code>.
			 * 
			 * @param minutes
			 * @return milliseconds
			 */
			private int safeConvertToMillis(final int minutes)   {
				
				long safeInteger = 0;
				try{
					safeInteger =  intPreconditionCheck( (long)minutes * 60000 );
					
				} catch(ArithmeticException e){
				
					final String message = "An overflow exception have occrurred transforming the configurated maxDocAgeInMinutes value:" + minutes
							+ ". The value should be less than " + Integer.MAX_VALUE / 60000 + " Minutes";
					
					LOG.error(message);

					safeInteger = Integer.MAX_VALUE; // set the default value 
				}
				return (int)safeInteger; // safe cast
			}

			/**
			 * checks the integer preconditions
			 * 
			 * @param value 
			 * @return a safe integer
			 * @throws ArithmeticException
			 */
			private long intPreconditionCheck(long value) throws ArithmeticException {
				
				if( (value < Integer.MIN_VALUE)  ||  (value > Integer.MAX_VALUE)){
					throw  new ArithmeticException("integer overflow: " + value );
				}
				return value;
					
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
		result = prime * result + maxDocAgeInMinutes;
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
		if (maxDocAgeInMinutes != other.maxDocAgeInMinutes)
			return false;
		return true;
	}

}