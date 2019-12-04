//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.RscController;

import com.phoenixcontact.ade.commonremoting.CommonRemotingException;
import com.phoenixcontact.arp.device.iface.services.IDeviceControlService;
import com.phoenixcontact.arp.plc.domain.services.IPlcManagerService2;
import com.phoenixcontact.arp.plc.domain.services.PlcStartKind;
import com.phoenixcontact.arp.plc.domain.services.PlcState;
import com.phoenixcontact.arp.plc.domain.services.PlcStates;
import com.phoenixcontact.arp.system.rsc.ServiceManager;
import com.phoenixcontact.rsc.demo.Helper.CoreCommunication;

/**
 * The background service for the MainTab
 * 
 * @author andreniggemann
 *
 */
public class MainService {

    private final CoreCommunication communication;

    private final IPlcManagerService2 plcService;

    private final IDeviceControlService deviceControlService;

    public MainService(ServiceManager sm, CoreCommunication communication) throws CommonRemotingException {
        plcService = sm.getService(IPlcManagerService2.class);
        deviceControlService = sm.getService(IDeviceControlService.class);
        this.communication = communication;
    }

    /**
     * Reads the current state of the controller
     * @return
     */
    public synchronized PlcStates getStatus(){
        try {
            return plcService.getPlcState();
        }
        catch(Exception ex){
            communication.error("Retrieving status failed");
        }
        var emptyState = new PlcStates();
        emptyState.add(PlcState.NONE);
        return emptyState;
    }

    /**
     * loads the configuration and makes a cold start.
     */
    public synchronized void start() {
        try {
            plcService.reset(false);
            plcService.load(false);
            plcService.start(PlcStartKind.COLD, false);
            communication.message("Start issued");
        }
        catch(Exception ex){
            communication.error("Starting failed");
        }

    }

    /**
     * stops the program on the plc and unloads the configuration
     */
    public synchronized void stop() {
        try {
            plcService.stop(false);
            plcService.reset(false);
            communication.message("Stop issued");
        }
        catch(Exception ex){

            communication.error("Stopping failed");
        }
    }

    /**
     * reboots the controller.
     */
    public synchronized void restart() {
        try {
            deviceControlService.restartDevice();
            communication.message("Restart issued");
            communication.backToLogin();
        }
        catch(Exception ex){
            communication.error("Restarting failed");
        }
    }

    /**
     * starts the firmware update process.
     * Does only work, when a new firmware is present in /opt/plcnext
     */
    public synchronized void startFWUpdate() {
        try {
            deviceControlService.startFirmwareUpdate(0);
            communication.message("Started firmware update");
            communication.backToLogin();
        }
        catch (Exception ex){
            communication.error("Starting the firmware update failed");
        }
    }
}
