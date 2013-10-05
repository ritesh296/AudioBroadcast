package com.ccleeric.audiobroadcast;

/**
 * Created by ccleeric on 13/9/26.
 */
public interface Subject {
    public void attachObserver(Observer observer);
    public void detachObserver(Observer observer);
    public void notifyObserver(int action, Object args);
}
