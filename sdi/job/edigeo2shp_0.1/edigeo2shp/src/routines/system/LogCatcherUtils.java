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

public class LogCatcherUtils {

    public class LogCatcherMessage {

        private String type;

        private String origin;

        private int priority;

        private String message;

        private int code;

        public LogCatcherMessage(String type, String origin, int priority, String message, int code) {
            this.type = type;
            this.origin = origin;
            this.priority = priority;
            this.message = message;
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    java.util.List<LogCatcherMessage> messages = java.util.Collections
            .synchronizedList(new java.util.ArrayList<LogCatcherMessage>());

    public void addMessage(String type, String origin, int priority, String message, int code) {
        LogCatcherMessage lcm = new LogCatcherMessage(type, origin, priority, message, code);
        messages.add(lcm);
    }

    public java.util.List<LogCatcherMessage> getMessages() {
        java.util.List<LogCatcherMessage> messagesToSend = new java.util.ArrayList<LogCatcherMessage>();
        synchronized(messages) {
        	  for (LogCatcherMessage lcm : messages) {
                  messagesToSend.add(lcm);
              }
        }
      
        messages.clear();
        return messagesToSend;
    }
}
