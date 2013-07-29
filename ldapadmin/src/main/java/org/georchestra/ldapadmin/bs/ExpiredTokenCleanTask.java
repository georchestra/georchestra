package org.georchestra.ldapadmin.bs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.UserTokenDao;

/**
 * This task searches and removes the expired tokens generated when for the "lost password" use case.
 * 
 * @author Mauricio Pazos
 *
 */
class ExpiredTokenCleanTask implements Runnable {

	private static final Log LOG = LogFactory.getLog(ExpiredTokenCleanTask.class.getName());

	
	private long delayInMillisecconds;

	public void setDelayInMillisecconds(long delayInMilisecconds) {
		this.delayInMillisecconds = delayInMilisecconds;
	}

	/**
	 * Removes the expired tokens
	 *  
	 * This task is scheduled taking into account the delay period. 
	 */
	@Override
	public void run(){

		UserTokenDao userTokenDao = new UserTokenDao();
		
		Calendar calendar = Calendar.getInstance();
		
		long now = calendar.getTimeInMillis();
		Date expired = new Date(now - this.delayInMillisecconds);

		try {
			List<Map<String, Object>>  userTokenToDelete = userTokenDao.findBeforeDate(expired);
			for (Map<String, Object> userToken : userTokenToDelete) {
				
				try {
					userTokenDao.delete( (String) userToken.get("uid") );
					
				} catch (Exception e) {
					LOG.error(e.getMessage());
				} 
			}
			
		} catch (DataServiceException e1) {
			LOG.error(e1);
		}
		
	}
	

}
