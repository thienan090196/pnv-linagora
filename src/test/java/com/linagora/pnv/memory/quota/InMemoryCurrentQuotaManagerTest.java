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

package com.linagora.pnv.memory.quota;

import static org.assertj.core.api.Assertions.assertThat;


import org.junit.Before;
import org.junit.Test;

import com.linagora.pnv.QuotaRoot;
import com.linagora.pnv.QuotaRootImpl;

public class InMemoryCurrentQuotaManagerTest {

    public static final QuotaRoot QUOTA_ROOT = QuotaRootImpl.quotaRoot("benwa");

    private InMemoryCurrentQuotaManager testee;

    @Before
    public void setUp() throws Exception {
        testee = new InMemoryCurrentQuotaManager();
    }

    @Test
    public void getCurrentStorageShouldReturnZeroByDefault() throws Exception {
        assertThat(testee.getCurrentStorage(QUOTA_ROOT)).isEqualTo(0);
    }

    @Test
    public void increaseShouldWork() throws Exception {
        testee.increase(QUOTA_ROOT, 10, 100);

        assertThat(testee.getCurrentMessageCount(QUOTA_ROOT)).isEqualTo(10);
        assertThat(testee.getCurrentStorage(QUOTA_ROOT)).isEqualTo(100);
    }

    /*
    Add a test showing that decrease works
     */

    @Test(expected = IllegalArgumentException.class)
    public void increaseShouldThrowOnZeroCount() throws Exception {
        testee.increase(QUOTA_ROOT, 0, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void increaseShouldThrowOnNegativeCount() throws Exception {
        testee.increase(QUOTA_ROOT, -1, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void increaseShouldThrowOnZeroSize() throws Exception {
        testee.increase(QUOTA_ROOT, 5, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void increaseShouldThrowOnNegativeSize() throws Exception {
        testee.increase(QUOTA_ROOT, 5, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decreaseShouldThrowOnZeroCount() throws Exception {
        testee.decrease(QUOTA_ROOT, 0, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decreaseShouldThrowOnNegativeCount() throws Exception {
        testee.decrease(QUOTA_ROOT, -1, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decreaseShouldThrowOnZeroSize() throws Exception {
        testee.decrease(QUOTA_ROOT, 5, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decreaseShouldThrowOnNegativeSize() throws Exception {
        testee.decrease(QUOTA_ROOT, 5, -1);
    }

}