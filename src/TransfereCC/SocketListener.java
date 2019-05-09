package TransfereCC;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static TransfereCC.ConnectionControl.*;
import static TransfereCC.MySegment.*;

public class SocketListener implements Runnable  {
    DatagramSocket socket;
    AgenteUDP connectionManager;

    public SocketListener(AgenteUDP connectionManager){
        this.connectionManager = connectionManager;
        this.socket = connectionManager.serverSocket;
    }

    public void run(){
        System.out.println("Listening socket");

        while(true){
            MySegment to_process;
            byte[] receiveData = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try{
                socket.receive(receivePacket);
                to_process = fromByteArray(receivePacket.getData());

                InetAddress ip = receivePacket.getAddress();
                int port  = receivePacket.getPort();

                setPacketAtConnection(new SimpleEntry<InetAddress,Integer>(ip,port), to_process);
            }
            catch(ClassNotFoundException e){
                System.out.println("Can't convert UDP content to MySegment");
            }
            catch(IOException e){
                System.out.println("Error ocurred during receive method");
            }
        }
    }

    private void setPacketAtConnection(SimpleEntry key, MySegment to_process) {
        ConnectionHandler connection = connectionManager.connections.get(key);
        if(connection != null){
            System.out.println("********Novo pacote recebido********");
            boolean ready_to_process = connection.addSegmentToProcess(to_process);
            if(ready_to_process) wakeUpConnectionHandler(connection);
            deferLockAttempt(connection);
        }
        else //if no connection matches key, new request was made
            if(isFileGetRequest(to_process))
                connectionManager.addSenderRoleConnection((InetAddress)key.getKey(), (Integer)key.getValue(),to_process, null);
            else if (isFilePutRequest(to_process))
                connectionManager.addReceiverRoleConnection((InetAddress)key.getKey(), (Integer)key.getValue(),to_process, null);
    }

    private void deferLockAttempt(ConnectionHandler connection) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }


    private void wakeUpConnectionHandler(ConnectionHandler connection){
        connection.l.lock();
        connection.readyToProcess = true;
        connection.waitPacketCondition.signal();
        connection.l.unlock();
    }



}
