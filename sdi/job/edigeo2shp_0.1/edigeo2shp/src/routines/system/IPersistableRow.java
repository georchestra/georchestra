package routines.system;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface IPersistableRow<R> {

    public void writeData(ObjectOutputStream out);

    public void readData(ObjectInputStream in);

}
