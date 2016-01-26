package org.georchestra.extractorapp.ws.extractor.task;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.extractorapp.ws.extractor.ExtractorController;
import org.georchestra.extractorapp.ws.extractor.ExtractorLayerRequest;
import org.georchestra.extractorapp.ws.extractor.FileUtils;
import org.georchestra.extractorapp.ws.extractor.OversizedCoverageRequestException;
import org.georchestra.extractorapp.ws.extractor.RequestConfiguration;
import org.georchestra.extractorapp.ws.extractor.WcsExtractor;
import org.georchestra.extractorapp.ws.extractor.WfsExtractor;
import org.georchestra.extractorapp.ws.extractor.csw.CSWExtractor;
import org.json.JSONException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;


/**
 * Thread responsible for downloading all the data for a single request and
 * emailing the link for obtaining the data.
 *
 * @author jeichar
 */
public class ExtractionTask implements Runnable, Comparable<ExtractionTask> {
	private static final Log LOG = LogFactory.getLog(ExtractionTask.class
			.getPackage().getName());

	private static final int EXTRACTION_ATTEMPTS = 3;
	public final ExecutionMetadata executionMetadata;

	private RequestConfiguration requestConfig;

	public ExtractionTask(RequestConfiguration requestConfig)
			throws NoSuchAuthorityCodeException, MalformedURLException, JSONException, FactoryException {
		this.requestConfig = requestConfig;
		this.executionMetadata = new ExecutionMetadata(
				this.requestConfig.requestUuid,
				this.requestConfig.username,
				new Date(),
				this.requestConfig.strRequest);
	}
	public ExtractionTask(ExtractionTask toCopy) {

		this.requestConfig = toCopy.requestConfig;
		this.executionMetadata = toCopy.executionMetadata;
	}


	@Override
	public void run() {
		executionMetadata.setRunning();
		requestConfig.setThreadLocal();

		final File tmpDir = FileUtils.createTempDirectory();
		final File tmpExtractionBundle = mkDirTmpExtractionBundle(tmpDir, requestConfig.extractionFolderPrefix+requestConfig.requestUuid .toString());

		try {
			long start = System.currentTimeMillis();
			LOG.info("Starting extraction into directory: "
					+ tmpExtractionBundle);

			final File failureFile = new File(tmpExtractionBundle,
					"failures.html");
			final List<String> successes = new ArrayList<String>();
			final List<String> failures = new ArrayList<String>();
			final List<String> oversized = new ArrayList<String>();
			for (ExtractorLayerRequest request : requestConfig.requests) {

				int tries = 0;
				while (tries < EXTRACTION_ATTEMPTS) {

					tries++;
					String name = String.format("%s__%s",
							request._url.getHost(), request._layerName);
					File layerTmpDir = mkDirTmpExtractionBundle(tmpDir, name);
					LOG.info("Attempt " + tries + " for extracting layer: "
							+ request._url + " -- " + request._layerName);

					try {
						// extracts the layer in the temporal directory
						File newDir;
						switch (request._owsType) {
						case WCS:
							newDir = extractWcsLayer(request, layerTmpDir);
							break;
						case WFS:
							newDir = extractWfsLayer(request, layerTmpDir);
							break;
						default:
							throw new IllegalArgumentException(request._owsType
									+ " not supported");
						}
						// extracts the metadata into the temporal directory
						if(request._isoMetadataURL != null && !"".equals(request._isoMetadataURL) ){
							extractMetadata(request, newDir);
						}

						for (File from : layerTmpDir.listFiles()) {
							File to = new File(tmpExtractionBundle,
									from.getName());
							FileUtils.moveFile(from, to);
						}
						FileUtils.delete(layerTmpDir);
						LOG.info("Finished extracting layer: " + request._url
								+ " -- " + request._layerName);
						tries = EXTRACTION_ATTEMPTS + 1;
						successes.add(name);
					} catch (OversizedCoverageRequestException e) {
						tries = EXTRACTION_ATTEMPTS + 1; // don't re-try
						oversized.add(name);
						handleExtractionException(request, e, failureFile);
					} catch (SecurityException e) {
						tries = EXTRACTION_ATTEMPTS + 1; // don't re-try

						try {
							FileUtils.delete(layerTmpDir);
						} catch (Throwable t) { /* ignore */
						}

						failures.add(name);
						handleExtractionException(request, e, failureFile);
					} catch (Throwable e) {
						try {
							FileUtils.delete(layerTmpDir);
						} catch (Throwable t) { /* ignore */
						}

						if (tries >= EXTRACTION_ATTEMPTS) {
							failures.add(name);
							handleExtractionException(request, e, failureFile);
						}
					}
				}
			}

			closeFailuresFile(failureFile);

			File archive = archiveExtraction(tmpExtractionBundle);
			long fileSize = archive.length();
			long end = System.currentTimeMillis();

			String msg = String
					.format("Finished extraction into directory: %s achive is: %s (size : %s bytes) \nExtraction took %s",
							tmpExtractionBundle, archive, fileSize, time(start, end));
			LOG.info(msg);

			if (!requestConfig.testing) {
				try {
					requestConfig.email.sendDone(successes, failures, oversized, fileSize);
				} catch (Throwable e) {
					handleException(e);
				}
			} else if (requestConfig.testing && !failures.isEmpty()) {
				throw new RuntimeException(Arrays.toString(failures.toArray()));
			}
		} finally {
			executionMetadata.setCompleted();
			FileUtils.delete(tmpExtractionBundle);
			FileUtils.delete(tmpDir);

		}
	}

	private String time(long start, long end) {
		long seconds = (end - start) / 1000;
		if (seconds > 60) {
			long minutes = seconds / 60;
			seconds = seconds - (minutes * 60);
			if (minutes > 24) {
				long hours = minutes / 24;
				minutes = minutes - (hours * 24);
				return (hours + " hour " + minutes + " min");
			}
			return minutes + " min " + seconds + " sec";
		}

		return seconds + " seconds";
	}

	// ----------------- support methods ----------------- //
	/**
	 * Protected to allow unit test to override
	 * @throws AssertionError
	 * @throws IOException
	 */
	protected File mkDirTmpExtractionBundle(File tmpDir, String name) {

		File tmpExtractionBundle = new File(tmpDir, FileUtils.toSafeFileName(name));
		if (!tmpExtractionBundle.mkdirs() && !tmpExtractionBundle.exists()) {
			throw new RuntimeException("Unable to make directory: "+tmpExtractionBundle);
		}
		return tmpExtractionBundle;
	}

	/**
	 * Protected to allow unit test to override
	 *
	 * @return
	 */
	protected File archiveExtraction(File tmpExtractionBundle) {
		String filename = requestConfig.requestUuid.toString()
				+ ExtractorController.EXTRACTION_ZIP_EXT;
		File storageFile = FileUtils.storageFile(filename);
		if (!storageFile.getParentFile().exists()) {
			storageFile.getParentFile().mkdirs();
		}
		try {
			FileUtils.archiveToZip(tmpExtractionBundle, storageFile);
		} catch (IOException e1) {
			handleException(e1);
		}
		return storageFile;
	}

	private void handleExtractionException(ExtractorLayerRequest request,
			Throwable e, File failureFile) {
		if (!failureFile.getParentFile().exists()) {
			throw new AssertionError(
					"The temporary extraction bundle directory: "
							+ failureFile.getParentFile() + " does not exist");
		}

		String msg = "Exception occurred while extracting data";
		LOG.error(msg, e);

		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		openFailuresFile(failureFile);
		String message = String
				.format("<li>Erreur d'accès à la couche: %s \n"
						+ "  <ul>\n"
						+ "    <li>Serveur: %s</li>\n"
						+ "    <li>Couche: %s</li>\n"
						+ "    <li>Exception (à destination de l'administrateur): <![CDATA[%s]]></li>\n"
						+ "    <li><pre><code style=\"font-size: 1.2em;\"><![CDATA[%s]]></code></pre></li>\n"
						+ "  </ul>\n" + "</li>\n", request._layerName,
						request._url, request._layerName, e.getLocalizedMessage().substring(0, Math.min(200,
						e.getLocalizedMessage().length())).replaceAll("<","&lt;"),
						stackTrace.toString().replaceAll("<","&lt;"));
		writeToFile(failureFile, message, true);
	}

	private void openFailuresFile(File failureFile) {
		if (!failureFile.exists()) {
			String msg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<html>\n"
					+ "<head>\n"
					+ "<title>"
					+ "Erreurs lors de l'extraction"
					+ "</title>\n" +
					"<style type=\"text/css\">" +
					"html { margin: 0; padding: 0; }\n" +
					"body {  font: 75% georgia, sans-serif; line-height: 1.88889; color: #555753;  background: #fff url(blossoms.jpg) " +
					"no-repeat bottom right;  margin: 0;  padding: 0; }\n" +
					"p {  margin-top: 0;  text-align: justify; }\n" +
					"h3 {  font: italic normal 1.4em georgia, sans-serif; letter-spacing: 1px;  margin-bottom: 0;  color: #7D775C; }\n" +
					"a:link {  font-weight: bold;  text-decoration: none;  color: #B7A5DF; }\n" +
					"a:visited {  font-weight: bold;  text-decoration: none;  color: #D4CDDC; }\n" +
					"a:hover, a:focus, a:active {  text-decoration: underline;  color: #9685BA; }\n" +
					"abbr { border-bottom: none; }\n" +
					"\n" +
					"\n" +
					"/* specific divs */\n" +
					".page-wrapper {  background: url(zen-bg.jpg) no-repeat top left;  padding: 0 175px 0 110px;   margin: 0;  " +
					"position: relative; }\n" +
					"\n" +
					".intro {  min-width: 470px; width: 100%; }\n" +
					"\n" +
					"header h1 {  background: transparent url(h1.gif) no-repeat top left; margin-top: 10px; display: block; width: " +
					"219px; height: 87px; float: left; text-indent: 100%; white-space: nowrap; overflow: hidden; }\n" +
					"header h2 {  background: transparent url(h2.gif) no-repeat top left;  margin-top: 58px;  margin-bottom: 40px;  " +
					"width: 200px;  height: 18px;  float: right; text-indent: 100%; white-space: nowrap; overflow: hidden; }\n" +
					"header { padding-top: 20px; height: 87px;}\n" +
					"\n" +
					".summary { clear: both;  margin: 20px 20px 20px 10px;  width: 160px;  float: left; }\n" +
					".summary p { font: italic 1.1em/2.2 georgia;  text-align: center; }\n" +
					"\n" +
					".preamble { clear: right;  padding: 0px 10px 0 10px; }\n" +
					".supporting {\t padding-left: 10px;  margin-bottom: 40px; }\n" +
					"\n" +
					"footer {  text-align: center;  }\n" +
					"footer a:link, footer a:visited {  margin-right: 20px;  }\n" +
					"\n" +
					".sidebar { margin-left: 600px;  position: absolute;  top: 0;  right: 0; }\n" +
					".sidebar .wrapper {  font: 10px verdana, sans-serif;  background: transparent url(paper-bg.jpg) top left repeat-y;" +
					"  padding: 10px;  margin-top: 150px;  width: 130px;  }\n" +
					".sidebar h3.select {  background: transparent url(h3.gif) no-repeat top left;  margin: 10px 0 5px 0;  width: 97px;" +
					"  height: 16px; text-indent: 100%; white-space: nowrap; overflow: hidden; }\n" +
					".sidebar h3.archives {  background: transparent url(h5.gif) no-repeat top left;  margin: 25px 0 5px 0;  " +
					"width:57px;  height: 14px; text-indent: 100%; white-space: nowrap; overflow: hidden; }\n" +
					".sidebar h3.resources {  background: transparent url(h6.gif) no-repeat top left;  margin: 25px 0 5px 0;  " +
					"width:63px;  height: 10px; text-indent: 100%; white-space: nowrap; overflow: hidden; }\n" +
					"\n" +
					"\n" +
					".sidebar ul { margin: 0; padding: 0; }\n" +
					".sidebar li { line-height: 1.3em;  background: transparent url(cr1.gif) no-repeat top center;  display: block;  " +
					"padding-top: 5px;  margin-bottom: 5px; list-style-type: none; }\n" +
					".sidebar li a:link { color: #988F5E; }\n" +
					".sidebar li a:visited { color: #B3AE94; }\n" +
					"\n" +
					"\n" +
					".extra1 { background: transparent url(cr2.gif) top left no-repeat;  position: absolute;  top: 40px;  right: 0;  width: 148px;  height: 110px; }"
					+ "</style></head><body>\n"
					+ "L'extraction a échoué pour certaines données.  "
					+ "Contacter l'administrateur concernant les serveurs/couches suivants\n"
					+ "\n\nToutes les couches ont été contactées "
					+ EXTRACTION_ATTEMPTS
					+ " fois afin d'extraire les données.\n\n" + "<ul>";
			writeToFile(failureFile, msg, false);
		}
	}

	private void closeFailuresFile(File failureFile) {
		if (failureFile.exists()) {
			writeToFile(failureFile, "</ul></body></html>", true);
		}
	}

	private void writeToFile(File failureFile, String message, boolean append) {
		FileOutputStream writer = null;
		try {
			writer = new FileOutputStream(failureFile, append);
			writer.write(message.getBytes("UTF-8"));
		} catch (IOException e1) {
			handleException(e1);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e2) {
					handleException(e2);
				}
			}
		}
	}

	private void handleException(Throwable e1) {
		// TODO handle failure. What am I supposed to do about it?
		e1.printStackTrace();
	}


	/**
	 * Creates a directory which contains the extracted layers
	 *
	 * @param request
	 * @param requestBaseDir
	 * @return the directory that contain the layers
	 *
	 * @throws IOException
	 * @throws TransformException
	 * @throws FactoryException
	 */
	private File  extractWcsLayer(ExtractorLayerRequest request, File requestBaseDir)
			throws IOException, TransformException, FactoryException {

		WcsExtractor extractor = new WcsExtractor(requestBaseDir, requestConfig);

		extractor.checkPermission(request, requestConfig.secureHost, requestConfig.username, requestConfig.roles);

		return extractor.extract(request);
	}

	/**
	 * Creates a directory which contains the extracted layers
	 * @param request
	 * @param requestBaseDir
	 *
	 * @return the directory that contain the layers
	 *
	 * @throws IOException
	 * @throws TransformException
	 * @throws FactoryException
	 */
	private File extractWfsLayer(ExtractorLayerRequest request, File requestBaseDir)
			throws IOException, TransformException, FactoryException {

		WfsExtractor extractor = new WfsExtractor(requestBaseDir,
				requestConfig.adminCredentials.getUserName(),
				requestConfig.adminCredentials.getPassword(),
				requestConfig.secureHost,
				requestConfig.userAgent);

		extractor.checkPermission(request, requestConfig.secureHost, requestConfig.username, requestConfig.roles);

		return extractor.extract(request);
	}

	/**
	 * Extracts the layer's metadata and save it in the layer directory.
	 *
	 * @param request
	 * @param layerDirectory
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private void extractMetadata(final ExtractorLayerRequest request, final File layerDirectory) throws IOException {

		final String adminUserName = requestConfig.adminCredentials.getUserName();
		final String adminPassword = requestConfig.adminCredentials.getPassword();

		String cswHost = request._isoMetadataURL.getHost();

		CSWExtractor extractor = new CSWExtractor(layerDirectory, adminUserName, adminPassword, cswHost, requestConfig.userAgent);

		extractor.checkPermission(request, requestConfig.username, requestConfig.roles);

		extractor.extract(request._isoMetadataURL);
	}


	@Override
	public int compareTo(ExtractionTask other) {

		return other.executionMetadata.getPriority().compareTo(
				this.executionMetadata.getPriority());
// Replaced because this code order from low to high
//		return executionMetadata.getPriority().compareTo(
//				other.executionMetadata.getPriority());
	}

	public boolean equalId(String uuid) {
		return requestConfig.requestUuid.toString().equals(uuid);
	}
}
