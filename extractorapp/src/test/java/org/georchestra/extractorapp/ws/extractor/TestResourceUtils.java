package org.georchestra.extractorapp.ws.extractor;

import com.google.common.io.Resources;

import java.io.IOException;

/**
 * Some useful method for loading resources for tests.
 *
 * @author Jesse on 5/5/2014.
 */
public class TestResourceUtils {
    /**
     * Load a file into memory as a string.
     *
     * @param testClass the class to use as the reference class for loading the resource
     * @param resourceName name of the resource, If the name starts with / then the resources is relative to the root of the classpath
     *                     otherwise the resource is relative to the package of the testclass
     */
    public static String getResourceAsString(Class<?> testClass, String resourceName) throws IOException {
        return new String(getResourceAsBytes(testClass, resourceName), "UTF-8");
    }
    /**
     * Load a file into memory as a byte array.
     *
     * @param testClass the class to use as the reference class for loading the resource
     * @param resourceName name of the resource, If the name starts with / then the resources is relative to the root of the classpath
     *                     otherwise the resource is relative to the package of the testclass
     */
    public static byte[] getResourceAsBytes(Class<?> testClass, String resourceName) throws IOException {
        return Resources.toByteArray(testClass.getResource(resourceName));
    }
}
