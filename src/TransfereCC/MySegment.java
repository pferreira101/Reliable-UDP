package TransfereCC;

import java.io.*;

class MySegment implements Serializable {
    int seq_number;
    int ack_number;
    int max_window_size;
    byte flag;
    byte[] checksum;
    byte[] fileData;

    MySegment(){
        this.checksum = new byte[] {(byte)0,(byte)0};
    }

    void resetChecksum(){
        this.checksum = new byte[] {(byte)0,(byte)0};
    }

    void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }

    void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    byte[] toByteArray() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(this);
        } catch (IOException e) {
            System.out.println("Error serializing segment");
        }

        return bos.toByteArray();
    }

    static MySegment fromByteArray(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = new ObjectInputStream(bis);
        return (MySegment) in.readObject();
    }

    public byte[] getChecksum() {
        return this.checksum;
    }

    public void setMaxWindowSize(int max_window_size) {
        this.max_window_size = max_window_size;
    }
}

