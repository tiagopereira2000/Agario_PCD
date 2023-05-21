package game;
import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WinnerCounter implements Serializable {
    private int winnersLeft = Game.NUM_FINISHED_PLAYERS_TO_END_GAME;
    private Game game;
//    private Lock lock = new ReentrantLock();

    public WinnerCounter(Game game) {
        this.game = game;
    }

    public synchronized void decrementar(){
        winnersLeft--;
        if(winnersLeft == 0) notifyAll();
    }
    public synchronized void await() throws InterruptedException {
        while (winnersLeft > 0){
            wait();
        }
    }

}
