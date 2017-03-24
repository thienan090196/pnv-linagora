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

package org.apache.james.mailbox.jpa;

import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.james.mailbox.jpa.mail.model.JPAMailbox;
import org.apache.james.mailbox.jpa.mail.model.JPAMailboxAnnotation;
import org.apache.james.mailbox.jpa.mail.model.JPAProperty;
import org.apache.james.mailbox.jpa.mail.model.JPAUserFlag;
import org.apache.james.mailbox.jpa.mail.model.openjpa.AbstractJPAMailboxMessage;
import org.apache.james.mailbox.jpa.mail.model.openjpa.JPAMailboxMessage;
import org.apache.james.mailbox.jpa.quota.model.MaxDefaultMessageCount;
import org.apache.james.mailbox.jpa.quota.model.MaxDefaultStorage;
import org.apache.james.mailbox.jpa.quota.model.MaxUserMessageCount;
import org.apache.james.mailbox.jpa.quota.model.MaxUserStorage;
import org.apache.james.mailbox.jpa.user.model.JPASubscription;
import org.apache.openjpa.persistence.OpenJPAPersistence;

import com.google.common.base.Supplier;

public class EntityManagerFactorySupplier implements Supplier<EntityManagerFactory> {

    @Override
    public EntityManagerFactory get() {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("openjpa.ConnectionDriverName", org.h2.Driver.class.getName());
        properties.put("openjpa.ConnectionURL", "jdbc:h2:mem:mailboxintegration;DB_CLOSE_DELAY=-1"); // Memory H2 database
        properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)"); // Create Foreign Keys
        properties.put("openjpa.jdbc.MappingDefaults", "ForeignKeyDeleteAction=restrict, JoinForeignKeyDeleteAction=restrict");
        properties.put("openjpa.jdbc.SchemaFactory", "native(ForeignKeys=true)");
        properties.put("openjpa.jdbc.MappingDefaults", "ForeignKeyDeleteAction=cascade, JoinForeignKeyDeleteAction=cascade");
        properties.put("openjpa.jdbc.QuerySQLCache", "false");
        properties.put("openjpa.Log", "JDBC=WARN, SQL=WARN, Runtime=WARN");
        properties.put("openjpa.MetaDataFactory", "jpa(Types=" +
            JPAMailbox.class.getName() + ";" +
            AbstractJPAMailboxMessage.class.getName() + ";" +
            JPAMailboxMessage.class.getName() + ";" +
            JPAProperty.class.getName() + ";" +
            JPAUserFlag.class.getName() + ";" +
            JPAMailboxAnnotation.class.getName() + ";" +
            JPASubscription.class.getName() + "," +
            MaxDefaultMessageCount.class.getName() + "," +
            MaxDefaultStorage.class.getName() + "," +
            MaxUserMessageCount.class.getName() + "," +
            MaxUserStorage.class.getName() + ")");

        return OpenJPAPersistence.getEntityManagerFactory(properties);
    }

    public void clear() {
        EntityManager entityManager = get().createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createNativeQuery("TRUNCATE table JAMES_MAIL_USERFLAG;").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE table JAMES_MAIL_PROPERTY;").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE table JAMES_MAILBOX_ANNOTATION;").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE table JAMES_MAILBOX;").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE table JAMES_MAIL;").executeUpdate();
        /*
        Question 9 :

        Delete the four tables you created here.
         */
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
