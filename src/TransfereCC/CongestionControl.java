package TransfereCC;

public class CongestionControl {

    static final int MSS = 1500;

    // eventos
    static final int NEWACK = 0;
    static final int TIMEOUT = 1;
    static final int ACKDUP = 2;
    static final int ACK3DUP = 3;

    // estados
    static final int SS = 4;
    static final int CA = 5;
    static final int FR = 6;

/*
    private int cong_window;
    private int round;

    // semelhante a TCP Reno

    public CongestionControl(){
        this.cong_window = MSS;
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
*/

    public void updateAcked(StateTable st){
        st.last_ack_value++;
    }

    public void increaseWindow(StateTable st){
        if (st.windowSize < st.threshold) st.windowSize++;

        // congestion avoidance
        else {
            st.congestion_state = CA;
            st.windowSize = st.windowSize + (1 / st.windowSize);
        }
    }

    public void increaseCongestedWindow(StateTable st){
            st.windowSize = st.windowSize + (1 / st.windowSize);
    }

    public void resetACKDup(int ack_number , StateTable st){

    }

    public void increaseACKDup(int ack_number , StateTable st){

    }

    private void processSSState(int ack_number , StateTable st, int type){
        switch(type) {
            case NEWACK:
                increaseWindow(st);
                updateAcked(st);
                break;

            case TIMEOUT:
                st.threshold = st.windowSize/2;
                st.windowSize = 1;
                break;

            case ACKDUP:
                increaseACKDup(ack_number, st);
                break;
        }
    }

    private void processCAState(int ack_number , StateTable st, int type){
        switch (type){
            case NEWACK:
                increaseCongestedWindow(st);
                break;

            case ACKDUP:
                increaseACKDup(ack_number, st);
                break;

            case TIMEOUT:
                st.threshold = st.windowSize/2;
                st.windowSize = 1;
                st.congestion_state = SS;
                break;

            case ACK3DUP:
                st.threshold = st.windowSize/2;
                st.windowSize = st.threshold+3;
                break;
        }
    }

    public void processFRState(int ack_number , StateTable st, int type){
        switch (type){
            case NEWACK:
                st.windowSize = st.threshold;
                resetACKDup(ack_number, st);
                st.congestion_state = CA;
                break;

            case ACKDUP:
                st.windowSize++;
                break;

            case TIMEOUT:
                st.threshold = st.windowSize/2;
                st.windowSize = 1;
                resetACKDup(ack_number, st);
                st.congestion_state = SS;
                break;
        }
    }

    // retornar algum tipo de ação a fazer? "transmit new segment as allowed"
    // calcular o tipo antes da chamada da função ou então calcular dentro da funçao?
    public void processReceivedACK(int ack_number /*em vez do segmento completo?? */, StateTable st, int type){
        switch (st.congestion_state){
            case SS:
                processSSState(ack_number, st, type);
                break;

            case CA:
                processCAState(ack_number, st, type);
                break;

            case FR:
                processFRState(ack_number, st, type);
                break;
        }
    }

}