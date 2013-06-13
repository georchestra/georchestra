import com.yahoo.platform.yui.compressor.CssCompressor
import com.yahoo.platform.yui.compressor.JavaScriptCompressor

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.io.*

/**
 * Use YUI to minify javascript or css files into a single file. (Obviously css and js files should not be mixed
 * Example:
 * <pre><code>
 * new Minify(
 * &nbsp;&nbspsources: [
 * &nbsp;&nbsp&nbsp;&nbspnew FileSet().descendants(
 * &nbsp;&nbsp&nbsp;&nbsp&nbsp;&nbspsource:"$basedirFile/src/main/resources/georchestra/js", 
 * &nbsp;&nbsp&nbsp;&nbsp&nbsp;&nbspfilter:{ it.name.endsWith("*.js") }
 * &nbsp;&nbsp&nbsp;&nbsp)
 * &nbsp;&nbsp],
 * &nbsp;&nbspoutput: "$targetDir/classes/apps/georchestra/js/Minified.js")
 * }
 * </code></pre>
 */
class Minify {
	/** NOT API */
	static final String TAG = "[YUI minification]"
	/**
	 * A list of FileSet objects. There must be at least one source
	 */
	List sources
	/**
	 * The output file.  Can be a string or file object
	 */
	def output
	/**
	 * if true then we are minifying javascript (default)
	 * if false then we are minifying css
	 */
	boolean js = true
	/**
	 * The charset to use for reading the files
	 */
	String charset = "UTF-8"
	/**
	 * if true then output extra debug information
	 */
	boolean verbose = false
	/**
	 * munge the variable names.  Will result in smaller files
	 */
	boolean munge = true
	/**
	 * Don't do all the optimizations for the minification. Good for debugging.  
	 */
	boolean disableOptimizations = false
	/**
	 * Keep all semicolons in minified file
	 */
	boolean preserveAllSemiColons = true
	/**
	 * how wide to allow a line in the minified file
	 */
	int linebreakpos  = 8000

	/**
	 * Perform the minification
	 */
	def execute() {
		def params = Parameters.get
		def log = params.log
		log.info("Beginning minification for $output")
		
		File outputFile
		if(output instanceof File) {
			outputFile = output
		} else {
			def tmp = new File( output.toString().replace("/", File.separator))
			if (tmp.isAbsolute()) {
				outputFile = tmp
			} else {
				outputFile = new File(params.outputDir, tmp.path)
			}
		}

		outputFile.parentFile.mkdirs()
		outputFile.withWriter(charset, { writer ->
			sources.each { it.each { file ->
				log.info("$TAG compressing: $file") 
				file.withReader( charset, { reader ->
					if (js) {
						jsCompress(file, reader, writer, log)
					} else {
						cssCompress(reader, writer, log)
					}
				})
			}}
		})
		log.info("Done minification for $output")
	}

	/** NOT API */
	private formatLog (File file, def message, def sourceName, def line, def lineSource, def lineOffset) {
		if (line < 0) {
			return " $TAG $file:$message"
		} else {
			return " $TAG $file:$line:$lineOffset:$message"
		}
	}
	
	/** NOT API */
	private void jsCompress(File file, Reader reader, Writer writer, def log) {
		def compressor = new JavaScriptCompressor(reader, [

			warning: { message, sourceName, line, lineSource, lineOffset ->
				log.warning(formatLog(file, message, sourceName, line, lineSource, lineOffset))
			},

			error: { message, sourceName, line, lineSource, lineOffset ->
				log.error(formatLog(file, message, sourceName, line, lineSource, lineOffset))
			},

			runtimeError : { message, sourceName, line, lineSource, lineOffset ->
				log.error(formatLog(file, message, sourceName, line, lineSource, lineOffset))
				return new EvaluatorException(message)
			}
		] as ErrorReporter)

		compressor.compress(writer, linebreakpos, munge, verbose, preserveAllSemiColons, disableOptimizations)
	}

	/** NOT API */
	private void cssCompress(Reader reader, Writer writer, def log) {
		CssCompressor compressor = new CssCompressor(reader)
		compressor.compress(writer, linebreakpos)
	}
}