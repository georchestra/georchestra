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

public class TalendThreadResult {

    private Integer errorCode = null;

    private String status = ""; //$NON-NLS-1$

    public Integer getErrorCode() {
        return errorCode;
    }

    // only keep the max error code
    public void setErrorCode(Integer errorCode) {
        if (errorCode != null) {
            if (this.errorCode == null || errorCode.compareTo(this.errorCode) > 0) {
                this.errorCode = errorCode;
            }
        }
    }

    public String getStatus() {
        return status;
    }

    // status will be "" , "failure" or "end"
    public void setStatus(String status) {
        this.status = status;
    }

}
