package routines.system;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface IPersistableLookupRow<R> {

    public void writeKeysData(ObjectOutputStream out);

    public void readKeysData(ObjectInputStream in);

    public void writeValuesData(DataOutputStream dataOut, ObjectOutputStream objectOut);

    public void readValuesData(DataInputStream dataIn, ObjectInputStream objectIn);

    public void copyDataTo(R other);

    public void copyKeysDataTo(R other);

}
