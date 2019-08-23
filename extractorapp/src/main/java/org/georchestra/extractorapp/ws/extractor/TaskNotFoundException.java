package org.georchestra.extractorapp.ws.extractor;

/**
 * Task was not found in the task queue.
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public class TaskNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3442453408513381630L;

	public TaskNotFoundException(String msg) {
		super(msg);
	}

}
