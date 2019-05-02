package TransfereCC;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class SocketListener implements Runnable  {
    Map<AbstractMap.SimpleEntry, ConnectionHandler> connections;
    DatagramSocket socket;

    public SocketListener(DatagramSocket socket, Map<AbstractMap.SimpleEntry, ConnectionHandler> connections){
        this.socket = socket;
        this.connections = connections;
    }

    public void run(){
        while(true){
            MySegment to_process;
            byte[] receiveData = new byte[4096];
            byte[] sendData;
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try{
                socket.receive(receivePacket);
                to_process = MySegment.fromByteArray(receivePacket.getData());
                InetAddress ip = receivePacket.getAddress();
                int port  = receivePacket.getPort();
                setPacketAtConnection(new SimpleEntry<InetAddress,Integer>(ip,port), to_process);
            }
            catch(IOException e){
                System.out.println("Error ocurred during receive method");
            }
            catch(ClassNotFoundException e){
                System.out.println("Can't convert to MySegment");
            }
        }
    }

    private void setPacketAtConnection(SimpleEntry key, MySegment to_process) {
        ConnectionHandler connection = connections.get(key);
        if(connection != null){
            connection.addPacketToProcess(to_process);
        }
        else System.out.println("Criar nova conexão");

    }

}
