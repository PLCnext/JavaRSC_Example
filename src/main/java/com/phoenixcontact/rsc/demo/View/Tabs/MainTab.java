//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.View.Tabs;

import com.phoenixcontact.arp.plc.domain.services.PlcState;
import com.phoenixcontact.arp.system.rsc.ServiceManager;
import com.phoenixcontact.rsc.demo.Helper.CoreCommunication;
import com.phoenixcontact.rsc.demo.RscController.MainService;
import com.phoenixcontact.rsc.demo.View.TabController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author andreniggemann
 * 
 * 
 * The view controller for the main tab loaded from MainTab.fxml
 * 
 *
 */

public class MainTab implements TabController {

    @FXML
    Label stateLabel;

    CoreCommunication communication;
    MainService controller;

    private ScheduledThreadPoolExecutor updater;
    private ScheduledFuture updaterCallback;
    private boolean isRunning;

    @Override
    public void init(ServiceManager sm, CoreCommunication communication) throws Exception {
        controller = new MainService(sm, communication);
        this.communication = communication;
        updater = new ScheduledThreadPoolExecutor(1);
        updaterCallback = updater.scheduleAtFixedRate(
                this::refresh, 20, 500, TimeUnit.MILLISECONDS);
    }


    @Override
    public void clean() {
        updaterCallback.cancel(false);
        updater.shutdown();
    }

    //Refresh the state label with the current state of the plc
    private  void refresh() {
        Iterator<PlcState> states;

        states = controller.getStatus().getFlags().iterator();

        var firstState = states.next();
        var newRunningState = firstState.getValue() == PlcState.RUNNING.getValue();
        if (newRunningState && !isRunning)
            communication.start();
        else if (!newRunningState && isRunning)
            communication.stop();
        isRunning = newRunningState;

        var sb = new StringBuilder();
        sb.append(firstState.toString());
        states.forEachRemaining(flag -> sb.append(", " + flag.toString()));
        Platform.runLater(() ->
            stateLabel.setText(sb.toString()));
    }

    @FXML
    private void start(MouseEvent event){
        controller.start();

    }

    @FXML
    private void stop(MouseEvent event){
        controller.stop();
    }

    @FXML
    private void restart(MouseEvent event){
        controller.restart();
    }

    @FXML
    private void startFWUpdate(MouseEvent event){
        controller.startFWUpdate();
    }

    @FXML
    private void backToLogin(MouseEvent event){
        communication.backToLogin();
    }



}
