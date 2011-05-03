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

public class TalendThread extends Thread {

    public TalendThreadPool talendThreadPool = null;

    public Integer errorCode = null;

    public String status = ""; //$NON-NLS-1$

    // this is a template for Iterate Parallel
    public void run() {
        try {

        } catch (Exception e) {
            talendThreadPool.setErrorThread(this);
            talendThreadPool.stopAllWorkers();
            e.printStackTrace();
        }
    }
}
