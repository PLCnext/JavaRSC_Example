//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.RscController;

import com.phoenixcontact.ade.commonremoting.CommonRemotingException;
import com.phoenixcontact.ade.commonremoting.utils.AdeObject;
import com.phoenixcontact.ade.commonremoting.utils.OutParam;
import com.phoenixcontact.arp.plc.gds.services.*;
import com.phoenixcontact.arp.system.rsc.ServiceManager;
import com.phoenixcontact.rsc.demo.Helper.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.math.BigInteger;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Background service for the GDS Tab
 * 
 * @author andreniggemann
 *
 */
public class GdsServices {

    private final CoreCommunication communication;
    //reference to the list displayed in the subscription table.
    private final ObservableList<SubscriptionView> gdsSubs;

    //The library services used by this class
    private final ISubscriptionService subService;
    private final IDataAccessService dataService;


    //private boolean needSubscribe = true;
    final BigInteger REFRESH_TIME = BigInteger.valueOf(100000);
    private ScheduledThreadPoolExecutor updater;
    private ScheduledFuture<?> updatetask;

    /*
     * The id of the current subscription. -1 if no subscription is held.
     * 
     * Notes on the Subscription Service Interface:
     * The lifecycle for a subscription looks as follows:
     * 
     * 	subId = subService.createSubscription(SubscriptionKind.DIRECT_READ);
     *  subService.add(subId, "uri of variable1");
     *  subservice.subscribe(subId, REFRESH_TIME);
     *  repeat 
     *  	//unsubscribe necessary to add new variable
     *  	subService.unsubscribe(subId);	
     *  	//change subscription
     *  	subService.add(subId, "uri of variable2");
     *  	or subService.remove(subId, "uri of varibable2");
     *  	//resubscribe again. Subscribing does not return an error, but will fail silently
 *  		subService.resubscribe(subId, REFRESH_TIME); 
     *  subService.unsubscribe(subId);
     *  subService.deleteSubscription(subId);
     *  subId = -1;
     * 
     */
    private long subId;

    public GdsServices(ServiceManager sm, CoreCommunication communication, ObservableList<SubscriptionView> gdsSubs) throws CommonRemotingException {
        this.communication = communication;
        communication.addStopListener(this::removeAllSubscriptions);
        communication.addStartListener(this::newSubscription);
        this.gdsSubs = gdsSubs;
        subService = sm.getService(ISubscriptionService.class);
        dataService = sm.getService(IDataAccessService.class);
        subId = -1;
        newSubscription();
        updater = new ScheduledThreadPoolExecutor(1);
    }


    public synchronized void clean() {
        updatetask.cancel(false);
        updater.shutdown();
        ((Action) () -> subService.unsubscribe(subId)).silence().run();
        ((Action) () -> subService.deleteSubscription(subId)).silence().run();
    }


    //example uri : Arp.Plc.Eclr/MainInstance.TempValue
    public synchronized void subscribe(String uri){

        try{
            if(gdsSubs.stream().anyMatch(x -> x.getUri().equals(uri))){
                communication.error("Subscription to that uri already exists");
                return;
            }

            DataAccessError result;
            if(gdsSubs.size() == 0){
                result = mergeResults(
                        () -> subService.addVariable(subId, uri),
                        () -> subService.subscribe(subId, REFRESH_TIME)
                );
            }
            else {
                result = mergeResults(
                        () -> subService.unsubscribe(subId),
                        () -> subService.addVariable(subId, uri),
                        () -> subService.resubscribe(subId, REFRESH_TIME)
                );
            }

            switch (result){
                case NONE:

                    //Read the uri for the first time.
                    var item = dataService.readSingle(uri);
                    if(item.getError().getValue() == DataAccessError.NONE.getValue()){
                        var value = item.getValue();
                        var view = new SubscriptionView(uri, value);
                        gdsSubs.add(view);
                        if(updatetask == null || updatetask.isCancelled()){
                            updatetask = updater.scheduleAtFixedRate(this::updateSubs, 50, 100, TimeUnit.MILLISECONDS);
                        }
                        communication.message("Successfully subscribed to " + uri);
                    }
                    else{
                        communication.error("Test reading the uri failed");
                    }

                    break;
                case NOT_EXISTS:
                    communication.error("The requested uri does not exist");
                    break;
                default:
                    communication.error("Subscribing to " + uri +" didn't work");
            }

        }
        catch(Exception ex) {
            communication.error("Subscribing to " + uri + "failed");
        }


    }

    /**
     * Execute Commands until one throws an Exception or returns an Error Code.
     * @param args
     * Commands to execute.
     * @return
     * Returns the first Error Code or None.
     * @throws Exception
     * If one of the arguments throws it will not be caught.
     */
    @SafeVarargs
	private static DataAccessError mergeResults(Creator<DataAccessError> ...args ) throws Exception{
        final var noneValue = DataAccessError.NONE.getValue();
        for(var creator : args){
            var result  = creator.run();
            if (result.getValue() != noneValue)
                return result;
        }

        return DataAccessError.NONE;
    }

    //Periodically reads the Values of the subscriptions and updates the view
    private synchronized void updateSubs() {

        try{
            var outInfos = new OutParam<VariableInfo[]>();
            var outValues = new OutParam<AdeObject[]>();
            var result = mergeResults(
                    () -> subService.getVariableInfos(subId, outInfos),
                    () -> subService.readValues(subId, outValues));

            if (DataAccessError.NONE.equals(result)) {
                var infos = outInfos.getValue();
                var values = outValues.getValue();

                for (int i = 0; i < Math.min(infos.length, values.length); i++) {

                    var info = infos[i];
                    var value = values[i];

                    //Find the SubscriptionView with the corresponding uri and update it.
                    gdsSubs.stream()
                            .filter(view -> view.getUri().equals(info.getName()))
                            .findFirst()
                            .ifPresentOrElse(
                                view -> {
                                    view.setObject(value);
                                    Platform.runLater(() -> {
                                        var index = gdsSubs.indexOf(view);
                                        if(index >= 0)
                                            gdsSubs.set(index, view);
                                    }); },
                                () -> communication.error("No such uri in subscription list"));

                }
            }
        }
        catch (Exception ex){
            communication.error("Update failed");
        }

    }

    public synchronized void unsubscribe(SubscriptionView subscription){
        try {
            var result = mergeResults(
                    () -> subService.unsubscribe(subId),
                    () -> subService.removeVariable(subId, subscription.getUri()),
                    () -> subService.resubscribe(subId, REFRESH_TIME)
            );

            var resultValue = result.getValue();
            if(resultValue == DataAccessError.NONE.getValue()
                    || resultValue == DataAccessError.NOT_EXISTS.getValue()){
                communication.message("Successfully unsubscribed from " + subscription.getUri());
                gdsSubs.remove(subscription);
                if(gdsSubs.size() == 0)
                    //needSubscribe = true;
                    if(updatetask != null && !updatetask.isCancelled())
                        updatetask.cancel(false);


            }
            else
                communication.error("Unsubscribing from " + subscription.getUri() +" failed");

        }
        catch(Exception ex){
            communication.error("Unsubscribing from " + subscription.getUri() +" failed");
        }
    }



    public synchronized void write(SubscriptionView selected, String valueString){
        try {
            //Create the WriteItem which is passed to the DataAccessService
            var item = new WriteItem();
            item.setPortName(selected.getUri());

            //create the value for the writeItem, consisting of a type and value
            var obj = new AdeObject();
            //read the type from the SubscriptionView
            var type = selected.getType();
            obj.setCoreType(type);
            //parse the content of the valueString to match the CoreType
            var value = SubscriptionView.parseToCoreType(type, valueString);
            if(value == null){
                communication.error("Parsing the value to the variable type failed");
                return;
            }
            obj.setValue(value);
            item.setValue(obj);

            //Write to the via the DataAccessService
            var result = dataService.writeSingle(item);
            if(result.getValue() == DataAccessError.NONE.getValue())
                communication.message("Succesfully wrote to " + selected.getUri());

            else
                communication.error("Writing to " + selected.getUri() + " failed");

        }
        catch (Exception ex){
            communication.error("Writing to " + selected + " failed");
        }
    }

    public synchronized void removeAllSubscriptions(){
        try{
            subService.unsubscribe(subId);
            subService.deleteSubscription(subId);
            updatetask.cancel(true);
            subId = -1;
            gdsSubs.removeAll(gdsSubs);
        }
        catch(Exception ex){
            communication.error("Deleting all subscriptions failed");
        }

    }

    public void newSubscription(){
        try {
            if(subId != -1)
                subService.deleteSubscription(subId);
            subId = subService.createSubscription(SubscriptionKind.DIRECT_READ);

        }catch (Exception ex){

        }

    }


}
