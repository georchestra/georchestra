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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class RunTrace implements Runnable {

    private boolean openSocket = true;

    public void openSocket(boolean openSocket) {
        this.openSocket = openSocket;
    }

    private class TraceBean {

        private String componentId;

        private int nbLine;

        public TraceBean(String componentId) {
            this.componentId = componentId;
        }

        public String getComponentId() {
            return this.componentId;
        }

        public void setComponentId(String componentId) {
            this.componentId = componentId;
        }

        public int getNbLine() {
            return this.nbLine;
        }

        public void setNbLine(int nbLine) {
            this.nbLine = nbLine;
        }
    }

    private java.util.concurrent.ConcurrentHashMap<String, TraceBean> processTraces = new java.util.concurrent.ConcurrentHashMap<String, TraceBean>();

    private java.net.Socket s;

    private java.io.PrintWriter pred;

    private boolean jobIsFinished = false;

    private String str = ""; //$NON-NLS-1$

    private Thread t;

    public void startThreadTrace(String clientHost, int portTraces) throws java.io.IOException, java.net.UnknownHostException {
        if (!openSocket) {
            return;
        }
        System.out.println("[trace] connecting to socket on port " + portTraces); //$NON-NLS-1$
        s = new java.net.Socket(clientHost, portTraces);
        pred = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.OutputStreamWriter(s.getOutputStream())), true);
        System.out.println("[trace] connected"); //$NON-NLS-1$
        t = new Thread(this);
        t.start();

    }

    public void run() {
        synchronized (this) {
            try {
                while (!jobIsFinished) {
                    wait(100);
                }
            } catch (InterruptedException e) {
                System.out.println("[trace] interrupted"); //$NON-NLS-1$
            }
        }
    }

    public void stopThreadTrace() {
        if (!openSocket) {
            return;
        }
        jobIsFinished = true;
        try {
            pred.close();
            s.close();
            System.out.println("[trace] disconnected"); //$NON-NLS-1$
        } catch (java.io.IOException ie) {
        }
    }

    public boolean isPause() {
        if (!openSocket) {
            return false;
        }
        InputStream in;
        try {
            askForStatus();
            in = s.getInputStream();
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
            return "PAUSE".equals(reader.readLine()); //$NON-NLS-1$
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void askForStatus() {
        pred.println("ID_STATUS"); //$NON-NLS-1$
    }

    public void sendTrace(String componentId, String datas) {
        if (!openSocket) {
            return;
        }
        TraceBean bean;
        if (processTraces.containsKey(componentId)) {
            bean = processTraces.get(componentId);
        } else {
            bean = new TraceBean(componentId);
        }
        bean.setNbLine(bean.getNbLine() + 1);
        processTraces.put(componentId, bean);

        str = bean.getComponentId() + "|" + bean.getNbLine() + "|" + datas; //$NON-NLS-1$ //$NON-NLS-2$
        pred.println(str); // envoi d'un message
    }
}
