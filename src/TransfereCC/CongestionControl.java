package TransfereCC;


public class CongestionControl {

    // eventos
    static final int NEWACK = 0;
    static final int TIMEOUT = 1;
    static final int ACKDUP = 2;
    static final int ACK3DUP = 3;

    // estados
    static final int SS = 4;
    static final int CA = 5;
    static final int FR = 6;

    static void recalculateWindowSize(StateTable st, int type){
        switch(st.congestion_state) {
            case SS:
                processSSState(st, type);
                break;

            case CA:
                processCAState(st, type);
                break;

            case FR:
                processFRState(st, type);
                break;

        }

        System.out.println("Novo tamanho da janela = " + st.windowSize + "( estado: " +st.congestion_state+" step: "+ st.windowSizeCAaux +")");
    }

    static private void increaseWindowSize(StateTable st){
        switch(st.congestion_state) {
            case SS:
                if (st.windowSize < st.threshold) st.windowSize++;
                else {
                    st.congestion_state = CA;
                    ++st.windowSizeCAaux;
                }
                break;

            case CA:
                ++st.windowSizeCAaux;
                if(st.windowSizeCAaux >= st.windowSize){
                    ++st.windowSize;
                    st.windowSizeCAaux = 0;
                }
                break;
        }
    }


    static private void processSSState(StateTable st, int type){
        switch(type) {
            case NEWACK:
                increaseWindowSize(st);
                break;

            case TIMEOUT:
                st.threshold = (int) st.windowSize/2;
                st.windowSize = 1;
                break;

            case ACK3DUP:
                st.threshold = (int) st.windowSize/2;
                st.windowSize = st.threshold+3;
                st.congestion_state = FR;
                break;

        }
    }

    static private void processCAState(StateTable st, int type){
        switch (type){
            case NEWACK:
                increaseWindowSize(st);
                break;

            case TIMEOUT:
                st.threshold = (int) st.windowSize/2;
                st.windowSize = 1;
                st.windowSizeCAaux = 0;
                st.congestion_state = SS;
                break;

            case ACK3DUP:
                st.threshold = (int) st.windowSize/2;
                st.windowSize = st.threshold+3;
                st.windowSizeCAaux = 0;
                st.congestion_state = FR;
                break;
        }
    }

    static private void processFRState(StateTable st, int type){
        switch (type){
            case NEWACK:
                st.windowSize = st.threshold;
                st.congestion_state = CA;
                break;

            case ACKDUP:
                st.windowSize++;
                break;

            case TIMEOUT:
                st.threshold = (int) st.windowSize/2;
                st.windowSize = 1;
                st.congestion_state = SS;
                break;
        }
    }
}