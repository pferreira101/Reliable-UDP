package TransfereCC;

import java.io.*;

class MySegment implements Serializable {
    int seq_number;
    int ack_number;
    byte flag;
    private byte[] checksum;
    byte[] fileData;

    MySegment(){
        this.checksum = new byte[] {(byte)0,(byte)0};
    }

    void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }

    void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    byte[] toByteArray() throws  IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(this);
        return bos.toByteArray();
    }

    static MySegment fromByteArray(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = new ObjectInputStream(bis);
        return (MySegment) in.readObject();
    }

}

