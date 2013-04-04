/**
 * Represents a set of files.  Can be the files in a directory, a single file or all descendants of a directory.
 * If the file set contains several files a sort and a filter can be applied to the files
 * 
 * <b>Note:</b> sorting the files requires loading all the files into memory and sorting.  This is both slower and 
 * requires more memory.
 *
 * <b>Note:</b> Sorting only applies to a single source.  Not to all files in the file set.
 
 * Examples:
 *  <pre><code>
 *  // Represents all js files that are descendants of
 *  // $basedirFile/src/main/resources/georchestra/js
 *  // all directories are recursively visited
 *  new FileSet().descendants(
 *  &nbsp;&nbspsource:"$basedirFile/src/main/resources/georchestra/js", 
 *  &nbsp;&nbspfilter:{ it.name.endsWith("*.js") }
 *	)
 *  
 *  // Represents a single file
 *  new FileSet().file("App.js")
 *
 *  // Represents the js files directly (not recursively) in the 
 *  // "web-client/src/main/resources/app/search/js" of the geonetwork project
 *  // files are sorted by lastmodified date
 *  new FileSet(project: "geonetwork").children(
 *  &nbsp;&nbspsource: "web-client/src/main/resources/app/search/js",
 *  &nbsp;&nbspfilter: {it.name.endsWith("*js")},
 *  &nbsp;&nbspsort: {o1, o2 -> o1.lastModified() - o2.lastModified}
 *  )
 *
 *  // A fileset with first App.js then all js files in geonetwork directory
 *  new FileSet().
 *  &nbsp;&nbspfile("App.js").
 *  &nbsp;&nbspchildren(
 *  &nbsp;&nbsp&nbsp;&nbspsource:"geonetwork", 
 *  &nbsp;&nbsp&nbsp;&nbspfilter: {it.name.endsWith(".js)}
 *  &nbsp;&nbsp)
 *  </code></pre>
 *
 * The each method can be used to iterate through all the files and perform an action 
 * on each file in the FileSet
 *
 */
class FileSet {
	/**
	 * The project to use as the base of the source paths.  If null then the source
	 * will be interpretted without any modification.
	 */
	String project
	/** NOT API */
	private def sources = []

	/**
	 * Iterate through all files in FileSet and pass each to the closure
	 */
	def each(Closure c) {
		sources.each {
			it.eachFile(c) 
		}
	}
	
	/** NOT API */
	private def resolve(def source) {
		def s
		
		if (source instanceof File) {
			s = source
		} else {
			s = new File(source.toString())
		}
		def outFile
		if(project != null) {
			outFile = new File(Parameters.get.basedirFile.parent+("/$project/$s").replace('/', File.separator))
		} else {
			outFile = s
		}
		
		if (!outFile.exists()) {
			throw new AssertionError("$outFile does not exist. Check definition of FileSet")
		}
		
		return outFile

	}
	
	/**
	 * Recursively search for all children of the source.
	 *
	 * @param source the root of the file set. Must be a directory
	 * @param filter 
	 *		 each file is passed to filter.  
	 * 		 If filter returns true then the file is included.  
	 *		 If null all descandants are kept.
	 * @param sort
	 *		 Return -1,0,1 (Like comparable) to sort files by any criteria
	 *		 If null then no sorting is applied.  This is faster and uses less memory
	 * @return this fileset
	 */
	FileSet descendants(def source, Closure filter = null, Closure sort = null) {
		dir (source, true, filter, sort)
	}

	/**
	 * Search for all children of the source.  This is NOT a recursive search
	 *
	 * @param source the root of the file set. Must be a directory
	 * @param filter 
	 *		 each file is passed to filter.  
	 * 		 If filter returns true then the file is included.  
	 *		 If null all descandants are kept.
	 * @param sort
	 *		 Return -1,0,1 (Like comparable) to sort files by any criteria
	 *		 If null then no sorting is applied.  This is faster and uses less memory
	 * @return this fileset
	 */
	FileSet children(def source, Closure filter = null, Closure sort = null) {
		dir (source, false, filter, sort)
	}
	
	/** NOT API */
	private FileSet dir(def source, Boolean recurse = true, Closure filter = null, Closure sort = null) {
		if(filter == null) {
			filter = { f -> true }
		}
		if (recurse == null) {
			recurse = true
		}
		def processDir = { c ->
			if (recurse) {
				resolve(source).eachFileRecurse { file ->
					if (filter(file)) {
						c(file)
					}
				}
			} else {
				resolve(source).eachFile { file ->
					if (filter(file)) {
						c(file)
					}
				}		
			}
		}
		sources << [
			eachFile: { c ->
				if(sort != null) {
					def files = new TreeSet([compare: { o1, o2 ->
							return sort(o1,o2)
						}] as Comparator)
					
					processDir {files.add(it)}
					
					files.each { c(it) }
				} else {
					processDir (c)
				}
			}
		]
		return this
	}
	/**
	 * add a file to the file set
	 */
	FileSet file (def source) {
		sources << [
			eachFile: { c ->
				c(resolve(source))
			}
		]
		return this
	}



}