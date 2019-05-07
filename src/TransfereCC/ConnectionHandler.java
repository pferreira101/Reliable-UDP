package TransfereCC;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static TransfereCC.ConnectionControl.*;
import static TransfereCC.ErrorControl.*;

class ConnectionHandler implements Runnable{
    TreeSet<MySegment> segmentsToProcess;
    ReentrantLock l;
    Condition waitPacketCondition;
    StateTable st;
    AgenteUDP msg_sender;

    ConnectionHandler(){
        l = new ReentrantLock(true);
        waitPacketCondition = l.newCondition();
    }

    /**
     *
     * @param received - received segment
     * @return true - if all buffered segments are ready to be processed
     * @return false - if a segment is missing or if a bit flip occurred in the received segment
     */
    boolean addSegmentToProcess(MySegment received) {
        // check integrity
        boolean isOk = verificaChecksum(received);
        if(!isOk) {
            msg_sender.requestRepeat(this.st,received.seq_number-1);
            return false;
        }
        l.lock();
        // error free segments will be buffered
        this.segmentsToProcess.add(received);
        l.unlock();

        if(isACK(received)) return true;

        boolean inOrder = isInOrder(st,received);

        if(!inOrder) msg_sender.requestRepeat(this.st, this.st.last_ack_value);

        return inOrder;
    }

    void waitSegment() {
        l.lock();
        while (segmentsToProcess.size() == 0) {
            try {
                waitPacketCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        l.unlock();
    }





    MySegment getNextSegment(){
        MySegment to_return =  this.segmentsToProcess.pollFirst();
        return to_return;
    }

    public void run(){}
}
