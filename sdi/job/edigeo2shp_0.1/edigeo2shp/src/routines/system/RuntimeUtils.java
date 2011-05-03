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

import routines.TalendDate;

public class RuntimeUtils {

    public static boolean isDateType(Object o) {
        return getRuntimeType(o).equals("java.util.Date"); //$NON-NLS-1$
    }

    /**
     * This function is in order to check the i type for "int i = 10".
     * 
     * @param o
     * @return
     */
    public static String getRuntimeType(Object o) {
        return o.getClass().getName();
    }

    /**
     * This function is in order to check the Date type in tRunJob when transmit the context to child job.
     * 
     * @param o
     * @return
     */
    public static Object tRunJobConvertContext(Object o) {
        if (o == null) {
            return null;
        }

        // when tRunJob transmit the date to child job, it should format with "yyyy-MM-dd HH:mm:ss"
        if (isDateType(o)) {
            return TalendDate.formatDate("yyyy-MM-dd HH:mm:ss", (Date) o); //$NON-NLS-1$
        }

        return o;
    }

    public static void main(String[] args) {
        int i = 10;
        System.out.println(tRunJobConvertContext(i));

        Date date = new Date();
        System.out.println(tRunJobConvertContext(date));

    }
}
