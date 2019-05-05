package TransfereCC;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ConnectionHandler implements Runnable{
    private LinkedList<MySegment> segmentsToProcess;
    Lock l;
    Condition waitPacketCondition;

    ConnectionHandler(){
        this.segmentsToProcess = new LinkedList<>();
        l = new ReentrantLock();
        waitPacketCondition = l.newCondition();
    }

    void addSegmentToProcess(MySegment to_process) {
        l.lock();
        this.segmentsToProcess.add(to_process);
        l.unlock();
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
