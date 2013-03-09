package org.georchestra.mapfishapp.ws;

import java.io.File;
import java.io.FileFilter;
import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

final class PurgeDocsRunnable implements Runnable {
	
	private static final Log LOG = LogFactory.getLog(PurgeDocsRunnable.class.getPackage().getName());

	
	private final long maxDocAgeInMinutes;

	private final String tempDirectory;

	public PurgeDocsRunnable(final long maxDocAgeIn, final String tempDirectory) {
		this.maxDocAgeInMinutes = maxDocAgeIn;
		this.tempDirectory = tempDirectory;
	}

	public void run() {

		File dir = new File(this.tempDirectory);
		if (!dir.exists()) {
			throw new RuntimeException(this.tempDirectory+ " dir not found");
		}

		// prepare filter to get old files
		FileFilter filter = new FileFilter() {
			public boolean accept(File file) {

				// has to be a geodoc file
				if (file.getName().contains(A_DocService.DOC_PREFIX)) {
					long currentTime = System.currentTimeMillis();
					long lastModified = file.lastModified();

					// has to have a time life above TIMER_MIN minutes
					long maxMillis= safeConvertToMillis(maxDocAgeInMinutes) ;
					if ((currentTime - lastModified) > maxMillis ) {
						return true;
					}
				}
				return false;
			}

			/**
			 * Converts the value to milliseconds taking into account a possible overflow.
			 * If an overflow exception is found the returned value will be the <code> Long.MAX_VALUE </code>.
			 * 
			 * @param minutes
			 * @return milliseconds 
			 */
			private long safeConvertToMillis(final long minutes)   {
				
				long safeInteger = 0;
				try{
					BigInteger bigMilliseconds = (BigInteger.valueOf(60000)).multiply(BigInteger.valueOf(minutes)); 
					
					safeInteger =  longPreconditionCheck( bigMilliseconds );
					
				} catch(ArithmeticException e){
				
					final String message = "An overflow exception have occrurred transforming the configurated maxDocAgeInMinutes value:" + minutes
							+ ". The value should be less than " + Long.MAX_VALUE / 60000 + " Minutes";
					
					LOG.error(message);

					safeInteger = Long.MAX_VALUE; // set the default value 
				}
				return safeInteger; 
			}

			/**
			 * checks the integer preconditions
			 * 
			 * @param value 
			 * @return a safe long integer
			 * @throws ArithmeticException
			 */
			private long longPreconditionCheck(BigInteger value) throws ArithmeticException {
				
				if( (value.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) == -1  )  ||  (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1)) {
					throw  new ArithmeticException("Long overflow: " + value );
				}
				return value.longValue();
					
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
		result = prime * result
				+ (int) (maxDocAgeInMinutes ^ (maxDocAgeInMinutes >>> 32));
		result = prime * result
				+ ((tempDirectory == null) ? 0 : tempDirectory.hashCode());
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
		if (tempDirectory == null) {
			if (other.tempDirectory != null)
				return false;
		} else if (!tempDirectory.equals(other.tempDirectory))
			return false;
		return true;
	}
}