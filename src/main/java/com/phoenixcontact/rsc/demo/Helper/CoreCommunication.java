//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.Helper;

import javafx.event.EventHandler;

import java.util.List;
import java.util.Vector;


/**
 * Shared communication infrastructure. 
 * Passed to tabs and controllers to allow them to display messages and share events
 * Initialized by the MainViewController
 * 
 * @author andreniggemann
 *
 */

public class CoreCommunication {
    private final EventHandler<MessageEvent> messageHandler;
    private final EventHandler<MessageEvent> errorHandler;
    private final EventHandler<MessageEvent> logHandler;
    private final Runnable backToLoginHandler;
    private final List<Action> stopListeners;
    private final List<Action> startListeners;

    public CoreCommunication(EventHandler<MessageEvent> messageHandler,EventHandler<MessageEvent> errorHandler, EventHandler<MessageEvent> logHandler, Runnable backToLoginHandler){
        this.messageHandler = messageHandler;
        this.errorHandler = errorHandler;
        this.logHandler = logHandler;
        this.backToLoginHandler = backToLoginHandler;
        stopListeners = new Vector<>();
        startListeners = new Vector<>();
    }

    /**
     * Add message only to log
     * @param message
     */
    public void log(String message){
        logHandler.handle(new MessageEvent(message));
    }

    /**
     * Display message to user, currently logs it as well
     * @param message
     */
    public void message(String message){
        messageHandler.handle(new MessageEvent(message));
    }
    
    /**
     * Display error message to user, currently logs it as well
     * @param message
     */
    public void error(String message){
        errorHandler.handle(new MessageEvent(message));
    }

    /**
     * Notify all listeners about start of plc
     */
    public void start(){
        synchronized (startListeners){
            for(var a : startListeners){
                a.silence().run();
            }
        }
    }

    /**
     * Notify all listeners about stop of plc
     */
    public void stop(){
        synchronized (stopListeners) {
            for (var a : stopListeners) {
                a.silence().run();
            }
        }
    }


    /**
     * go back to login screen
     */
    public void backToLogin() {
        backToLoginHandler.run();
    }

    /**
     * start listening to start event
     * @param a
     */
    public void addStartListener(Action a){

        synchronized (startListeners) {
            startListeners.add(a);
        }
    }

    /**
     * start listening to stop event
     * @param a
     */
    public void addStopListener(Action a){
        synchronized (stopListeners){
            stopListeners.add(a);
        }
    }



}
