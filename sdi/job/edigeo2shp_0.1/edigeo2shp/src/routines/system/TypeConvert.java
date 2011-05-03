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

/**
 * DOC liyilin class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 * 
 */
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import routines.TalendDate;

public class TypeConvert {

    public static class ConvertTypeNotSupportException extends RuntimeException {

        public ConvertTypeNotSupportException() {
            super();
        }

        public ConvertTypeNotSupportException(String s) {
            super(s);
        }

        public ConvertTypeNotSupportException(String s, Object o) {
            super(s);
            System.out.println(o);
        }

    }

    public static class ConvertTypeIllegalArgumentException extends IllegalArgumentException {

        public ConvertTypeIllegalArgumentException() {
            super();
        }

        public ConvertTypeIllegalArgumentException(String s) {
            super(s);
        }

        public ConvertTypeIllegalArgumentException(String s, Throwable cause) {
            super(s, cause);
        }

        static ConvertTypeIllegalArgumentException forInputArgument(Object argument) {
            return new ConvertTypeIllegalArgumentException("For input argument: \"" + argument + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        static ConvertTypeIllegalArgumentException forInputArgument(Object argument, Throwable cause) {
            return new ConvertTypeIllegalArgumentException("For input argument: \"" + argument + "\"", cause); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * No.1 boolean.1 to boolean
     * 
     */
    public static boolean boolean2boolean(boolean o) {
        return o;
    }

    /**
     * No.2 boolean.2 to Boolean
     * 
     */
    public static Boolean boolean2Boolean(boolean o) {
        return Boolean.valueOf(o);
    }

    /**
     * No.3 boolean.3 to byte
     * 
     */
    public static byte boolean2byte(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to byte  "); //$NON-NLS-1$
    }

    /**
     * No.4 boolean.4 to Byte
     * 
     */
    public static Byte boolean2Byte(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to Byte  "); //$NON-NLS-1$
    }

    /**
     * No.5 boolean.5 to byte[]
     * 
     */
    public static byte[] boolean2byteArray(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to byte[]  "); //$NON-NLS-1$
    }

    /**
     * No.6 boolean.6 to char
     * 
     */
    public static char boolean2char(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to char  "); //$NON-NLS-1$
    }

    /**
     * No.7 boolean.7 to Character
     * 
     */
    public static Character boolean2Character(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to Character  "); //$NON-NLS-1$
    }

    /**
     * No.8 boolean.8 to Date
     * 
     */
    public static Date boolean2Date(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to Date  "); //$NON-NLS-1$
    }

    /**
     * No.9 boolean.9 to double
     * 
     */
    public static double boolean2double(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to double  "); //$NON-NLS-1$
    }

    /**
     * No.10 boolean.10 to Double
     * 
     */
    public static Double boolean2Double(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to Double  "); //$NON-NLS-1$
    }

    /**
     * No.11 boolean.11 to float
     * 
     */
    public static float boolean2float(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to float  "); //$NON-NLS-1$
    }

    /**
     * No.12 boolean.12 to Float
     * 
     */
    public static Float boolean2Float(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to Float  "); //$NON-NLS-1$
    }

    /**
     * No.13 boolean.13 to BigDecimal
     * 
     */
    public static BigDecimal boolean2BigDecimal(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to BigDecimal  "); //$NON-NLS-1$
    }

    /**
     * No.14 boolean.14 to int
     * 
     */
    public static int boolean2int(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to int  "); //$NON-NLS-1$
    }

    /**
     * No.15 boolean.15 to Integer
     * 
     */
    public static Integer boolean2Integer(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to Integer  "); //$NON-NLS-1$
    }

    /**
     * No.16 boolean.16 to long
     * 
     */
    public static long boolean2long(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to long  "); //$NON-NLS-1$
    }

    /**
     * No.17 boolean.17 to Long
     * 
     */
    public static Long boolean2Long(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to Long  "); //$NON-NLS-1$
    }

    /**
     * No.18 boolean.18 to Object
     * 
     */
    public static Object boolean2Object(boolean o) {
        return Boolean.valueOf(o);
    }

    /**
     * No.19 boolean.19 to short
     * 
     */
    public static short boolean2short(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to short  "); //$NON-NLS-1$
    }

    /**
     * No.20 boolean.20 to Short
     * 
     */
    public static Short boolean2Short(boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert boolean to Short  "); //$NON-NLS-1$
    }

    /**
     * No.21 boolean.21 to String
     * 
     */
    public static String boolean2String(boolean o) {
        return Boolean.toString(o);
    }

    /**
     * No.22 boolean.22 to List
     * 
     */
    public static List boolean2List(boolean o) {
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.23 Boolean.1 to boolean
     * 
     */
    public static boolean Boolean2boolean(Boolean o) {
        if (o == null)
            return false;
        return o.booleanValue();
    }

    /**
     * No.24 Boolean.2 to Boolean
     * 
     */
    public static Boolean Boolean2Boolean(Boolean o) {

        return o;
    }

    /**
     * No.25 Boolean.3 to byte
     * 
     */
    public static byte Boolean2byte(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to byte  "); //$NON-NLS-1$
    }

    /**
     * No.26 Boolean.4 to Byte
     * 
     */
    public static Byte Boolean2Byte(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to Byte  "); //$NON-NLS-1$
    }

    /**
     * No.27 Boolean.5 to byte[]
     * 
     */
    public static byte[] Boolean2byteArray(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to byte[]  "); //$NON-NLS-1$
    }

    /**
     * No.28 Boolean.6 to char
     * 
     */
    public static char Boolean2char(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to char  "); //$NON-NLS-1$
    }

    /**
     * No.29 Boolean.7 to Character
     * 
     */
    public static Character Boolean2Character(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to Character  "); //$NON-NLS-1$
    }

    /**
     * No.30 Boolean.8 to Date
     * 
     */
    public static Date Boolean2Date(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to Date  "); //$NON-NLS-1$
    }

    /**
     * No.31 Boolean.9 to double
     * 
     */
    public static double Boolean2double(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to double  "); //$NON-NLS-1$
    }

    /**
     * No.32 Boolean.10 to Double
     * 
     */
    public static Double Boolean2Double(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to Double  "); //$NON-NLS-1$
    }

    /**
     * No.33 Boolean.11 to float
     * 
     */
    public static float Boolean2float(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to float  "); //$NON-NLS-1$
    }

    /**
     * No.34 Boolean.12 to Float
     * 
     */
    public static Float Boolean2Float(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to Float  "); //$NON-NLS-1$
    }

    /**
     * No.35 Boolean.13 to BigDecimal
     * 
     */
    public static BigDecimal Boolean2BigDecimal(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to BigDecimal  "); //$NON-NLS-1$
    }

    /**
     * No.36 Boolean.14 to int
     * 
     */
    public static int Boolean2int(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to int  "); //$NON-NLS-1$
    }

    /**
     * No.37 Boolean.15 to Integer
     * 
     */
    public static Integer Boolean2Integer(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to Integer  "); //$NON-NLS-1$
    }

    /**
     * No.38 Boolean.16 to long
     * 
     */
    public static long Boolean2long(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to long  "); //$NON-NLS-1$
    }

    /**
     * No.39 Boolean.17 to Long
     * 
     */
    public static Long Boolean2Long(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to Long  "); //$NON-NLS-1$
    }

    /**
     * No.40 Boolean.18 to Object
     * 
     */
    public static Object Boolean2Object(Boolean o) {
        return o;
    }

    /**
     * No.41 Boolean.19 to short
     * 
     */
    public static short Boolean2short(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to short  "); //$NON-NLS-1$
    }

    /**
     * No.42 Boolean.20 to Short
     * 
     */
    public static Short Boolean2Short(Boolean o) {
        throw new ConvertTypeNotSupportException("Can't support convert Boolean to Short  "); //$NON-NLS-1$
    }

    /**
     * No.43 Boolean.21 to String
     * 
     */
    public static String Boolean2String(Boolean o) {
        if (o == null)
            return null;
        return o.toString();
    }

    /**
     * No.44 Boolean.22 to List
     * 
     */
    public static List Boolean2List(Boolean o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.45 byte.1 to boolean
     * 
     */
    public static boolean byte2boolean(byte o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.46 byte.2 to Boolean
     * 
     */
    public static Boolean byte2Boolean(byte o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.47 byte.3 to byte
     * 
     */
    public static byte byte2byte(byte o) {
        return o;
    }

    /**
     * No.48 byte.4 to Byte
     * 
     */
    public static Byte byte2Byte(byte o) {
        return Byte.valueOf(o);
    }

    /**
     * No.49 byte.5 to byte[]
     * 
     */
    public static byte[] byte2byteArray(byte o) {
        return new byte[] { o };
    }

    /**
     * No.50 byte.6 to char
     * 
     */
    public static char byte2char(byte o) {
        return (char) o;
    }

    /**
     * No.51 byte.7 to Character
     * 
     */
    public static Character byte2Character(byte o) {
        return Character.valueOf((char) o);
    }

    /**
     * No.52 byte.8 to Date
     * 
     */
    public static Date byte2Date(byte o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte to Date  "); //$NON-NLS-1$
    }

    /**
     * No.53 byte.9 to double
     * 
     */
    public static double byte2double(byte o) {
        return (double) o;
    }

    /**
     * No.54 byte.10 to Double
     * 
     */
    public static Double byte2Double(byte o) {
        return Double.valueOf(o);
    }

    /**
     * No.55 byte.11 to float
     * 
     */
    public static float byte2float(byte o) {
        return (float) o;
    }

    /**
     * No.56 byte.12 to Float
     * 
     */
    public static Float byte2Float(byte o) {
        return Float.valueOf(o);
    }

    /**
     * No.57 byte.13 to BigDecimal
     * 
     */
    public static BigDecimal byte2BigDecimal(byte o) {
        return new BigDecimal(o);
    }

    /**
     * No.58 byte.14 to int
     * 
     */
    public static int byte2int(byte o) {
        return (int) o;
    }

    /**
     * No.59 byte.15 to Integer
     * 
     */
    public static Integer byte2Integer(byte o) {
        return Integer.valueOf(o);
    }

    /**
     * No.60 byte.16 to long
     * 
     */
    public static long byte2long(byte o) {
        return (long) o;
    }

    /**
     * No.61 byte.17 to Long
     * 
     */
    public static Long byte2Long(byte o) {
        return Long.valueOf(o);
    }

    /**
     * No.62 byte.18 to Object
     * 
     */
    public static Object byte2Object(byte o) {
        return Byte.valueOf(o);
    }

    /**
     * No.63 byte.19 to short
     * 
     */
    public static short byte2short(byte o) {
        return (short) o;
    }

    /**
     * No.64 byte.20 to Short
     * 
     */
    public static Short byte2Short(byte o) {
        return Short.valueOf(o);
    }

    /**
     * No.65 byte.21 to String
     * 
     */
    public static String byte2String(byte o) {
        return String.valueOf(o);
    }

    /**
     * No.66 byte.22 to List
     * 
     */
    public static List byte2List(byte o) {
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.67 Byte.1 to boolean
     * 
     */
    public static boolean Byte2boolean(Byte o) {
        throw new ConvertTypeNotSupportException("Can't support convert Byte to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.68 Byte.2 to Boolean
     * 
     */
    public static Boolean Byte2Boolean(Byte o) {
        throw new ConvertTypeNotSupportException("Can't support convert Byte to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.69 Byte.3 to byte
     * 
     */
    public static byte Byte2byte(Byte o) {
        if (o == null)
            return (byte) 0;
        return Byte.valueOf(o);
    }

    /**
     * No.70 Byte.4 to Byte
     * 
     */
    public static Byte Byte2Byte(Byte o) {
        if (o == null)
            return null;
        return o;
    }

    /**
     * No.71 Byte.5 to byte[]
     * 
     */
    public static byte[] Byte2byteArray(Byte o) {
        if (o == null)
            return null;
        return new byte[] { o.byteValue() };
    }

    /**
     * No.72 Byte.6 to char
     * 
     */
    public static char Byte2char(Byte o) {
        if (o == null)
            return (char) 0;
        return (char) o.byteValue();
    }

    /**
     * No.73 Byte.7 to Character
     * 
     */
    public static Character Byte2Character(Byte o) {
        if (o == null)
            return null;
        return Character.valueOf((char) o.byteValue());
    }

    /**
     * No.74 Byte.8 to Date
     * 
     */
    public static Date Byte2Date(Byte o) {
        throw new ConvertTypeNotSupportException("Can't support convert Byte to Date  "); //$NON-NLS-1$
    }

    /**
     * No.75 Byte.9 to double
     * 
     */
    public static double Byte2double(Byte o) {
        if (o == null)
            return 0d;
        return (double) o.doubleValue();
    }

    /**
     * No.76 Byte.10 to Double
     * 
     */
    public static Double Byte2Double(Byte o) {
        if (o == null)
            return null;
        return Double.valueOf(o);
    }

    /**
     * No.77 Byte.11 to float
     * 
     */
    public static float Byte2float(Byte o) {
        if (o == null)
            return 0f;
        return o.floatValue();
    }

    /**
     * No.78 Byte.12 to Float
     * 
     */
    public static Float Byte2Float(Byte o) {
        if (o == null)
            return null;
        return Float.valueOf(o.floatValue());
    }

    /**
     * No.79 Byte.13 to BigDecimal
     * 
     */
    public static BigDecimal Byte2BigDecimal(Byte o) {
        if (o == null)
            return null;
        return new BigDecimal(o);
    }

    /**
     * No.80 Byte.14 to int
     * 
     */
    public static int Byte2int(Byte o) {
        if (o == null)
            return 0;
        return o.intValue();
    }

    /**
     * No.81 Byte.15 to Integer
     * 
     */
    public static Integer Byte2Integer(Byte o) {
        if (o == null)
            return null;
        return Integer.valueOf(o);
    }

    /**
     * No.82 Byte.16 to long
     * 
     */
    public static long Byte2long(Byte o) {
        if (o == null)
            return 0L;
        return o.longValue();
    }

    /**
     * No.83 Byte.17 to Long
     * 
     */
    public static Long Byte2Long(Byte o) {
        if (o == null)
            return null;
        return Long.valueOf(o);
    }

    /**
     * No.84 Byte.18 to Object
     * 
     */
    public static Object Byte2Object(Byte o) {
        return o;
    }

    /**
     * No.85 Byte.19 to short
     * 
     */
    public static short Byte2short(Byte o) {
        if (o == null)
            return 0;
        return o.shortValue();
    }

    /**
     * No.86 Byte.20 to Short
     * 
     */
    public static Short Byte2Short(Byte o) {
        if (o == null)
            return null;
        return Short.valueOf(o);
    }

    /**
     * No.87 Byte.21 to String
     * 
     */
    public static String Byte2String(Byte o) {
        if (o == null)
            return null;
        return o.toString();
    }

    /**
     * No.88 Byte.22 to List
     * 
     */
    public static List Byte2List(Byte o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.89 byte[].1 to boolean
     * 
     */
    public static boolean byteArray2boolean(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.90 byte[].2 to Boolean
     * 
     */
    public static Boolean byteArray2Boolean(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.91 byte[].3 to byte
     * 
     */
    public static byte byteArray2byte(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to byte  "); //$NON-NLS-1$
    }

    /**
     * No.92 byte[].4 to Byte
     * 
     */
    public static Byte byteArray2Byte(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to Byte  "); //$NON-NLS-1$
    }

    /**
     * No.93 byte[].5 to byte[]
     * 
     */
    public static byte[] byteArray2byteArray(byte[] o) {
        return o;
    }

    /**
     * No.94 byte[].6 to char
     * 
     */
    public static char byteArray2char(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to char  "); //$NON-NLS-1$
    }

    /**
     * No.95 byte[].7 to Character
     * 
     */
    public static Character byteArray2Character(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to Character  "); //$NON-NLS-1$
    }

    /**
     * No.96 byte[].8 to Date
     * 
     */
    public static Date byteArray2Date(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to Date  "); //$NON-NLS-1$
    }

    /**
     * No.97 byte[].9 to double
     * 
     */
    public static double byteArray2double(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to double  "); //$NON-NLS-1$
    }

    /**
     * No.98 byte[].10 to Double
     * 
     */
    public static Double byteArray2Double(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to Double  "); //$NON-NLS-1$
    }

    /**
     * No.99 byte[].11 to float
     * 
     */
    public static float byteArray2float(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to float  "); //$NON-NLS-1$
    }

    /**
     * No.100 byte[].12 to Float
     * 
     */
    public static Float byteArray2Float(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to Float  "); //$NON-NLS-1$
    }

    /**
     * No.101 byte[].13 to BigDecimal
     * 
     */
    public static BigDecimal byteArray2BigDecimal(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to BigDecimal  "); //$NON-NLS-1$
    }

    /**
     * No.102 byte[].14 to int
     * 
     */
    public static int byteArray2int(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to int  "); //$NON-NLS-1$
    }

    /**
     * No.103 byte[].15 to Integer
     * 
     */
    public static Integer byteArray2Integer(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to Integer  "); //$NON-NLS-1$
    }

    /**
     * No.104 byte[].16 to long
     * 
     */
    public static long byteArray2long(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to long  "); //$NON-NLS-1$
    }

    /**
     * No.105 byte[].17 to Long
     * 
     */
    public static Long byteArray2Long(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to Long  "); //$NON-NLS-1$
    }

    /**
     * No.106 byte[].18 to Object
     * 
     */
    public static Object byteArray2Object(byte[] o) {
        return o;
    }

    /**
     * No.107 byte[].19 to short
     * 
     */
    public static short byteArray2short(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to short  "); //$NON-NLS-1$
    }

    /**
     * No.108 byte[].20 to Short
     * 
     */
    public static Short byteArray2Short(byte[] o) {
        throw new ConvertTypeNotSupportException("Can't support convert byte[] to Short  "); //$NON-NLS-1$
    }

    /**
     * No.109 byte[].21 to String
     * 
     */
    public static String byteArray2String(byte[] o) {
        if (o == null)
            return null;
        return o.toString();
    }

    /**
     * No.110 byte[].22 to List
     * 
     */
    public static List byteArray2List(byte[] o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        for (int i = 0; i < o.length; i++) {
            list.add(o[i]);
        }
        return list;
    }

    /**
     * No.111 char.1 to boolean
     * 
     */
    public static boolean char2boolean(char o) {
        throw new ConvertTypeNotSupportException("Can't support convert char to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.112 char.2 to Boolean
     * 
     */
    public static Boolean char2Boolean(char o) {
        throw new ConvertTypeNotSupportException("Can't support convert char to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.113 char.3 to byte
     * 
     */
    public static byte char2byte(char o) {
        return (byte) o;
    }

    /**
     * No.114 char.4 to Byte
     * 
     */
    public static Byte char2Byte(char o) {
        return Byte.valueOf((byte) o);
    }

    /**
     * No.115 char.5 to byte[]
     * 
     */
    public static byte[] char2byteArray(char o) {
        return new byte[] { (byte) o };
    }

    /**
     * No.116 char.6 to char
     * 
     */
    public static char char2char(char o) {
        return o;
    }

    /**
     * No.117 char.7 to Character
     * 
     */
    public static Character char2Character(char o) {
        return Character.valueOf(o);
    }

    /**
     * No.118 char.8 to Date
     * 
     */
    public static Date char2Date(char o) {
        throw new ConvertTypeNotSupportException("Can't support convert char to Date  "); //$NON-NLS-1$
    }

    /**
     * No.119 char.9 to double
     * 
     */
    public static double char2double(char o) {
        return (double) o;
    }

    /**
     * No.120 char.10 to Double
     * 
     */
    public static Double char2Double(char o) {
        return Double.valueOf(o);
    }

    /**
     * No.121 char.11 to float
     * 
     */
    public static float char2float(char o) {
        return (float) o;
    }

    /**
     * No.122 char.12 to Float
     * 
     */
    public static Float char2Float(char o) {
        return Float.valueOf(o);
    }

    /**
     * No.123 char.13 to BigDecimal
     * 
     */
    public static BigDecimal char2BigDecimal(char o) {
        return new BigDecimal(o);
    }

    /**
     * No.124 char.14 to int
     * 
     */
    public static int char2int(char o) {
        return (int) o;
    }

    /**
     * No.125 char.15 to Integer
     * 
     */
    public static Integer char2Integer(char o) {
        return Integer.valueOf(o);
    }

    /**
     * No.126 char.16 to long
     * 
     */
    public static long char2long(char o) {
        return (long) o;
    }

    /**
     * No.127 char.17 to Long
     * 
     */
    public static Long char2Long(char o) {
        return Long.valueOf(o);
    }

    /**
     * No.128 char.18 to Object
     * 
     */
    public static Object char2Object(char o) {
        return Character.valueOf(o);
    }

    /**
     * No.129 char.19 to short
     * 
     */
    public static short char2short(char o) {
        return (short) o;
    }

    /**
     * No.130 char.20 to Short
     * 
     */
    public static Short char2Short(char o) {
        return Short.valueOf((short) o);
    }

    /**
     * No.131 char.21 to String
     * 
     */
    public static String char2String(char o) {
        return String.valueOf(o);
    }

    /**
     * No.132 char.22 to List
     * 
     */
    public static List char2List(char o) {
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.133 Character.1 to boolean
     * 
     */
    public static boolean Character2boolean(Character o) {
        throw new ConvertTypeNotSupportException("Can't support convert Character to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.134 Character.2 to Boolean
     * 
     */
    public static Boolean Character2Boolean(Character o) {
        throw new ConvertTypeNotSupportException("Can't support convert Character to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.135 Character.3 to byte
     * 
     */
    public static byte Character2byte(Character o) {
        if (o == null)
            return (byte) 0;
        return (byte) o.charValue();
    }

    /**
     * No.136 Character.4 to Byte
     * 
     */
    public static Byte Character2Byte(Character o) {
        if (o == null)
            return null;
        return Byte.valueOf((byte) o.charValue());
    }

    /**
     * No.137 Character.5 to byte[]
     * 
     */
    public static byte[] Character2byteArray(Character o) {
        if (o == null)
            return null;
        return new byte[] { (byte) o.charValue() };
    }

    /**
     * No.138 Character.6 to char
     * 
     */
    public static char Character2char(Character o) {
        if (o == null)
            return (char) 0;
        return o.charValue();
    }

    /**
     * No.139 Character.7 to Character
     * 
     */
    public static Character Character2Character(Character o) {
        if (o == null)
            return null;
        return o;
    }

    /**
     * No.140 Character.8 to Date
     * 
     */
    public static Date Character2Date(Character o) {
        throw new ConvertTypeNotSupportException("Can't support convert Character to Date  "); //$NON-NLS-1$
    }

    /**
     * No.141 Character.9 to double
     * 
     */
    public static double Character2double(Character o) {
        if (o == null)
            return (double) 0;
        return (double) o.charValue();
    }

    /**
     * No.142 Character.10 to Double
     * 
     */
    public static Double Character2Double(Character o) {
        if (o == null)
            return null;
        return Double.valueOf(o);
    }

    /**
     * No.143 Character.11 to float
     * 
     */
    public static float Character2float(Character o) {
        if (o == null)
            return 0f;
        return (float) o.charValue();
    }

    /**
     * No.144 Character.12 to Float
     * 
     */
    public static Float Character2Float(Character o) {
        if (o == null)
            return null;
        return Float.valueOf(o);
    }

    /**
     * No.145 Character.13 to BigDecimal
     * 
     */
    public static BigDecimal Character2BigDecimal(Character o) {
        if (o == null)
            return null;
        return new BigDecimal((char) o.charValue());
    }

    /**
     * No.146 Character.14 to int
     * 
     */
    public static int Character2int(Character o) {
        if (o == null)
            return 0;
        return (int) o.charValue();
    }

    /**
     * No.147 Character.15 to Integer
     * 
     */
    public static Integer Character2Integer(Character o) {
        if (o == null)
            return null;
        return Integer.valueOf(o);
    }

    /**
     * No.148 Character.16 to long
     * 
     */
    public static long Character2long(Character o) {
        if (o == null)
            return 0L;
        return (long) o.charValue();
    }

    /**
     * No.149 Character.17 to Long
     * 
     */
    public static Long Character2Long(Character o) {
        if (o == null)
            return null;
        return Long.valueOf(o.charValue());
    }

    /**
     * No.150 Character.18 to Object
     * 
     */
    public static Object Character2Object(Character o) {
        return o;
    }

    /**
     * No.151 Character.19 to short
     * 
     */
    public static short Character2short(Character o) {
        if (o == null)
            return 0;
        return (short) o.charValue();
    }

    /**
     * No.152 Character.20 to Short
     * 
     */
    public static Short Character2Short(Character o) {
        if (o == null)
            return null;
        return Short.valueOf((short) o.charValue());
    }

    /**
     * No.153 Character.21 to String
     * 
     */
    public static String Character2String(Character o) {
        if (o == null)
            return null;
        return o.toString();
    }

    /**
     * No.154 Character.22 to List
     * 
     */
    public static List Character2List(Character o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.155 Date.1 to boolean
     * 
     */
    public static boolean Date2boolean(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.156 Date.2 to Boolean
     * 
     */
    public static Boolean Date2Boolean(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.157 Date.3 to byte
     * 
     */
    public static byte Date2byte(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to byte  "); //$NON-NLS-1$
    }

    /**
     * No.158 Date.4 to Byte
     * 
     */
    public static Byte Date2Byte(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to Byte  "); //$NON-NLS-1$
    }

    /**
     * No.159 Date.5 to byte[]
     * 
     */
    public static byte[] Date2byteArray(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to byte[]  "); //$NON-NLS-1$
    }

    /**
     * No.160 Date.6 to char
     * 
     */
    public static char Date2char(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to char  "); //$NON-NLS-1$
    }

    /**
     * No.161 Date.7 to Character
     * 
     */
    public static Character Date2Character(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to Character  "); //$NON-NLS-1$
    }

    /**
     * No.162 Date.8 to Date
     * 
     */
    public static Date Date2Date(Date o) {
        return o;
    }

    /**
     * No.163 Date.9 to double
     * 
     */
    public static double Date2double(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to double  "); //$NON-NLS-1$
    }

    /**
     * No.164 Date.10 to Double
     * 
     */
    public static Double Date2Double(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to Double  "); //$NON-NLS-1$
    }

    /**
     * No.165 Date.11 to float
     * 
     */
    public static float Date2float(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to float  "); //$NON-NLS-1$
    }

    /**
     * No.166 Date.12 to Float
     * 
     */
    public static Float Date2Float(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to Float  "); //$NON-NLS-1$
    }

    /**
     * No.167 Date.13 to BigDecimal
     * 
     */
    public static BigDecimal Date2BigDecimal(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to BigDecimal  "); //$NON-NLS-1$
    }

    /**
     * No.168 Date.14 to int
     * 
     */
    public static int Date2int(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to int  "); //$NON-NLS-1$
    }

    /**
     * No.169 Date.15 to Integer
     * 
     */
    public static Integer Date2Integer(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to Integer  "); //$NON-NLS-1$
    }

    /**
     * No.170 Date.16 to long
     * 
     */
    public static long Date2long(Date o) {
        if (o == null)
            return 0L;
        return o.getTime();
    }

    /**
     * No.171 Date.17 to Long
     * 
     */
    public static Long Date2Long(Date o) {
        if (o == null)
            return null;
        return Long.valueOf(o.getTime());
    }

    /**
     * No.172 Date.18 to Object
     * 
     */
    public static Object Date2Object(Date o) {
        return o;
    }

    /**
     * No.173 Date.19 to short
     * 
     */
    public static short Date2short(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to short  "); //$NON-NLS-1$
    }

    /**
     * No.174 Date.20 to Short
     * 
     */
    public static Short Date2Short(Date o) {
        throw new ConvertTypeNotSupportException("Can't support convert Date to Short  "); //$NON-NLS-1$
    }

    /**
     * No.175 Date.21 to String
     * 
     */
    public static String Date2String(Date o, String pattern) {
        if (o == null)
            return null;
        return TalendDate.formatDate(pattern, o);
    }

    /**
     * No.176 Date.22 to List
     * 
     */
    public static List Date2List(Date o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.177 double.1 to boolean
     * 
     */
    public static boolean double2boolean(double o) {
        throw new ConvertTypeNotSupportException("Can't support convert double to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.178 double.2 to Boolean
     * 
     */
    public static Boolean double2Boolean(double o) {
        throw new ConvertTypeNotSupportException("Can't support convert double to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.179 double.3 to byte
     * 
     */
    public static byte double2byte(double o) {
        return (byte) o;
    }

    /**
     * No.180 double.4 to Byte
     * 
     */
    public static Byte double2Byte(double o) {
        return Byte.valueOf((byte) o);
    }

    /**
     * No.181 double.5 to byte[]
     * 
     */
    public static byte[] double2byteArray(double o) {
        return new byte[] { (byte) o };
    }

    /**
     * No.182 double.6 to char
     * 
     */
    public static char double2char(double o) {
        return (char) o;
    }

    /**
     * No.183 double.7 to Character
     * 
     */
    public static Character double2Character(double o) {
        return Character.valueOf((char) o);
    }

    /**
     * No.184 double.8 to Date
     * 
     */
    public static Date double2Date(double o) {
        throw new ConvertTypeNotSupportException("Can't support convert double to Date  "); //$NON-NLS-1$
    }

    /**
     * No.185 double.9 to double
     * 
     */
    public static double double2double(double o) {
        return o;
    }

    /**
     * No.186 double.10 to Double
     * 
     */
    public static Double double2Double(double o) {
        return Double.valueOf(o);
    }

    /**
     * No.187 double.11 to float
     * 
     */
    public static float double2float(double o) {
        return (float) o;
    }

    /**
     * No.188 double.12 to Float
     * 
     */
    public static Float double2Float(double o) {
        return Float.valueOf((float) o);
    }

    /**
     * No.189 double.13 to BigDecimal
     * 
     */
    public static BigDecimal double2BigDecimal(double o) {
        return new BigDecimal(o);
    }

    /**
     * No.190 double.14 to int
     * 
     */
    public static int double2int(double o) {
        return (int) o;
    }

    /**
     * No.191 double.15 to Integer
     * 
     */
    public static Integer double2Integer(double o) {
        return Integer.valueOf((int) o);
    }

    /**
     * No.192 double.16 to long
     * 
     */
    public static long double2long(double o) {
        return (long) o;
    }

    /**
     * No.193 double.17 to Long
     * 
     */
    public static Long double2Long(double o) {
        return Long.valueOf((long) o);
    }

    /**
     * No.194 double.18 to Object
     * 
     */
    public static Object double2Object(double o) {
        return Double.valueOf(o);
    }

    /**
     * No.195 double.19 to short
     * 
     */
    public static short double2short(double o) {
        return (short) o;
    }

    /**
     * No.196 double.20 to Short
     * 
     */
    public static Short double2Short(double o) {
        return Short.valueOf((short) o);
    }

    /**
     * No.197 double.21 to String
     * 
     */
    public static String double2String(double o) {
        return String.valueOf(o);
    }

    /**
     * No.198 double.22 to List
     * 
     */
    public static List double2List(double o) {
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.199 Double.1 to boolean
     * 
     */
    public static boolean Double2boolean(Double o) {
        throw new ConvertTypeNotSupportException("Can't support convert Double to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.200 Double.2 to Boolean
     * 
     */
    public static Boolean Double2Boolean(Double o) {
        throw new ConvertTypeNotSupportException("Can't support convert Double to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.201 Double.3 to byte
     * 
     */
    public static byte Double2byte(Double o) {
        if (o == null)
            return (byte) 0;
        return o.byteValue();
    }

    /**
     * No.202 Double.4 to Byte
     * 
     */
    public static Byte Double2Byte(Double o) {
        if (o == null)
            return null;
        return Byte.valueOf(o.byteValue());
    }

    /**
     * No.203 Double.5 to byte[]
     * 
     */
    public static byte[] Double2byteArray(Double o) {
        if (o == null)
            return null;
        return new byte[] { o.byteValue() };
    }

    /**
     * No.204 Double.6 to char
     * 
     */
    public static char Double2char(Double o) {
        if (o == null)
            return (char) o.doubleValue();
        return (char) o.doubleValue();
    }

    /**
     * No.205 Double.7 to Character
     * 
     */
    public static Character Double2Character(Double o) {
        if (o == null)
            return null;
        return Character.valueOf((char) o.doubleValue());
    }

    /**
     * No.206 Double.8 to Date
     * 
     */
    public static Date Double2Date(Double o) {
        throw new ConvertTypeNotSupportException("Can't support convert Double to Date  "); //$NON-NLS-1$
    }

    /**
     * No.207 Double.9 to double
     * 
     */
    public static double Double2double(Double o) {
        if (o == null)
            return 0d;
        return o.doubleValue();
    }

    /**
     * No.208 Double.10 to Double
     * 
     */
    public static Double Double2Double(Double o) {
        if (o == null)
            return null;
        return o;
    }

    /**
     * No.209 Double.11 to float
     * 
     */
    public static float Double2float(Double o) {
        if (o == null)
            return 0f;
        return o.floatValue();
    }

    /**
     * No.210 Double.12 to Float
     * 
     */
    public static Float Double2Float(Double o) {
        if (o == null)
            return null;
        return Float.valueOf(o.floatValue());
    }

    /**
     * No.211 Double.13 to BigDecimal
     * 
     */
    public static BigDecimal Double2BigDecimal(Double o) {
        if (o == null)
            return null;
        return new BigDecimal(o.doubleValue());
    }

    /**
     * No.212 Double.14 to int
     * 
     */
    public static int Double2int(Double o) {
        if (o == null)
            return 0;
        return o.intValue();
    }

    /**
     * No.213 Double.15 to Integer
     * 
     */
    public static Integer Double2Integer(Double o) {
        if (o == null)
            return null;
        return Integer.valueOf(o.intValue());
    }

    /**
     * No.214 Double.16 to long
     * 
     */
    public static long Double2long(Double o) {
        if (o == null)
            return 0L;
        return o.longValue();
    }

    /**
     * No.215 Double.17 to Long
     * 
     */
    public static Long Double2Long(Double o) {
        if (o == null)
            return null;
        return Long.valueOf(o.longValue());
    }

    /**
     * No.216 Double.18 to Object
     * 
     */
    public static Object Double2Object(Double o) {
        return o;
    }

    /**
     * No.217 Double.19 to short
     * 
     */
    public static short Double2short(Double o) {
        if (o == null)
            return (short) 0;
        return o.shortValue();
    }

    /**
     * No.218 Double.20 to Short
     * 
     */
    public static Short Double2Short(Double o) {
        if (o == null)
            return null;
        return Short.valueOf(o.shortValue());
    }

    /**
     * No.219 Double.21 to String
     * 
     */
    public static String Double2String(Double o) {
        if (o == null)
            return null;
        return o.toString();
    }

    /**
     * No.220 Double.22 to List
     * 
     */
    public static List Double2List(Double o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        list.add(o);
        return list;

    }

    /**
     * No.221 float.1 to boolean
     * 
     */
    public static boolean float2boolean(float o) {
        throw new ConvertTypeNotSupportException("Can't support convert float to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.222 float.2 to Boolean
     * 
     */
    public static Boolean float2Boolean(float o) {
        throw new ConvertTypeNotSupportException("Can't support convert float to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.223 float.3 to byte
     * 
     */
    public static byte float2byte(float o) {
        return (byte) o;
    }

    /**
     * No.224 float.4 to Byte
     * 
     */
    public static Byte float2Byte(float o) {
        return Byte.valueOf((byte) o);
    }

    /**
     * No.225 float.5 to byte[]
     * 
     */
    public static byte[] float2byteArray(float o) {
        return new byte[] { (byte) o };
    }

    /**
     * No.226 float.6 to char
     * 
     */
    public static char float2char(float o) {
        return (char) o;
    }

    /**
     * No.227 float.7 to Character
     * 
     */
    public static Character float2Character(float o) {
        return Character.valueOf((char) o);
    }

    /**
     * No.228 float.8 to Date
     * 
     */
    public static Date float2Date(float o) {
        throw new ConvertTypeNotSupportException("Can't support convert float to Date  "); //$NON-NLS-1$
    }

    /**
     * No.229 float.9 to double
     * 
     */
    public static double float2double(float o) {
        return (double) o;
    }

    /**
     * No.230 float.10 to Double
     * 
     */
    public static Double float2Double(float o) {
        return Double.valueOf(o);
    }

    /**
     * No.231 float.11 to float
     * 
     */
    public static float float2float(float o) {
        return o;
    }

    /**
     * No.232 float.12 to Float
     * 
     */
    public static Float float2Float(float o) {
        return Float.valueOf(o);
    }

    /**
     * No.233 float.13 to BigDecimal
     * 
     */
    public static BigDecimal float2BigDecimal(float o) {
        return new BigDecimal(o);
    }

    /**
     * No.234 float.14 to int
     * 
     */
    public static int float2int(float o) {
        return (int) o;
    }

    /**
     * No.235 float.15 to Integer
     * 
     */
    public static Integer float2Integer(float o) {
        return Integer.valueOf((int) o);
    }

    /**
     * No.236 float.16 to long
     * 
     */
    public static long float2long(float o) {
        return (long) o;
    }

    /**
     * No.237 float.17 to Long
     * 
     */
    public static Long float2Long(float o) {
        return Long.valueOf((long) o);
    }

    /**
     * No.238 float.18 to Object
     * 
     */
    public static Object float2Object(float o) {
        return Float.valueOf(o);
    }

    /**
     * No.239 float.19 to short
     * 
     */
    public static short float2short(float o) {
        return (short) o;
    }

    /**
     * No.240 float.20 to Short
     * 
     */
    public static Short float2Short(float o) {
        return Short.valueOf((short) o);
    }

    /**
     * No.241 float.21 to String
     * 
     */
    public static String float2String(float o) {
        return String.valueOf(o);
    }

    /**
     * No.242 float.22 to List
     * 
     */
    public static List float2List(float o) {
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.243 Float.1 to boolean
     * 
     */
    public static boolean Float2boolean(Float o) {
        throw new ConvertTypeNotSupportException("Can't support convert Float to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.244 Float.2 to Boolean
     * 
     */
    public static Boolean Float2Boolean(Float o) {
        throw new ConvertTypeNotSupportException("Can't support convert Float to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.245 Float.3 to byte
     * 
     */
    public static byte Float2byte(Float o) {
        if (o == null)
            return (byte) 0;
        return o.byteValue();
    }

    /**
     * No.246 Float.4 to Byte
     * 
     */
    public static Byte Float2Byte(Float o) {
        if (o == null)
            return null;
        return Byte.valueOf(o.byteValue());
    }

    /**
     * No.247 Float.5 to byte[]
     * 
     */
    public static byte[] Float2byteArray(Float o) {
        if (o == null)
            return null;
        return new byte[] { o.byteValue() };
    }

    /**
     * No.248 Float.6 to char
     * 
     */
    public static char Float2char(Float o) {
        if (o == null)
            return (char) 0;
        return (char) o.floatValue();
    }

    /**
     * No.249 Float.7 to Character
     * 
     */
    public static Character Float2Character(Float o) {
        if (o == null)
            return null;

        return Character.valueOf((char) o.floatValue());

    }

    /**
     * No.250 Float.8 to Date
     * 
     */
    public static Date Float2Date(Float o) {
        throw new ConvertTypeNotSupportException("Can't support convert Float to Date  "); //$NON-NLS-1$
    }

    /**
     * No.251 Float.9 to double
     * 
     */
    public static double Float2double(Float o) {
        if (o == null)
            return 0;
        return o.doubleValue();
    }

    /**
     * No.252 Float.10 to Double
     * 
     */
    public static Double Float2Double(Float o) {
        if (o == null)
            return null;
        return Double.valueOf(o);
    }

    /**
     * No.253 Float.11 to float
     * 
     */
    public static float Float2float(Float o) {
        if (o == null)
            return 0f;
        return o.floatValue();
    }

    /**
     * No.254 Float.12 to Float
     * 
     */
    public static Float Float2Float(Float o) {
        if (o == null)
            return null;
        return Float.valueOf(o);
    }

    /**
     * No.255 Float.13 to BigDecimal
     * 
     */
    public static BigDecimal Float2BigDecimal(Float o) {
        if (o == null)
            return null;
        return new BigDecimal(o.floatValue());
    }

    /**
     * No.256 Float.14 to int
     * 
     */
    public static int Float2int(Float o) {
        if (o == null)
            return 0;
        return o.intValue();
    }

    /**
     * No.257 Float.15 to Integer
     * 
     */
    public static Integer Float2Integer(Float o) {
        if (o == null)
            return null;
        return Integer.valueOf(o.intValue());
    }

    /**
     * No.258 Float.16 to long
     * 
     */
    public static long Float2long(Float o) {
        if (o == null)
            return 0L;
        return o.longValue();
    }

    /**
     * No.259 Float.17 to Long
     * 
     */
    public static Long Float2Long(Float o) {
        if (o == null)
            return null;
        return Long.valueOf(o.longValue());
    }

    /**
     * No.260 Float.18 to Object
     * 
     */
    public static Object Float2Object(Float o) {
        return o;
    }

    /**
     * No.261 Float.19 to short
     * 
     */
    public static short Float2short(Float o) {
        if (o == null)
            return (short) 0;
        return o.shortValue();
    }

    /**
     * No.262 Float.20 to Short
     * 
     */
    public static Short Float2Short(Float o) {
        if (o == null)
            return null;
        return Short.valueOf(o.shortValue());
    }

    /**
     * No.263 Float.21 to String
     * 
     */
    public static String Float2String(Float o) {
        if (o == null)
            return null;
        return o.toString();
    }

    /**
     * No.264 Float.22 to List
     * 
     */
    public static List Float2List(Float o) {
        if (o == null)
            return new ArrayList();
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.265 BigDecimal.1 to boolean
     * 
     */
    public static boolean BigDecimal2boolean(BigDecimal o) {
        throw new ConvertTypeNotSupportException("Can't support convert BigDecimal to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.266 BigDecimal.2 to Boolean
     * 
     */
    public static Boolean BigDecimal2Boolean(BigDecimal o) {
        throw new ConvertTypeNotSupportException("Can't support convert BigDecimal to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.267 BigDecimal.3 to byte
     * 
     */
    public static byte BigDecimal2byte(BigDecimal o) {
        if (o == null)
            return (byte) 0;
        return o.byteValue();
    }

    /**
     * No.268 BigDecimal.4 to Byte
     * 
     */
    public static Byte BigDecimal2Byte(BigDecimal o) {
        if (o == null)
            return null;
        return Byte.valueOf(o.byteValue());
    }

    /**
     * No.269 BigDecimal.5 to byte[]
     * 
     */
    public static byte[] BigDecimal2byteArray(BigDecimal o) {
        if (o == null)
            return null;
        return new byte[] { o.byteValue() };
    }

    /**
     * No.270 BigDecimal.6 to char
     * 
     */
    public static char BigDecimal2char(BigDecimal o) {
        if (o == null)
            return (char) 0;
        return (char) o.intValue();
    }

    /**
     * No.271 BigDecimal.7 to Character
     * 
     */
    public static Character BigDecimal2Character(BigDecimal o) {
        if (o == null)
            return null;
        return Character.valueOf((char) o.intValue());
    }

    /**
     * No.272 BigDecimal.8 to Date
     * 
     */
    public static Date BigDecimal2Date(BigDecimal o) {
        throw new ConvertTypeNotSupportException("Can't support convert BigDecimal to Date  "); //$NON-NLS-1$
    }

    /**
     * No.273 BigDecimal.9 to double
     * 
     */
    public static double BigDecimal2double(BigDecimal o) {
        if (o == null)
            return (double) 0;
        return o.doubleValue();
    }

    /**
     * No.274 BigDecimal.10 to Double
     * 
     */
    public static Double BigDecimal2Double(BigDecimal o) {
        if (o == null)
            return null;
        return Double.valueOf(o.doubleValue());
    }

    /**
     * No.275 BigDecimal.11 to float
     * 
     */
    public static float BigDecimal2float(BigDecimal o) {
        if (o == null)
            return 0f;
        return o.floatValue();
    }

    /**
     * No.276 BigDecimal.12 to Float
     * 
     */
    public static Float BigDecimal2Float(BigDecimal o) {
        if (o == null)
            return null;
        return Float.valueOf(o.floatValue());
    }

    /**
     * No.277 BigDecimal.13 to BigDecimal
     * 
     */
    public static BigDecimal BigDecimal2BigDecimal(BigDecimal o) {
        return o;
    }

    /**
     * No.278 BigDecimal.14 to int
     * 
     */
    public static int BigDecimal2int(BigDecimal o) {
        if (o == null)
            return 0;
        return o.intValue();
    }

    /**
     * No.279 BigDecimal.15 to Integer
     * 
     */
    public static Integer BigDecimal2Integer(BigDecimal o) {
        if (o == null)
            return null;
        return Integer.valueOf(o.intValue());
    }

    /**
     * No.280 BigDecimal.16 to long
     * 
     */
    public static long BigDecimal2long(BigDecimal o) {
        if (o == null)
            return 0L;
        return o.longValue();
    }

    /**
     * No.281 BigDecimal.17 to Long
     * 
     */
    public static Long BigDecimal2Long(BigDecimal o) {
        if (o == null)
            return null;
        return Long.valueOf(o.longValue());
    }

    /**
     * No.282 BigDecimal.18 to Object
     * 
     */
    public static Object BigDecimal2Object(BigDecimal o) {
        return o;
    }

    /**
     * No.283 BigDecimal.19 to short
     * 
     */
    public static short BigDecimal2short(BigDecimal o) {
        if (o == null)
            return (short) 0;
        return o.shortValue();
    }

    /**
     * No.284 BigDecimal.20 to Short
     * 
     */
    public static Short BigDecimal2Short(BigDecimal o) {
        if (o == null)
            return null;
        return Short.valueOf((short) o.intValue());
    }

    /**
     * No.285 BigDecimal.21 to String
     * 
     */
    public static String BigDecimal2String(BigDecimal o) {
        if (o == null)
            return null;
        return o.toString();
    }

    /**
     * No.286 BigDecimal.22 to List
     * 
     */
    public static List BigDecimal2List(BigDecimal o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.287 int.1 to boolean
     * 
     */
    public static boolean int2boolean(int o) {
        throw new ConvertTypeNotSupportException("Can't support convert int to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.288 int.2 to Boolean
     * 
     */
    public static Boolean int2Boolean(int o) {
        throw new ConvertTypeNotSupportException("Can't support convert int to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.289 int.3 to byte
     * 
     */
    public static byte int2byte(int o) {
        return (byte) o;
    }

    /**
     * No.290 int.4 to Byte
     * 
     */
    public static Byte int2Byte(int o) {
        return Byte.valueOf((byte) o);
    }

    /**
     * No.291 int.5 to byte[]
     * 
     */
    public static byte[] int2byteArray(int o) {
        return new byte[] { (byte) o };
    }

    /**
     * No.292 int.6 to char
     * 
     */
    public static char int2char(int o) {
        return (char) o;
    }

    /**
     * No.293 int.7 to Character
     * 
     */
    public static Character int2Character(int o) {
        return Character.valueOf((char) o);
    }

    /**
     * No.294 int.8 to Date
     * 
     */
    public static Date int2Date(int o) {
        throw new ConvertTypeNotSupportException("Can't support convert int to Date  "); //$NON-NLS-1$
    }

    /**
     * No.295 int.9 to double
     * 
     */
    public static double int2double(int o) {
        return (double) o;
    }

    /**
     * No.296 int.10 to Double
     * 
     */
    public static Double int2Double(int o) {
        return Double.valueOf(o);
    }

    /**
     * No.297 int.11 to float
     * 
     */
    public static float int2float(int o) {
        return (float) o;
    }

    /**
     * No.298 int.12 to Float
     * 
     */
    public static Float int2Float(int o) {
        return Float.valueOf(o);
    }

    /**
     * No.299 int.13 to BigDecimal
     * 
     */
    public static BigDecimal int2BigDecimal(int o) {
        return new BigDecimal(o);
    }

    /**
     * No.300 int.14 to int
     * 
     */
    public static int int2int(int o) {
        return o;
    }

    /**
     * No.301 int.15 to Integer
     * 
     */
    public static Integer int2Integer(int o) {
        return Integer.valueOf(o);
    }

    /**
     * No.302 int.16 to long
     * 
     */
    public static long int2long(int o) {
        return (long) o;
    }

    /**
     * No.303 int.17 to Long
     * 
     */
    public static Long int2Long(int o) {
        return Long.valueOf(o);
    }

    /**
     * No.304 int.18 to Object
     * 
     */
    public static Object int2Object(int o) {
        return Integer.valueOf(o);
    }

    /**
     * No.305 int.19 to short
     * 
     */
    public static short int2short(int o) {
        return (short) o;
    }

    /**
     * No.306 int.20 to Short
     * 
     */
    public static Short int2Short(int o) {
        return Short.valueOf((short) o);
    }

    /**
     * No.307 int.21 to String
     * 
     */
    public static String int2String(int o) {
        return String.valueOf(o);
    }

    /**
     * No.308 int.22 to List
     * 
     */
    public static List int2List(int o) {
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.309 Integer.1 to boolean
     * 
     */
    public static boolean Integer2boolean(Integer o) {
        throw new ConvertTypeNotSupportException("Can't support convert Integer to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.310 Integer.2 to Boolean
     * 
     */
    public static Boolean Integer2Boolean(Integer o) {
        throw new ConvertTypeNotSupportException("Can't support convert Integer to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.311 Integer.3 to byte
     * 
     */
    public static byte Integer2byte(Integer o) {
        if (o == null)
            return (byte) 0;
        return o.byteValue();
    }

    /**
     * No.312 Integer.4 to Byte
     * 
     */
    public static Byte Integer2Byte(Integer o) {
        if (o == null)
            return null;
        return Byte.valueOf(o.byteValue());
    }

    /**
     * No.313 Integer.5 to byte[]
     * 
     */
    public static byte[] Integer2byteArray(Integer o) {
        if (o == null)
            return null;
        return new byte[] { o.byteValue() };

    }

    /**
     * No.314 Integer.6 to char
     * 
     */
    public static char Integer2char(Integer o) {
        if (o == null)
            return (char) 0;
        return (char) o.intValue();
    }

    /**
     * No.315 Integer.7 to Character
     * 
     */
    public static Character Integer2Character(Integer o) {
        if (o == null)
            return null;
        return Character.valueOf((char) o.intValue());
    }

    /**
     * No.316 Integer.8 to Date
     * 
     */
    public static Date Integer2Date(Integer o) {
        throw new ConvertTypeNotSupportException("Can't support convert Integer to Date  "); //$NON-NLS-1$
    }

    /**
     * No.317 Integer.9 to double
     * 
     */
    public static double Integer2double(Integer o) {
        if (o == null)
            return (double) 0;
        return o.doubleValue();
    }

    /**
     * No.318 Integer.10 to Double
     * 
     */
    public static Double Integer2Double(Integer o) {
        if (o == null)
            return null;
        return Double.valueOf(o.doubleValue());
    }

    /**
     * No.319 Integer.11 to float
     * 
     */
    public static float Integer2float(Integer o) {
        if (o == null)
            return 0f;
        return o.floatValue();
    }

    /**
     * No.320 Integer.12 to Float
     * 
     */
    public static Float Integer2Float(Integer o) {
        if (o == null)
            return null;
        return Float.valueOf(o.floatValue());
    }

    /**
     * No.321 Integer.13 to BigDecimal
     * 
     */
    public static BigDecimal Integer2BigDecimal(Integer o) {
        if (o == null)
            return null;
        return new BigDecimal(o.intValue());
    }

    /**
     * No.322 Integer.14 to int
     * 
     */
    public static int Integer2int(Integer o) {
        if (o == null)
            return 0;
        return o.intValue();
    }

    /**
     * No.323 Integer.15 to Integer
     * 
     */
    public static Integer Integer2Integer(Integer o) {
        return o;
    }

    /**
     * No.324 Integer.16 to long
     * 
     */
    public static long Integer2long(Integer o) {
        if (o == null)
            return 0L;
        return o.longValue();
    }

    /**
     * No.325 Integer.17 to Long
     * 
     */
    public static Long Integer2Long(Integer o) {
        if (o == null)
            return null;
        return Long.valueOf(o);
    }

    /**
     * No.326 Integer.18 to Object
     * 
     */
    public static Object Integer2Object(Integer o) {
        return o;
    }

    /**
     * No.327 Integer.19 to short
     * 
     */
    public static short Integer2short(Integer o) {
        if (o == null)
            return (short) 0;
        return o.shortValue();
    }

    /**
     * No.328 Integer.20 to Short
     * 
     */
    public static Short Integer2Short(Integer o) {
        if (o == null)
            return null;
        return Short.valueOf(o.shortValue());
    }

    /**
     * No.329 Integer.21 to String
     * 
     */
    public static String Integer2String(Integer o) {
        if (o == null)
            return null;
        return o.toString();
    }

    /**
     * No.330 Integer.22 to List
     * 
     */
    public static List Integer2List(Integer o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.331 long.1 to boolean
     * 
     */
    public static boolean long2boolean(long o) {
        throw new ConvertTypeNotSupportException("Can't support convert long to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.332 long.2 to Boolean
     * 
     */
    public static Boolean long2Boolean(long o) {
        throw new ConvertTypeNotSupportException("Can't support convert long to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.333 long.3 to byte
     * 
     */
    public static byte long2byte(long o) {
        return (byte) o;
    }

    /**
     * No.334 long.4 to Byte
     * 
     */
    public static Byte long2Byte(long o) {
        return Byte.valueOf((byte) o);
    }

    /**
     * No.335 long.5 to byte[]
     * 
     */
    public static byte[] long2byteArray(long o) {
        return new byte[] { (byte) o };
    }

    /**
     * No.336 long.6 to char
     * 
     */
    public static char long2char(long o) {
        return (char) o;
    }

    /**
     * No.337 long.7 to Character
     * 
     */
    public static Character long2Character(long o) {
        return Character.valueOf((char) o);
    }

    /**
     * No.338 long.8 to Date
     * 
     */
    public static Date long2Date(long o) {
        throw new ConvertTypeNotSupportException("Can't support convert long to Date  "); //$NON-NLS-1$
    }

    /**
     * No.339 long.9 to double
     * 
     */
    public static double long2double(long o) {
        return (double) o;
    }

    /**
     * No.340 long.10 to Double
     * 
     */
    public static Double long2Double(long o) {
        return Double.valueOf(o);
    }

    /**
     * No.341 long.11 to float
     * 
     */
    public static float long2float(long o) {
        return (float) o;
    }

    /**
     * No.342 long.12 to Float
     * 
     */
    public static Float long2Float(long o) {
        return Float.valueOf(o);
    }

    /**
     * No.343 long.13 to BigDecimal
     * 
     */
    public static BigDecimal long2BigDecimal(long o) {
        return new BigDecimal(o);
    }

    /**
     * No.344 long.14 to int
     * 
     */
    public static int long2int(long o) {
        return (int) o;
    }

    /**
     * No.345 long.15 to Integer
     * 
     */
    public static Integer long2Integer(long o) {
        return Integer.valueOf((int) o);
    }

    /**
     * No.346 long.16 to long
     * 
     */
    public static long long2long(long o) {
        return o;
    }

    /**
     * No.347 long.17 to Long
     * 
     */
    public static Long long2Long(long o) {
        return Long.valueOf(o);
    }

    /**
     * No.348 long.18 to Object
     * 
     */
    public static Object long2Object(long o) {
        return Long.valueOf(o);
    }

    /**
     * No.349 long.19 to short
     * 
     */
    public static short long2short(long o) {
        return (short) o;
    }

    /**
     * No.350 long.20 to Short
     * 
     */
    public static Short long2Short(long o) {
        return Short.valueOf((short) o);
    }

    /**
     * No.351 long.21 to String
     * 
     */
    public static String long2String(long o) {
        return String.valueOf(o);
    }

    /**
     * No.352 long.22 to List
     * 
     */
    public static List long2List(long o) {
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.353 Long.1 to boolean
     * 
     */
    public static boolean Long2boolean(Long o) {
        throw new ConvertTypeNotSupportException("Can't support convert Long to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.354 Long.2 to Boolean
     * 
     */
    public static Boolean Long2Boolean(Long o) {
        throw new ConvertTypeNotSupportException("Can't support convert Long to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.355 Long.3 to byte
     * 
     */
    public static byte Long2byte(Long o) {
        if (o == null)
            return (byte) 0;
        return o.byteValue();
    }

    /**
     * No.356 Long.4 to Byte
     * 
     */
    public static Byte Long2Byte(Long o) {
        if (o == null)
            return null;
        return Byte.valueOf(o.byteValue());
    }

    /**
     * No.357 Long.5 to byte[]
     * 
     */
    public static byte[] Long2byteArray(Long o) {
        if (o == null)
            return null;
        return new byte[] { o.byteValue() };
    }

    /**
     * No.358 Long.6 to char
     * 
     */
    public static char Long2char(Long o) {
        if (o == null)
            return (char) 0;
        return (char) o.longValue();
    }

    /**
     * No.359 Long.7 to Character
     * 
     */
    public static Character Long2Character(Long o) {
        if (o == null)
            return null;
        return Character.valueOf((char) o.longValue());
    }

    /**
     * No.360 Long.8 to Date
     * 
     */
    public static Date Long2Date(Long o) {
        throw new ConvertTypeNotSupportException("Can't support convert Long to Date  "); //$NON-NLS-1$
    }

    /**
     * No.361 Long.9 to double
     * 
     */
    public static double Long2double(Long o) {
        if (o == null)
            return (double) 0;
        return o.doubleValue();
    }

    /**
     * No.362 Long.10 to Double
     * 
     */
    public static Double Long2Double(Long o) {
        if (o == null)
            return null;
        return Double.valueOf(o);
    }

    /**
     * No.363 Long.11 to float
     * 
     */
    public static float Long2float(Long o) {
        if (o == null)
            return 0f;
        return o.floatValue();
    }

    /**
     * No.364 Long.12 to Float
     * 
     */
    public static Float Long2Float(Long o) {
        if (o == null)
            return null;
        return Float.valueOf(o);
    }

    /**
     * No.365 Long.13 to BigDecimal
     * 
     */
    public static BigDecimal Long2BigDecimal(Long o) {
        if (o == null)
            return null;
        return new BigDecimal(o.longValue());
    }

    /**
     * No.366 Long.14 to int
     * 
     */
    public static int Long2int(Long o) {
        if (o == null)
            return 0;
        return o.intValue();
    }

    /**
     * No.367 Long.15 to Integer
     * 
     */
    public static Integer Long2Integer(Long o) {
        if (o == null)
            return null;
        return Integer.valueOf(o.intValue());
    }

    /**
     * No.368 Long.16 to long
     * 
     */
    public static long Long2long(Long o) {
        if (o == null)
            return 0L;
        return o.longValue();
    }

    /**
     * No.369 Long.17 to Long
     * 
     */
    public static Long Long2Long(Long o) {
        return o;
    }

    /**
     * No.370 Long.18 to Object
     * 
     */
    public static Object Long2Object(Long o) {
        return o;
    }

    /**
     * No.371 Long.19 to short
     * 
     */
    public static short Long2short(Long o) {
        if (o == null)
            return (short) 0;
        return o.shortValue();
    }

    /**
     * No.372 Long.20 to Short
     * 
     */
    public static Short Long2Short(Long o) {
        if (o == null)
            return null;
        return Short.valueOf(o.shortValue());
    }

    /**
     * No.373 Long.21 to String
     * 
     */
    public static String Long2String(Long o) {
        if (o == null)
            return null;
        return o.toString();
    }

    /**
     * No.374 Long.22 to List
     * 
     */
    public static List Long2List(Long o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.375 Object.1 to boolean
     * 
     */
    public static boolean Object2boolean(Object o) {
        if (o == null)
            return false;
        if (o instanceof Boolean)
            return ((Boolean) o).booleanValue();
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.376 Object.2 to Boolean
     * 
     */
    public static Boolean Object2Boolean(Object o) {
        if (o == null)
            return null;
        if (o instanceof Boolean)
            return (Boolean) o;
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.377 Object.3 to byte
     * 
     */
    public static byte Object2byte(Object o) {
        if (o == null)
            return (byte) 0;
        if (o instanceof Byte)
            return ((Byte) o).byteValue();
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.378 Object.4 to Byte
     * 
     */
    public static Byte Object2Byte(Object o) {
        if (o == null)
            return null;
        if (o instanceof Byte)
            return (Byte) o;
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.379 Object.5 to byte[]
     * 
     */
    public static byte[] Object2byteArray(Object o) {
        if (o == null)
            return null;
        if (o instanceof byte[])
            return (byte[]) o;
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.380 Object.6 to char
     * 
     */
    public static char Object2char(Object o) {
        if (o == null)
            return (char) 0;
        if (o instanceof Character)
            return ((Character) o).charValue();
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.381 Object.7 to Character
     * 
     */
    public static Character Object2Character(Object o) {
        if (o == null)
            return null;
        if (o instanceof Character)
            return (Character) o;
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.382 Object.8 to Date
     * 
     */
    public static Date Object2Date(Object o) {
        if (o == null)
            return null;
        if (o instanceof Date)
            return (Date) o;
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.383 Object.9 to double
     * 
     */
    public static double Object2double(Object o) {
        if (o == null)
            return (double) 0;
        if (o instanceof Double)
            return ((Double) o).doubleValue();
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.384 Object.10 to Double
     * 
     */
    public static Double Object2Double(Object o) {
        if (o == null)
            return null;
        if (o instanceof Double)
            return (Double) o;
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.385 Object.11 to float
     * 
     */
    public static float Object2float(Object o) {
        if (o == null)
            return 0f;
        if (o instanceof Float)
            return ((Float) o).floatValue();
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.386 Object.12 to Float
     * 
     */
    public static Float Object2Float(Object o) {
        if (o == null)
            return null;
        if (o instanceof Float)
            return (Float) o;
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.387 Object.13 to BigDecimal
     * 
     */
    public static BigDecimal Object2BigDecimal(Object o) {
        if (o == null)
            return null;
        if (o instanceof BigDecimal)
            return (BigDecimal) o;
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.388 Object.14 to int
     * 
     */
    public static int Object2int(Object o) {
        if (o == null)
            return 0;
        if (o instanceof Integer)
            return ((Integer) o).intValue();
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.389 Object.15 to Integer
     * 
     */
    public static Integer Object2Integer(Object o) {
        if (o == null)
            return null;
        if (o instanceof Integer)
            return (Integer) o;
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.390 Object.16 to long
     * 
     */
    public static long Object2long(Object o) {
        if (o == null)
            return 0;
        if (o instanceof Long)
            return ((Long) o).longValue();
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.391 Object.17 to Long
     * 
     */
    public static Long Object2Long(Object o) {
        if (o == null)
            return null;
        if (o instanceof Long)
            return (Long) o;
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.392 Object.18 to Object
     * 
     */
    public static Object Object2Object(Object o) {
        return o;

    }

    /**
     * No.393 Object.19 to short
     * 
     */
    public static short Object2short(Object o) {
        if (o == null)
            return (short) 0;
        if (o instanceof Short)
            return ((Short) o).shortValue();
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.394 Object.20 to Short
     * 
     */
    public static Short Object2Short(Object o) {
        if (o == null)
            return null;
        if (o instanceof Short)
            return (Short) o;
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.395 Object.21 to String
     * 
     */
    public static String Object2String(Object o) {
        if (o == null)
            return null;
        return o.toString();

    }

    /**
     * No.396 Object.22 to List
     * 
     */
    public static List Object2List(Object o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.397 short.1 to boolean
     * 
     */
    public static boolean short2boolean(short o) {
        throw new ConvertTypeNotSupportException("Can't support convert short to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.398 short.2 to Boolean
     * 
     */
    public static Boolean short2Boolean(short o) {
        throw new ConvertTypeNotSupportException("Can't support convert short to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.399 short.3 to byte
     * 
     */
    public static byte short2byte(short o) {
        return (byte) o;
    }

    /**
     * No.400 short.4 to Byte
     * 
     */
    public static Byte short2Byte(short o) {
        return Byte.valueOf((byte) o);
    }

    /**
     * No.401 short.5 to byte[]
     * 
     */
    public static byte[] short2byteArray(short o) {
        return new byte[] { (byte) o };
    }

    /**
     * No.402 short.6 to char
     * 
     */
    public static char short2char(short o) {
        return (char) o;
    }

    /**
     * No.403 short.7 to Character
     * 
     */
    public static Character short2Character(short o) {
        return Character.valueOf((char) o);
    }

    /**
     * No.404 short.8 to Date
     * 
     */
    public static Date short2Date(short o) {
        throw new ConvertTypeNotSupportException("Can't support convert short to Date  "); //$NON-NLS-1$
    }

    /**
     * No.405 short.9 to double
     * 
     */
    public static double short2double(short o) {
        return (double) o;
    }

    /**
     * No.406 short.10 to Double
     * 
     */
    public static Double short2Double(short o) {
        return Double.valueOf((double) o);
    }

    /**
     * No.407 short.11 to float
     * 
     */
    public static float short2float(short o) {
        return (float) o;
    }

    /**
     * No.408 short.12 to Float
     * 
     */
    public static Float short2Float(short o) {
        return Float.valueOf((float) o);
    }

    /**
     * No.409 short.13 to BigDecimal
     * 
     */
    public static BigDecimal short2BigDecimal(short o) {
        return new BigDecimal(o);
    }

    /**
     * No.410 short.14 to int
     * 
     */
    public static int short2int(short o) {
        return (int) o;
    }

    /**
     * No.411 short.15 to Integer
     * 
     */
    public static Integer short2Integer(short o) {
        return Integer.valueOf((int) o);
    }

    /**
     * No.412 short.16 to long
     * 
     */
    public static long short2long(short o) {
        return (long) o;
    }

    /**
     * No.413 short.17 to Long
     * 
     */
    public static Long short2Long(short o) {
        return Long.valueOf(o);
    }

    /**
     * No.414 short.18 to Object
     * 
     */
    public static Object short2Object(short o) {
        return Short.valueOf(o);
    }

    /**
     * No.415 short.19 to short
     * 
     */
    public static short short2short(short o) {
        return o;
    }

    /**
     * No.416 short.20 to Short
     * 
     */
    public static Short short2Short(short o) {
        return Short.valueOf(o);
    }

    /**
     * No.417 short.21 to String
     * 
     */
    public static String short2String(short o) {
        return String.valueOf(o);
    }

    /**
     * No.418 short.22 to List
     * 
     */
    public static List short2List(short o) {
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.419 Short.1 to boolean
     * 
     */
    public static boolean Short2boolean(Short o) {
        throw new ConvertTypeNotSupportException("Can't support convert Short to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.420 Short.2 to Boolean
     * 
     */
    public static Boolean Short2Boolean(Short o) {
        throw new ConvertTypeNotSupportException("Can't support convert Short to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.421 Short.3 to byte
     * 
     */
    public static byte Short2byte(Short o) {
        if (o == null)
            return (byte) 0;
        return (byte) o.byteValue();
    }

    /**
     * No.422 Short.4 to Byte
     * 
     */
    public static Byte Short2Byte(Short o) {
        if (o == null)
            return null;
        return Byte.valueOf(o.byteValue());
    }

    /**
     * No.423 Short.5 to byte[]
     * 
     */
    public static byte[] Short2byteArray(Short o) {
        if (o == null)
            return null;
        return new byte[] { o.byteValue() };
    }

    /**
     * No.424 Short.6 to char
     * 
     */
    public static char Short2char(Short o) {
        if (o == null)
            return (char) 0;
        return (char) o.shortValue();
    }

    /**
     * No.425 Short.7 to Character
     * 
     */
    public static Character Short2Character(Short o) {
        if (o == null)
            return null;
        return Character.valueOf((char) o.shortValue());
    }

    /**
     * No.426 Short.8 to Date
     * 
     */
    public static Date Short2Date(Short o) {
        throw new ConvertTypeNotSupportException("Can't support convert Short to Date  "); //$NON-NLS-1$
    }

    /**
     * No.427 Short.9 to double
     * 
     */
    public static double Short2double(Short o) {
        if (o == null)
            return (double) 0;
        return o.doubleValue();
    }

    /**
     * No.428 Short.10 to Double
     * 
     */
    public static Double Short2Double(Short o) {
        if (o == null)
            return null;
        return Double.valueOf(o.doubleValue());
    }

    /**
     * No.429 Short.11 to float
     * 
     */
    public static float Short2float(Short o) {
        if (o == null)
            return 0f;
        return o.floatValue();
    }

    /**
     * No.430 Short.12 to Float
     * 
     */
    public static Float Short2Float(Short o) {
        if (o == null)
            return null;
        return Float.valueOf(o.floatValue());

    }

    /**
     * No.431 Short.13 to BigDecimal
     * 
     */
    public static BigDecimal Short2BigDecimal(Short o) {
        if (o == null)
            return null;
        return new BigDecimal(o.shortValue());
    }

    /**
     * No.432 Short.14 to int
     * 
     */
    public static int Short2int(Short o) {
        if (o == null)
            return 0;
        return o.intValue();
    }

    /**
     * No.433 Short.15 to Integer
     * 
     */
    public static Integer Short2Integer(Short o) {
        if (o == null)
            return null;
        return Integer.valueOf(o.intValue());
    }

    /**
     * No.434 Short.16 to long
     * 
     */
    public static long Short2long(Short o) {
        if (o == null)
            return 0L;
        return o.longValue();
    }

    /**
     * No.435 Short.17 to Long
     * 
     */
    public static Long Short2Long(Short o) {
        if (o == null)
            return null;
        return Long.valueOf(o);
    }

    /**
     * No.436 Short.18 to Object
     * 
     */
    public static Object Short2Object(Short o) {
        return o;
    }

    /**
     * No.437 Short.19 to short
     * 
     */
    public static short Short2short(Short o) {
        if (o == null)
            return (short) 0;
        return o.shortValue();
    }

    /**
     * No.438 Short.20 to Short
     * 
     */
    public static Short Short2Short(Short o) {
        return o;
    }

    /**
     * No.439 Short.21 to String
     * 
     */
    public static String Short2String(Short o) {
        if (o == null)
            return null;
        return o.toString();
    }

    /**
     * No.440 Short.22 to List
     * 
     */
    public static List Short2List(Short o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        list.add(o);
        return list;
    }

    /**
     * No.441 String.1 to boolean
     * 
     */
    public static boolean String2boolean(String o) {
        if (o == null)
            return false;
        if (o.equalsIgnoreCase("true") || o.equalsIgnoreCase("false")) //$NON-NLS-1$ //$NON-NLS-2$
            return Boolean.valueOf(o);
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.442 String.2 to Boolean
     * 
     */
    public static Boolean String2Boolean(String o) {
        if (o == null)
            return Boolean.FALSE;
        if (o.equalsIgnoreCase("true") || o.equalsIgnoreCase("false")) //$NON-NLS-1$ //$NON-NLS-2$
            return Boolean.valueOf(o);
        throw ConvertTypeIllegalArgumentException.forInputArgument(o);
    }

    /**
     * No.443 String.3 to byte
     * 
     */
    public static byte String2byte(String o) {
        if (o == null)
            return (byte) 0;
        return Byte.parseByte(o);
    }

    /**
     * No.444 String.4 to Byte
     * 
     */
    public static Byte String2Byte(String o) {
        if (o == null)
            return null;
        return Byte.valueOf(o);
    }

    /**
     * No.445 String.5 to byte[]
     * 
     */
    public static byte[] String2byteArray(String o) {
        if (o == null)
            return null;
        return new byte[] { Byte.parseByte(o) };
    }

    /**
     * No.446 String.6 to char
     * 
     */
    public static char String2char(String o) {
        if (o == null)
            return (char) 0;
        if (o.length() > 1)
            throw ConvertTypeIllegalArgumentException.forInputArgument(o);
        return o.charAt(0);
    }

    /**
     * No.447 String.7 to Character
     * 
     */
    public static Character String2Character(String o) {
        if (o == null)
            return null;
        if (o.length() > 1)
            throw ConvertTypeIllegalArgumentException.forInputArgument(o);
        return Character.valueOf(o.charAt(0));
    }

    /**
     * No.448 String.8 to Date
     * 
     */
    public static Date String2Date(String o) {
        if (o == null)
            return null;
        DateFormat d = DateFormat.getDateInstance();
        try {
            return d.parse(o);
        } catch (ParseException e) {
            throw ConvertTypeIllegalArgumentException.forInputArgument(o);
        }
    }

    public static Date String2Date(String o, String pattern) {
        if (o == null)
            return null;
        return TalendDate.parseDate(pattern, o);
    }

    /**
     * No.449 String.9 to double
     * 
     */
    public static double String2double(String o) {
        if (o == null)
            return (double) 0;
        return Double.parseDouble(o);
    }

    /**
     * No.450 String.10 to Double
     * 
     */
    public static Double String2Double(String o) {
        if (o == null)
            return null;
        return Double.valueOf(o);
    }

    /**
     * No.451 String.11 to float
     * 
     */
    public static float String2float(String o) {
        if (o == null)
            return 0f;
        return Float.parseFloat(o);
    }

    /**
     * No.452 String.12 to Float
     * 
     */
    public static Float String2Float(String o) {
        if (o == null)
            return null;
        return Float.valueOf(o);
    }

    /**
     * No.453 String.13 to BigDecimal
     * 
     */
    public static BigDecimal String2BigDecimal(String o) {
        if (o == null)
            return null;
        return new BigDecimal(o);
    }

    /**
     * No.454 String.14 to int
     * 
     */
    public static int String2int(String o) {
        if (o == null)
            return 0;
        return Integer.parseInt(o);
    }

    /**
     * No.455 String.15 to Integer
     * 
     */
    public static Integer String2Integer(String o) {
        if (o == null)
            return null;
        return Integer.valueOf(o);
    }

    /**
     * No.456 String.16 to long
     * 
     */
    public static long String2long(String o) {
        if (o == null)
            return 0L;
        return Long.parseLong(o);

    }

    /**
     * No.457 String.17 to Long
     * 
     */
    public static Long String2Long(String o) {
        if (o == null)
            return null;
        return Long.valueOf(o);
    }

    /**
     * No.458 String.18 to Object
     * 
     */
    public static Object String2Object(String o) {

        return o;

    }

    /**
     * No.459 String.19 to short
     * 
     */
    public static short String2short(String o) {
        if (o == null)
            return (short) 0;
        return Short.parseShort(o);
    }

    /**
     * No.460 String.20 to Short
     * 
     */
    public static Short String2Short(String o) {
        if (o == null)
            return null;
        return Short.valueOf(o);
    }

    /**
     * No.461 String.21 to String
     * 
     */
    public static String String2String(String o) {
        return o;

    }

    /**
     * No.462 String.22 to List
     * 
     */
    public static List String2List(String o) {
        if (o == null)
            return null;
        List list = new ArrayList();
        list.add(o);
        return list;

    }

    /**
     * No.463 List.1 to boolean
     * 
     */
    public static boolean List2boolean(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to boolean  "); //$NON-NLS-1$
    }

    /**
     * No.464 List.2 to Boolean
     * 
     */
    public static Boolean List2Boolean(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to Boolean  "); //$NON-NLS-1$
    }

    /**
     * No.465 List.3 to byte
     * 
     */
    public static byte List2byte(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to byte  "); //$NON-NLS-1$
    }

    /**
     * No.466 List.4 to Byte
     * 
     */
    public static Byte List2Byte(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to Byte  "); //$NON-NLS-1$
    }

    /**
     * No.467 List.5 to byte[]
     * 
     */
    public static byte[] List2byteArray(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to byte[]  "); //$NON-NLS-1$
    }

    /**
     * No.468 List.6 to char
     * 
     */
    public static char List2char(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to char  "); //$NON-NLS-1$
    }

    /**
     * No.469 List.7 to Character
     * 
     */
    public static Character List2Character(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to Character  "); //$NON-NLS-1$
    }

    /**
     * No.470 List.8 to Date
     * 
     */
    public static Date List2Date(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to Date  "); //$NON-NLS-1$
    }

    /**
     * No.471 List.9 to double
     * 
     */
    public static double List2double(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to double  "); //$NON-NLS-1$
    }

    /**
     * No.472 List.10 to Double
     * 
     */
    public static Double List2Double(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to Double  "); //$NON-NLS-1$
    }

    /**
     * No.473 List.11 to float
     * 
     */
    public static float List2float(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to float  "); //$NON-NLS-1$
    }

    /**
     * No.474 List.12 to Float
     * 
     */
    public static Float List2Float(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to Float  "); //$NON-NLS-1$
    }

    /**
     * No.475 List.13 to BigDecimal
     * 
     */
    public static BigDecimal List2BigDecimal(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to BigDecimal  "); //$NON-NLS-1$
    }

    /**
     * No.476 List.14 to int
     * 
     */
    public static int List2int(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to int  "); //$NON-NLS-1$
    }

    /**
     * No.477 List.15 to Integer
     * 
     */
    public static Integer List2Integer(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to Integer  "); //$NON-NLS-1$
    }

    /**
     * No.478 List.16 to long
     * 
     */
    public static long List2long(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to long  "); //$NON-NLS-1$
    }

    /**
     * No.479 List.17 to Long
     * 
     */
    public static Long List2Long(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to Long  "); //$NON-NLS-1$
    }

    /**
     * No.480 List.18 to Object
     * 
     */
    public static Object List2Object(List o) {
        return o;
    }

    /**
     * No.481 List.19 to short
     * 
     */
    public static short List2short(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to short  "); //$NON-NLS-1$
    }

    /**
     * No.482 List.20 to Short
     * 
     */
    public static Short List2Short(List o) {
        throw new ConvertTypeNotSupportException("Can't support convert List to Short  "); //$NON-NLS-1$
    }

    /**
     * No.483 List.21 to String
     * 
     */
    public static String List2String(List o) {
        if (o == null)
            return null;
        return o.toString();
    }

    /**
     * No.484 List.22 to List
     * 
     */
    public static List List2List(List o) {
        return o;
    }

}
