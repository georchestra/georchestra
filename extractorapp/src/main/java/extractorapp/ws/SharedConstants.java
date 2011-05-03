package extractorapp.ws;

/**
 * Class for collecting Constants common to several packages
 * 
 * @author jeichar
 */
public final class SharedConstants {
    /**
     * If the system is in production this variable should be true.
     */
    public static boolean inProduction() {
        final String key="geobretagne_production";
        return Boolean.parseBoolean(System.getenv(key)) || Boolean.getBoolean(key); 
    }
}
