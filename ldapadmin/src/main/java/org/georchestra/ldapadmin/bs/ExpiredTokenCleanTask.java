package org.georchestra.ldapadmin.bs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.UserTokenDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This task searches and removes the expired tokens generated when for the "lost password" use case.
 *
 * @author Mauricio Pazos
 *
 */
public class ExpiredTokenCleanTask implements Runnable {

	private static final Log LOG = LogFactory.getLog(ExpiredTokenCleanTask.class.getName());

	private UserTokenDao userTokenDao;

	private long delayInMilliseconds;

	@Autowired
	public ExpiredTokenCleanTask(UserTokenDao userTokenDao) {

		this.userTokenDao = userTokenDao;
	}

	public void setDelayInMilliseconds(long delayInMiliseconds) {

		this.delayInMilliseconds = delayInMiliseconds;
	}

	/**
	 * Removes the expired tokens
	 *
	 * This task is scheduled taking into account the delay period.
	 */
	@Override
	public void run() {

		Calendar calendar = Calendar.getInstance();

		long now = calendar.getTimeInMillis();
		Date expired = new Date(now - this.delayInMilliseconds);

		try {
			List<Map<String, Object>>  userTokenToDelete = userTokenDao.findBeforeDate(expired);
			for (Map<String, Object> userToken : userTokenToDelete) {
				try {
					userTokenDao.delete((String) userToken.get("uid"));
				} catch (Exception e) {
					LOG.error(e.getMessage());
				}
			}
		} catch (DataServiceException e1) {
			LOG.error(e1);
		}
	}
}
