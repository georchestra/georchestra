// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//   
// ============================================================================
package routines.system;

import java.util.Date;

public class AssertCatcherUtils {

    public class AssertCatcherMessage {

        private Date moment;

        private String pid;

        private String project;

        private String job;

        private String language;

        private String origin;

        private String status;

        private String substatus;

        private String description;

        public AssertCatcherMessage(String pid, String project, String job, String language, String origin, String status,
                String substatus, String description) {
            this.moment = java.util.Calendar.getInstance().getTime();
            this.pid = pid;
            this.project = project;
            this.job = job;
            this.language = language;
            this.origin = origin;
            this.status = status;
            this.substatus = substatus;
            this.description = description;
        }

        /**
         * Getter for moment.
         * 
         * @return the moment
         */
        public Date getMoment() {
            return this.moment;
        }

        /**
         * Sets the moment.
         * 
         * @param moment the moment to set
         */
        public void setMoment(Date moment) {
            this.moment = moment;
        }

        /**
         * Getter for description.
         * 
         * @return the description
         */
        public String getDescription() {
            return this.description;
        }

        /**
         * Sets the description.
         * 
         * @param description the description to set
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * Getter for job.
         * 
         * @return the job
         */
        public String getJob() {
            return this.job;
        }

        /**
         * Sets the job.
         * 
         * @param job the job to set
         */
        public void setJob(String job) {
            this.job = job;
        }

        /**
         * Getter for language.
         * 
         * @return the language
         */
        public String getLanguage() {
            return this.language;
        }

        /**
         * Sets the language.
         * 
         * @param language the language to set
         */
        public void setLanguage(String language) {
            this.language = language;
        }

        /**
         * Getter for origin.
         * 
         * @return the origin
         */
        public String getOrigin() {
            return this.origin;
        }

        /**
         * Sets the origin.
         * 
         * @param origin the origin to set
         */
        public void setOrigin(String origin) {
            this.origin = origin;
        }

        /**
         * Getter for pid.
         * 
         * @return the pid
         */
        public String getPid() {
            return this.pid;
        }

        /**
         * Sets the pid.
         * 
         * @param pid the pid to set
         */
        public void setPid(String pid) {
            this.pid = pid;
        }

        /**
         * Getter for project.
         * 
         * @return the project
         */
        public String getProject() {
            return this.project;
        }

        /**
         * Sets the project.
         * 
         * @param project the project to set
         */
        public void setProject(String project) {
            this.project = project;
        }

        /**
         * Getter for status.
         * 
         * @return the status
         */
        public String getStatus() {
            return this.status;
        }

        /**
         * Sets the status.
         * 
         * @param status the status to set
         */
        public void setStatus(String status) {
            this.status = status;
        }

        /**
         * Getter for substatus.
         * 
         * @return the substatus
         */
        public String getSubstatus() {
            return this.substatus;
        }

        /**
         * Sets the substatus.
         * 
         * @param substatus the substatus to set
         */
        public void setSubstatus(String substatus) {
            this.substatus = substatus;
        }
    }

    java.util.List<AssertCatcherMessage> messages = java.util.Collections
            .synchronizedList(new java.util.ArrayList<AssertCatcherMessage>());

    public void addMessage(String pid, String project, String job, String language, String origin, String status,
            String substatus, String description) {
        AssertCatcherMessage lcm = new AssertCatcherMessage(pid, project, job, language, origin, status, substatus, description);
        messages.add(lcm);
    }

    public java.util.List<AssertCatcherMessage> getMessages() {
        java.util.List<AssertCatcherMessage> messagesToSend = new java.util.ArrayList<AssertCatcherMessage>();
	    synchronized(messages) {
	        for (AssertCatcherMessage acm : messages) {
	            messagesToSend.add(acm);
	        }
	    }
        messages.clear();
        return messagesToSend;
    }
}
