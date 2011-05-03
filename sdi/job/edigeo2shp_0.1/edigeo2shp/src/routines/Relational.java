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
package routines;

public class Relational {

    /**
     * ISNULL( ) Indicates when a variable is the null value.
     * 
     * {talendTypes} boolean | Boolean
     * 
     * {Category} Relational
     * 
     * {param} Object(null)
     * 
     * {example} ISNULL(null)
     * 
     * 
     */
    public static boolean ISNULL(Object variable) {
        return variable == null;
    }

    /**
     * NOT( ) Returns the complement of the logical value of an expression.
     * 
     * {talendTypes} boolean | Boolean
     * 
     * {Category} Relational
     * 
     * {param} boolean(true)
     * 
     * {example} NOT(false)
     */
    public static boolean NOT(boolean expression) {
        return !expression;
    }
}
