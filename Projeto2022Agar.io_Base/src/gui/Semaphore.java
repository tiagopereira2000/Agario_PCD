package gui;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Semaphore {
    int permits;
    Lock redLightLocker = new ReentrantLock();
    Condition wait4permits = redLightLocker.newCondition();

    public Semaphore(int permits){
        this.permits = permits;
    }
    public Semaphore(){
        permits=0; //esperar primeiro cliente para soltar permissoes
    }

    //blocked until permits != 0
    public void acquire(){
        redLightLocker.lock();
        try{
            while (permits==0){
                wait4permits.await();
            }
            permits--;
        }catch (InterruptedException e){
            //interrupt causes exception every time
        }
        redLightLocker.unlock();
    }

    public void release(){
        redLightLocker.lock();
        permits++;
        redLightLocker.unlock();
        wait4permits.notifyAll();
    }
}
