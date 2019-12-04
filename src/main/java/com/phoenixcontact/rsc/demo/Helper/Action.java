//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.Helper;



/**
 *  A runnable that is able to throw.
 * @author andreniggemann
 *
 */
public interface Action
{
    void run() throws Exception;

    /**
     * Creates a runnable by ignoring exceptions
     * 
     * @return
     * A lambda that tries to execute the action.
     * If an exception occures it will be caught and ignored.
     */
    default Runnable silence(){
        return () -> {
            try{
                this.run();
            }
            catch(Exception ex){

            }
        };
    }
}
