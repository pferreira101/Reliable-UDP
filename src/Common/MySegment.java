package Common;

import java.io.*;


public class MySegment implements Serializable {
    public int seq_number;
    public int ack_number;
    public byte flag;
    public byte[] checksum;
    public byte[] fileData;


    public void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public byte[] toByteArray() throws  IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(this);
        return bos.toByteArray();
    }

    public static MySegment fromByteArray(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = new ObjectInputStream(bis);
        return (MySegment) in.readObject();
    }

}

