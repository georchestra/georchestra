/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.console.bs;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * This class is responsible of checking whether the user user does not use the generated token to change his password.
 *
 * <p>
 * The period to execute this task is configured.
 *
 * </p>
 *
 * @author Mauricio Pazos
 *
 */
public final class ExpiredTokenManagement {


	private static final Log LOG = LogFactory.getLog(ExpiredTokenManagement.class.getName());

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	/** delay in days to execute the cleaning task */
	private int delayInDays = -1;

	private ExpiredTokenCleanTask expiredTokenCleanTask;


	@Autowired
	public ExpiredTokenManagement(ExpiredTokenCleanTask expiredTokenCleanTask) {
		this.expiredTokenCleanTask = expiredTokenCleanTask;
	}

	public int getDelayInDays() {
		return delayInDays;
	}

	public void setDelayInDays(int delayInDays) {
		this.delayInDays = delayInDays;
	}

	public void start(){

		if(delayInDays == -1 ){
			throw new IllegalStateException("delayInDays property hasn't been set in the configuration bean.");
		}

		long delay = toMilliseconds(this.delayInDays);

		//final ExpiredTokenCleanTask expiredTokenCleanTask = new ExpiredTokenCleanTask();
		expiredTokenCleanTask.setDelayInMilliseconds(delay);

		this.scheduler.scheduleWithFixedDelay(expiredTokenCleanTask, 0, delay, TimeUnit.MILLISECONDS);

		if(LOG.isDebugEnabled()){
			LOG.debug("was scheduled - delay (days):" + delayInDays  );
		}
	}


	/**
	 * Converts the days to millisecconds
	 *
	 * @param delayInDays
	 * @return the delay days in milliseconds
	 */
	private static long toMilliseconds(int delayInDays) {

		return 24 * 3600000 * delayInDays;
	}



}
