package TransfereCC;

import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

import static TransfereCC.CongestionControl.*;

public class TimeoutManager {
    SenderSide connection;
    Timer timer;
    boolean active;

    TimeoutManager(SenderSide connection, int milliseconds) {
        timer = new Timer();
        this.connection = connection;
        timer.schedule(new ResendSegment(), milliseconds);
        active = true;
    }

    void cancelTimer(){
        timer.cancel();
        active = false;
    }

    class ResendSegment extends TimerTask {
        public void run() {
            if(!active) return;
            System.out.println("TIMEOUT - Voltar a enviar pacote -" + LocalTime.now());
            connection.l.lock();
            connection.reSend(0);
            recalculateWindowSize(connection.st, TIMEOUT);
            connection.resetTimer();
            connection.l.unlock();
            timer.cancel(); //Terminate the timer thread
        }
    }

}
