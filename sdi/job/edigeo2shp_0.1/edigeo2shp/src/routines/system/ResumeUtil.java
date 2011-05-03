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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ResumeUtil.
 * 
 */
public class ResumeUtil {

    // final private static String fieldSeparator = ",";

    String logFileName = null;

    SimpleCsvWriter csvWriter = null;

    ResumeCommonInfo commonInfo = null;

    // it is a flag, all jobs(parentjob/childjob) with the same root_pid will
    // share only one FileWriter.
    String root_pid = null;

    // <pid, SimpleCsvWriter>.
    private static Map<String, SimpleCsvWriter> sharedWriterMap = new HashMap<String, SimpleCsvWriter>();

    // step1: init the log file name
    public ResumeUtil(String logFileName, boolean createNewFile, String root_pid) {
        if (logFileName == null || logFileName.equals("null")) {
            return;
        }

        // only assign value one time.
        if (this.root_pid == null) {
            this.root_pid = root_pid;
        }

        SimpleCsvWriter sharedWriter = sharedWriterMap.get(root_pid);

        this.logFileName = logFileName;
        File file = new File(logFileName);
        try {
            if (sharedWriter == null) {
                this.csvWriter = new SimpleCsvWriter(new FileWriter(logFileName, createNewFile));

                // shared
                sharedWriterMap.put(this.root_pid, this.csvWriter);

                // output the header part
                if (file.length() == 0) {
                    csvWriter.write("eventDate");// eventDate--------------->???
                    csvWriter.write("pid");// pid
                    csvWriter.write("root_pid");// root_pid
                    csvWriter.write("father_pid");// father_pid
                    csvWriter.write("type");// type---------------->???
                    csvWriter.write("partName");// partName
                    csvWriter.write("parentPart");// parentPart
                    csvWriter.write("project");// project
                    csvWriter.write("jobName");// jobName
                    csvWriter.write("jobContext");// jobContext
                    csvWriter.write("jobVersion");// jobVersion
                    csvWriter.write("threadId");// threadId
                    csvWriter.write("logPriority");// logPriority
                    csvWriter.write("errorCode");// errorCode
                    csvWriter.write("message");// message
                    csvWriter.write("stackTrace");// stackTrace
                    csvWriter.endRecord();
                    csvWriter.flush();
                }
            } else {
                csvWriter = sharedWriter;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // step2: init the common log message info
    public void initCommonInfo(String pid, String root_pid, String father_pid, String project, String jobName, String jobContext,
            String jobVersion) {
        this.commonInfo = new ResumeCommonInfo();
        this.commonInfo.pid = pid;// 2
        this.commonInfo.root_pid = root_pid;// 3
        this.commonInfo.father_pid = father_pid;// 4
        // public String parentPart = null;// 7
        this.commonInfo.project = project;// 8
        this.commonInfo.jobName = jobName;// 9
        this.commonInfo.jobContext = jobContext;// 10
        this.commonInfo.jobVersion = jobVersion;// 11
    }

    // step3: add log item one by one
    public void addLog(String type, String partName, String parentPart, String threadId, String logPriority, String errorCode,
            String message, String stackTrace) {

        if (csvWriter == null) {
            return;
        }

        String eventDate = FormatterUtils.format_Date(new Date(), "yyyy-MM-dd HH:mm:ss.S");

        JobLogItem item = new JobLogItem(eventDate, type, partName, parentPart, threadId, logPriority, errorCode, message,
                stackTrace);
        try {
            csvWriter.write(item.eventDate);// eventDate--------------->???
            csvWriter.write(commonInfo.pid);// pid
            csvWriter.write(commonInfo.root_pid);// root_pid
            csvWriter.write(commonInfo.father_pid);// father_pid
            csvWriter.write(item.type);// type---------------->???
            csvWriter.write(item.partName);// partName

            csvWriter.write(item.parentPart);// parentPart

            csvWriter.write(commonInfo.project);// project
            csvWriter.write(commonInfo.jobName);// jobName
            csvWriter.write(commonInfo.jobContext);// jobContext
            csvWriter.write(commonInfo.jobVersion);// jobVersion
            csvWriter.write(null);// threadId
            csvWriter.write(item.logPriority);// logPriority
            csvWriter.write(item.errorCode);// errorCode
            csvWriter.write(item.message);// message
            csvWriter.write(item.stackTrace);// stackTrace
            csvWriter.endRecord();
            csvWriter.flush();

            // for test the order
            // System.out.print(item.partName + ",");// partName
            // System.out.print(item.parentPart + ",");// parentPart
            // System.out.print(commonInfo.project + ",");// project
            // System.out.print(commonInfo.jobName + ",");// jobName
            // System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Util: invoke target check point
    @Deprecated
    public static void invokeTargetCheckPoint(String resuming_checkpoint_path, Object jobObject,
            final java.util.Map<String, Object> globalMap) throws Exception {
        /*
         * String resuming_checkpoint_path =
         * "/JOB:parentJob/SUBJOB:tRunJob_1/NODE:tRunJob_1/JOB:ChildJob/SUBJOB:tSystem_2" ;
         */
        String currentJob_checkpoint_path = null;

        // 1. get currentJob_checkpoint_path
        if (resuming_checkpoint_path != null) {
            int indexOf = resuming_checkpoint_path.indexOf("/NODE:");

            if (indexOf != -1) {
                // currentJob_checkpoint_path: /JOB:parentJob/SUBJOB:tRunJob_1
                currentJob_checkpoint_path = resuming_checkpoint_path.substring(0, indexOf);
            } else {
                // currentJob_checkpoint_path: /JOB:ChildJob/SUBJOB:tSystem_2
                currentJob_checkpoint_path = resuming_checkpoint_path;
            }
        }

        String currentJob_subJob_resuming = null;
        // 2. get the target SUBJOB
        if (currentJob_checkpoint_path != null) {
            int indexOf = currentJob_checkpoint_path.indexOf("/SUBJOB:");
            if (indexOf != -1) {
                currentJob_subJob_resuming = currentJob_checkpoint_path.substring(indexOf + 8);
            }
        }

        String subjobMethodName = currentJob_subJob_resuming + "Process";
        // System.out.println(subjobMethodName);

        // 3. invoke the target method
        if (currentJob_subJob_resuming != null) {
            for (java.lang.reflect.Method m : jobObject.getClass().getMethods()) {
                if (m.getName().compareTo(subjobMethodName) == 0) {
                    m.invoke(jobObject, new Object[] { globalMap });
                    break;
                }
            }
        }
    }

    // Util: get the method name of resume entry. it is a method name of one
    // subjob in current job.
    public static String getResumeEntryMethodName(String resuming_checkpoint_path) {
        /*
         * String resuming_checkpoint_path =
         * "/JOB:parentJob/SUBJOB:tRunJob_1/NODE:tRunJob_1/JOB:ChildJob/SUBJOB:tSystem_2" ;
         */
        String currentJob_checkpoint_path = null;

        // 1. get currentJob_checkpoint_path
        if (resuming_checkpoint_path != null) {
            int indexOf = resuming_checkpoint_path.indexOf("/NODE:");

            if (indexOf != -1) {
                // currentJob_checkpoint_path: /JOB:parentJob/SUBJOB:tRunJob_1
                currentJob_checkpoint_path = resuming_checkpoint_path.substring(0, indexOf);
            } else {
                // currentJob_checkpoint_path: /JOB:ChildJob/SUBJOB:tSystem_2
                currentJob_checkpoint_path = resuming_checkpoint_path;
            }
        }

        String currentJob_subJob_resuming = null;
        // 2. get the target SUBJOB
        if (currentJob_checkpoint_path != null) {
            int indexOf = currentJob_checkpoint_path.indexOf("/SUBJOB:");
            if (indexOf != -1) {
                currentJob_subJob_resuming = currentJob_checkpoint_path.substring(indexOf + 8);
            }
        }

        String subjobMethodName = null;
        if (currentJob_subJob_resuming != null) {
            subjobMethodName = currentJob_subJob_resuming + "Process";
        }

        // do check
        if (resuming_checkpoint_path != null) {
            if (subjobMethodName == null || !subjobMethodName.matches("[\\w]*_[\\d]*Process")) {
                throw new RuntimeException("Parse the \"resuming_checkpoint_path=" + resuming_checkpoint_path
                        + "\" failed. There can't get the a valid resume subjob name.");
            }
        }

        // System.out.println(subjobMethodName);
        return subjobMethodName;
    }

    // Util: get check poit path for child job-->used by tRunJob
    public static String getChildJobCheckPointPath(String resuming_checkpoint_path) {
        /*
         * String resuming_checkpoint_path =
         * "/JOB:parentJob/SUBJOB:tRunJob_1/NODE:tRunJob_1/JOB:ChildJob/SUBJOB:tSystem_2" ;
         */
        String childJob_checkpoint_path = null;

        // get currentJob_checkpoint_path
        if (resuming_checkpoint_path != null) {
            int indexOf = resuming_checkpoint_path.indexOf("/NODE:");

            if (indexOf != -1) {
                String temp = resuming_checkpoint_path.substring(indexOf);

                int index = temp.indexOf("/JOB:");

                childJob_checkpoint_path = temp.substring(index);
            }
        }

        // System.out.println(childJob_checkpoint_path);

        return childJob_checkpoint_path;
    }

    // Util: get right tRunJob name, only one tRunJob will transmit the "resuming_checkpoint_path" to child job-->used
    // by tRunJob
    public static String getRighttRunJob(String resuming_checkpoint_path) {
        /*
         * String resuming_checkpoint_path =
         * "/JOB:parentJob/SUBJOB:tRunJob_1/NODE:tRunJob_1/JOB:ChildJob/SUBJOB:tSystem_2" ;
         */
        String tRunJobName = null;

        // get currentJob_checkpoint_path
        if (resuming_checkpoint_path != null) {
            int indexOf = resuming_checkpoint_path.indexOf("/NODE:");

            if (indexOf != -1) {
                String temp = resuming_checkpoint_path.substring(indexOf);

                int index = temp.indexOf("/JOB:");

                if (index != -1) {
                    // /NODE:tRunJob_1 ---> tRunJob_1
                    tRunJobName = temp.substring(6, index);
                }
            }
        }

        // System.out.println(tRunJobName);

        return tRunJobName;
    }

    // Util: get String type of ExceptionStackTrace
    public static String getExceptionStackTrace(Exception exception) {
        java.io.OutputStream out = new java.io.ByteArrayOutputStream();

        java.io.PrintStream ps = new java.io.PrintStream(out, true);
        exception.printStackTrace(ps);
        String str = out.toString();
        return str;
    }

    // 7 fields
    public class ResumeCommonInfo {

        // there are 7 fields as common info in resume log message
        public String pid = null;// 2

        public String root_pid = null;// 3

        public String father_pid = null;// 4

        // public String parentPart = null;// 7
        public String project = null;// 8

        public String jobName = null;// 9

        public String jobContext = null;// 10

        public String jobVersion = null;// 11
    }

    // 10 fields
    public class JobLogItem {

        public JobLogItem(String eventDate, String type, String partName, String parentPart, String threadId, String logPriority,
                String errorCode, String message, String stackTrace) {
            this.eventDate = eventDate;
            this.type = type;
            this.partName = partName;
            this.parentPart = parentPart;
            this.threadId = threadId;
            this.logPriority = logPriority;
            this.errorCode = errorCode;
            this.message = message;
            this.stackTrace = stackTrace;
        }

        // there are 10 fields for every different message
        public String eventDate = null;// 1

        public String type = null;// 5

        public String partName = null;// 6

        public String parentPart = null;// 7

        public String threadId = null;// 12

        public String logPriority = null;// 13

        public String errorCode = null;// 14

        public String message = null;// 15

        public String stackTrace = null;// 16
    }

    public enum LogPriority {
        NONE,
        WARN,
        ERROR,
        FATAL;
    }

    public enum ResumeEventType {
        JOB_STARTED,
        CHECKPOINT,
        SYSTEM_LOG,
        USER_DEF_LOG,
        JOB_ENDED;
    }

    /**
     * this class is reference with CsvWriter.
     * 
     * Because java.io.PrintWriter with this limit. {@link PrintWriter}, If automatic flushing is enabled it will be
     * done only when...
     * 
     * This limit will affect parentJob/childrenJob operate the same csv writer file, they always hold data buffer
     * themselves, and flush() can't really flush.
     * 
     * SimpleCsvWriter is without this problem.
     * 
     * @author wyang
     */
    public class SimpleCsvWriter {

        private Writer writer = null;

        private boolean firstColumn = true;

        private static final int ESCAPE_MODE_BACKSLASH = 2;

        private static final int EscapeMode = ESCAPE_MODE_BACKSLASH;

        private static final char TextQualifier = '"';

        private static final char BACKSLASH = '\\';

        private static final char Delimiter = ',';

        // JDK1.5 can't pass compile
        // private String lineSeparator = (String)
        // java.security.AccessController
        // .doPrivileged(new
        // sun.security.action.GetPropertyAction("line.separator"));

        private String lineSeparator = System.getProperty("line.separator");

        public SimpleCsvWriter(Writer writer) {
            this.writer = writer;
        }

        /**
         * writer a column
         */
        public void write(String content) throws IOException {

            if (content == null) {
                content = "";
            }

            if (!firstColumn) {
                writer.write(Delimiter);
            }

            writer.write(TextQualifier);

            // only support backslash mode
            if (EscapeMode == ESCAPE_MODE_BACKSLASH) {
                content = replace(content, "" + BACKSLASH, "" + BACKSLASH + BACKSLASH);
                content = replace(content, "" + TextQualifier, "" + BACKSLASH + TextQualifier);
            }

            writer.write(content);

            writer.write(TextQualifier);

            firstColumn = false;
        }

        /**
         * finish a record, prepare the next one
         */
        public void endRecord() throws IOException {
            writer.write(lineSeparator);
            firstColumn = true;
        }

        /**
         * flush
         */
        public void flush() {
            try {
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * close
         */
        public void close() {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String replace(String original, String pattern, String replace) {
            final int len = pattern.length();
            int found = original.indexOf(pattern);

            if (found > -1) {
                StringBuffer sb = new StringBuffer();
                int start = 0;

                while (found != -1) {
                    sb.append(original.substring(start, found));
                    sb.append(replace);
                    start = found + len;
                    found = original.indexOf(pattern, start);
                }

                sb.append(original.substring(start));

                return sb.toString();
            } else {
                return original;
            }
        }
    }
}
