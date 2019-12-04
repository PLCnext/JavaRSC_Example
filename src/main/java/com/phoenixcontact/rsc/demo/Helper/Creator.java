//  
// Copyright (c) Phoenix Contact GmbH & Co. KG. All rights reserved.  
// Licensed under the MIT. See LICENSE file in the project root for full license information.
// SPDX-License-Identifier:     MIT
// 
package com.phoenixcontact.rsc.demo.Helper;

import java.util.function.Supplier;


/**
 * A supplier that is allowed to throw
 * @author andreniggemann
 *
 * @param <T>
 * The Return type when the creator is executed
 */
public interface Creator<T> {

    T run() throws Exception;

    /**
     * Turn to a supplier by ignoring exceptions
     * @return
     * Returns what ever the lambda returns or null if it threw.
     */
    default Supplier<T> silence(){
        return () -> {
            try{
                return this.run();
            }
            catch(Exception ex){

            }
            return null;
        };
    }
}
