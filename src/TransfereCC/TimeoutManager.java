package TransfereCC;

import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class TimeoutManager {
    SenderSide connection;
    Timer timer;

    TimeoutManager(SenderSide connection, int milliseconds) {
        timer = new Timer();
        this.connection = connection;
        timer.schedule(new ResendSegment(), milliseconds);
    }

    void cancelTimer(){
        timer.cancel();
    }

    class ResendSegment extends TimerTask {
        public void run() {
            System.out.println("TIMEOUT - Voltar a enviar pacote -" + LocalTime.now());
            connection.l.lock();
            connection.reSend(0);
            connection.l.unlock();
            timer.cancel(); //Terminate the timer thread
        }
    }

}
