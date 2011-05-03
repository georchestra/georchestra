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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

public class ParserUtils {

    public static List parseTo_List(String s) {
        if (s != null) {
            List list = new ArrayList();
            list.add(s);
            return list;
        }
        return null;
    }

    public static Character parseTo_Character(String s) {
        if (s == null) {
            return null;
        }
        return s.charAt(0);
    }

    public static char parseTo_char(String s) {
        return parseTo_Character(s);
    }

    public static Byte parseTo_Byte(String s) {
        if (s == null) {
            return null;
        }
        return Byte.decode(s).byteValue();
    }

    public static byte parseTo_byte(String s) {
        return parseTo_Byte(s);
    }

    public static Double parseTo_Double(String s) {
        if (s == null) {
            return null;
        }
        return Double.parseDouble(s);
    }

    public static double parseTo_double(String s) {
        return parseTo_Double(s);
    }

    public static float parseTo_float(String s) {
        return Float.parseFloat(s);
    }

    public static Float parseTo_Float(String s) {
        if (s == null) {
            return null;
        }
        return parseTo_float(s);
    }

    public static int parseTo_int(String s) {
        return Integer.parseInt(s);
    }

    public static Integer parseTo_Integer(String s) {
        if (s == null) {
            return null;
        }
        return parseTo_int(s);
    }

    public static short parseTo_short(String s) {
        return Short.parseShort(s);
    }

    public static Short parseTo_Short(String s) {
        if (s == null) {
            return null;
        }
        return parseTo_short(s);
    }

    public static long parseTo_long(String s) {
        return Long.parseLong(s);
    }

    public static Long parseTo_Long(String s) {
        return parseTo_long(s);
    }

    public static Boolean parseTo_Boolean(String s) {
        if (s == null) {
            return null;
        }
        if (s.equals("1")) { //$NON-NLS-1$
            return Boolean.parseBoolean("true"); //$NON-NLS-1$
        }
        return Boolean.parseBoolean(s);
    }

    public static boolean parseTo_boolean(String s) {
        return parseTo_Boolean(s);
    }

    public static String parseTo_String(String s) {
        return s;
    }

    public static BigDecimal parseTo_BigDecimal(String s) {
        if (s == null) {
            return null;
        }
        return new BigDecimal(s);
    }

    public synchronized static java.util.Date parseTo_Date(String s, String pattern) {
        // check the parameter for supporting " ","2007-09-13"," 2007-09-13 "
        if (s != null) {
            s = s.trim();
        }
        if (s == null || s.length() == 0) {
            return null;
        }
        java.util.Date date = null;
        // try {
        // date = FastDateParser.getInstance(pattern).parse(s);
        // } catch (java.text.ParseException e) {
        // e.printStackTrace();
        // System.err.println("Current string to parse '" + s + "'");
        // }
        DateFormat format = FastDateParser.getInstance(pattern);
        ParsePosition pp = new ParsePosition(0);
        pp.setIndex(0);
        date = format.parse(s, pp);
        if (pp.getIndex() != s.length() || date == null) {
            throw new RuntimeException("Unparseable date: \"" + s + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return date;
    }

    public synchronized static java.util.Date parseTo_Date(String s, String pattern, boolean lenient) {
        // check the parameter for supporting " ","2007-09-13"," 2007-09-13 "
        if (s != null) {
            s = s.trim();
        }
        if (s == null || s.length() == 0) {
            return null;
        }
        java.util.Date date = null;
        // try {
        // date = FastDateParser.getInstance(pattern).parse(s);
        // } catch (java.text.ParseException e) {
        // e.printStackTrace();
        // System.err.println("Current string to parse '" + s + "'");
        // }
        DateFormat format = FastDateParser.getInstance(pattern, lenient);
        ParsePosition pp = new ParsePosition(0);
        pp.setIndex(0);
        date = format.parse(s, pp);
        if (pp.getIndex() != s.length() || date == null) {
            throw new RuntimeException("Unparseable date: \"" + s + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return date;
    }

    public static java.util.Date parseTo_Date(java.util.Date date, String pattern) {
        // java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat(pattern);
        // java.util.Date date = null;
        // try {
        // date = simpleDateFormat.parse(date);
        // } catch (java.text.ParseException e) {
        // e.printStackTrace();
        // System.err.println("Current string to parse '" + s + "'");
        // }
        return date;
    }

    /**
     * in order to transform the string "1.234.567,89" to number 1234567.89
     */
    public static String parseTo_Number(String s, Character thousandsSeparator, Character decimalSeparator) {
        if (s == null) {
            return null;
        }
        String result = s;
        if (thousandsSeparator != null) {
            result = StringUtils.deleteChar(s, thousandsSeparator);
        }
        if (decimalSeparator != null) {
            result = result.replace(decimalSeparator, '.');
        }
        return result;
    }
}
