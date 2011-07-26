package extractorapp.ws.extractor;

/**
 * User: jeichar
 * Date: Aug 25, 2010
 * Time: 2:19:13 PM
 */
public class OversizedCoverageRequestException extends RuntimeException{
    public OversizedCoverageRequestException(String name) {
        super("La zone demandée est trop grande pour la couche "+name);
    }
}
