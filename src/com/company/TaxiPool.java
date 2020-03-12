package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaxiPool {

    private final List<Taxi> available = Collections.synchronizedList(new ArrayList<>());
    private final List<Taxi> inUse = Collections.synchronizedList(new ArrayList<>());

    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicBoolean waiting = new AtomicBoolean(false);

    public synchronized Taxi getTaxi(){
        if(!available.isEmpty()){
            Taxi taxi = available.remove(0);
            inUse.add(taxi);
            return taxi;
        }

        if(count.get() == Constants.NUMBER_OF_TAXI){
            this.waitingUntilTaxiAvailable();
            return getTaxi();
        }

        Taxi taxi = this.createTaxi();
        inUse.add(taxi);
        return taxi;
    }

    public synchronized void release(Taxi taxi){
        inUse.remove(taxi);
        available.add(taxi);
        System.out.println(taxi.getName() + " is free");
    }

    private Taxi createTaxi(){
        waiting(200);
        Taxi taxi = new Taxi("Taxi: " + count.incrementAndGet());
        System.out.println(taxi.getName() + " is created");
        return taxi;
    }

    private void waitingUntilTaxiAvailable(){
        if(waiting.get()){
            waiting.set(false);
            throw new TaxiNotFoundException("Taxi not available");
        }
        waiting.set(true);
        waiting(Constants.EXPIRED_TIME_IN_MILISECOND);
    }

    private void waiting(long numberOfSecond){
        try{
            TimeUnit.MILLISECONDS.sleep(numberOfSecond);
        }catch (InterruptedException e){
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
