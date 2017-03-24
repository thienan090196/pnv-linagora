/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package com.linagora.pnv.jpa.quota.model;

/*
Question 1 :

Add the JPA table definition. You can have a look in org.apache.james.mailbox.jpa.mail.model.JPAUserFlag
 */
public class MaxUserMessageCount {
    /*
    Question 2 :

    Add quotaRoot as a primary key
     */
    private String quotaRoot;

    /*
    Question 3 :

    Add value as a column
     */
    private long value;

    public MaxUserMessageCount(String quotaRoot, long value) {
        this.quotaRoot = quotaRoot;
        this.value = value;
    }

    public MaxUserMessageCount() {
    }

    public long getValue() {
        return value;
    }
}
