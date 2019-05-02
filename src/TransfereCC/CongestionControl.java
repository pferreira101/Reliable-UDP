package TransfereCC;

public class CongestionControl {

    private static final int MSS = 1500;

    private int cong_window;
    private int lst_byte_sent; // é preciso usar?
    private int lst_byte_acked; // é preciso usar?
    private int threshold;
    private int round;

    // semelhante a TCP Reno

    public CongestionControl(){
        this.cong_window = MSS;
        this.lst_byte_acked = 0;
        this.lst_byte_sent = 0;
        this.threshold = 5 * MSS; // valor ao calhas, escolher um melhor
        this.round = 0;
    }

    public void updateSent(int size){
        this.lst_byte_sent += size;
    }

    public void updateAcked(int size){
        this.lst_byte_acked += size;
    }

    public void increaseWindow() {

        round++;
        // slow start
        if (cong_window < threshold) {
            cong_window += MSS;
        }

        // congestion avoidance
        else if (cong_window >= threshold)
            cong_window = cong_window + MSS * (MSS / cong_window);
    }

    // type 0: se houver timeout, type 1: se houver 3 duplicados
    public void decreaseWindow(int type) {
        round++;
        switch(type) {

            // timeout
            case 0: threshold = cong_window/2;
            cong_window = MSS;
            break;

            // fast recovery
            case 1: threshold = cong_window/2;
            cong_window = threshold;
            break;
        }
    }
}