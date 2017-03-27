/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package com.linagora.pnv;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public abstract class GroupMembershipResolverTest {

    private GroupMembershipResolver groupMembershipResolver;

    protected abstract GroupMembershipResolver getGroupMembershipResolver();

    @Before
    public void setUp() {
       groupMembershipResolver = getGroupMembershipResolver();
    }

    @Test
    public void isMemberShouldReturnFalseWhenEmptyResolver() throws Exception {
        //When
        boolean actual = groupMembershipResolver.isMember("user", "group");
        //Then
        assertThat(actual).isFalse();
    }

    @Test
    public void isMemberShouldReturnTrueWhenTheSearchedMembershipIsPresent() throws Exception {
        //Given
        groupMembershipResolver.addMembership("group", "user");
        //When
        boolean actual = groupMembershipResolver.isMember("user", "group");
        //Then
        assertThat(actual).isTrue();
    }

    @Test
    public void addMembershipShouldAddAMembershipWhenNonNullUser() throws Exception {
        //When
        groupMembershipResolver.addMembership("group", "user");
        boolean actual = groupMembershipResolver.isMember("user", "group");
        //Then
        assertThat(actual).isTrue();
    }

    @Test
    public void addMembershipShouldAddAMembershipWithANullUser() throws Exception {
        //Given
        String userAdded = null;
        //When
        groupMembershipResolver.addMembership("group", userAdded);
        boolean actual = groupMembershipResolver.isMember(userAdded, "group");
        //Then
        assertThat(actual).isTrue();
    }

    /*
    TODO show removeMembership works
     */

    /*
    TODO show removeMembership don't fail when membership do not exist
     */

}
