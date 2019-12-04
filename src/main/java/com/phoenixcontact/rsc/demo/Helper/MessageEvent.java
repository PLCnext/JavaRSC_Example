//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.Helper;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Basic Eventtype for passing strings the ui.
 * 
 * @author andreniggemann
 *
 */
public class MessageEvent extends Event {
	private static final long serialVersionUID = 7383871912770722549L;
	private String message;
    public MessageEvent(String message) {
        super(EventType.ROOT);
        this.message = message;

    }
    public String getMessage(){
        return message;
    }
}
