package TransfereCC;

public class FlowControl {
    private int buffer_size;
    private int lst_byte_read;
    private int lst_byte_rcvd;
    private int rcv_window;

    public FlowControl(){
        this.buffer_size = 8192;
        this.lst_byte_rcvd = 0;
        this.lst_byte_read = 0;
        this.rcv_window = this.buffer_size;
    }

    public void updateRcvd(int size){
        this.lst_byte_rcvd += size;
        this.rcv_window = buffer_size - (lst_byte_rcvd - lst_byte_read);
    }

    public void updateRead(int size){
        this.lst_byte_read += size;
        this.rcv_window = buffer_size - (lst_byte_rcvd - lst_byte_read);
    }

    public int getRcvWindow(){
        return this.rcv_window;
    }

}
