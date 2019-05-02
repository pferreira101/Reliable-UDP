package TransfereCC;

import java.util.*;

class ConnectionHandler {
    private List<MySegment> packetToProcess;

    ConnectionHandler(){
        this.packetToProcess = new LinkedList<>();
    }

    void addPacketToProcess(MySegment to_process) {
        this.packetToProcess.add(to_process);
    }
}
