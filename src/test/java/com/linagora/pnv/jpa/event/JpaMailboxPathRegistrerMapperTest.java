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

package com.linagora.pnv.jpa.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.linagora.pnv.event.MailboxPath;
import com.linagora.pnv.event.Topic;

public class JpaMailboxPathRegistrerMapperTest {

    private static final MailboxPath MAILBOX_PATH = new MailboxPath("namespace", "user", "name");
    private static final MailboxPath MAILBOX_PATH_2 = new MailboxPath("namespace2", "user2", "name2");
    private static final Topic TOPIC = new Topic("topic");
    private static final int TIMEOUT_IN_SECOND = 1;
    private static final Topic TOPIC_2 = new Topic("topic2");

    private JPAMailboxPathRegisterMapper mapper;

    @Before
    public void setUp() {
        mapper = new JPAMailboxPathRegisterMapper(TIMEOUT_IN_SECOND);
    }

    @After
    public void tearDown() {
    	EntityManager entityManager = Persistence.createEntityManagerFactory("global").createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createNativeQuery("TRUNCATE table JPA_REGISTRATION;").executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Test
    public void getTopicsShouldReturnEmptyResultByDefault() {
        assertThat(mapper.getTopics(MAILBOX_PATH)).isEmpty();
    }

    @Test
    public void doRegisterShouldWork() {
        mapper.doRegister(MAILBOX_PATH, TOPIC);
        assertThat(mapper.getTopics(MAILBOX_PATH)).containsOnly(TOPIC);
    }

    @Test
    public void doRegisterShouldBeMailboxPathSpecific() {
        mapper.doRegister(MAILBOX_PATH, TOPIC);
        assertThat(mapper.getTopics(MAILBOX_PATH_2)).isEmpty();
    }

    @Test
    public void doRegisterShouldAllowMultipleTopics() {
        mapper.doRegister(MAILBOX_PATH, TOPIC);
        mapper.doRegister(MAILBOX_PATH, TOPIC_2);
        assertThat(mapper.getTopics(MAILBOX_PATH)).containsOnly(TOPIC, TOPIC_2);
    }

    @Test
    public void doUnRegisterShouldWork() {
        mapper.doRegister(MAILBOX_PATH, TOPIC);
        mapper.doUnRegister(MAILBOX_PATH, TOPIC); 
        assertThat(mapper.getTopics(MAILBOX_PATH)).isEmpty();
    }

    @Test
    public void doUnregisterShouldBeMailboxSpecific() {
        mapper.doRegister(MAILBOX_PATH, TOPIC);
        mapper.doUnRegister(MAILBOX_PATH_2, TOPIC);
        assertThat(mapper.getTopics(MAILBOX_PATH)).containsOnly(TOPIC);
    }

    @Test
    public void doUnregisterShouldBeTopicSpecific() {
        mapper.doRegister(MAILBOX_PATH, TOPIC);
        mapper.doUnRegister(MAILBOX_PATH, TOPIC_2);
        assertThat(mapper.getTopics(MAILBOX_PATH)).containsOnly(TOPIC);
    }

    @Test
    public void entriesShouldExpire() throws Exception {
        mapper.doRegister(MAILBOX_PATH, TOPIC);
        Thread.sleep(2 * TimeUnit.SECONDS.toMillis(TIMEOUT_IN_SECOND));
        assertThat(mapper.getTopics(MAILBOX_PATH)).isEmpty();
    }

}
