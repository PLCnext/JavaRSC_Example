//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.View;

import com.phoenixcontact.arp.system.rsc.ServiceManager;
import com.phoenixcontact.rsc.demo.Helper.CoreCommunication;


/**
 * 
 * @author andreniggemann
 * 
 * Interface every TabController must implement to be loaded automatically with the loadTab method
 * in the MainViewController
 *
 */
public interface TabController {
    /**
     * Supplies necessary objects, is called during startup by the MainViewController
     * @param sm 
     * The service manager interface can be used to get instances of the service interfaces.
     * 
     * @param communication
     * Shared Communication Infrastructure. Can be used to listen to or fire events, 
     * which other tabs may listen to
     * 
     * @throws Exception
     * Might trow. Then the tab will not be displayed and the user will be notified that we 
     * failed to load this tab.
     */
    void init(ServiceManager sm, CoreCommunication communication) throws Exception;

    /**
     * Does necessary clean up. Will be called, if the application is closed or
     * the application is asked to switch back to the login screen.
     * 
     * @throws Exception
     * Exceptions may be thrown, but will not be handled. The application will close anyway.
     */
    void clean() throws Exception;
}
