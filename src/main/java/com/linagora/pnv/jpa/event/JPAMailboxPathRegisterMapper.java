package com.linagora.pnv.jpa.event;

import java.util.Date;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.base.Throwables;

import com.linagora.pnv.event.DistantMailboxPathRegisterMapper;
import com.linagora.pnv.event.MailboxPath;
import com.linagora.pnv.event.Topic;
import com.linagora.pnv.jpa.event.JPARegistration.JPARegistrationId;

public class JPAMailboxPathRegisterMapper implements DistantMailboxPathRegisterMapper {

    private final int timeOutInSeconds;
    private final EntityManager entityManager;
    
    public static final Function<JPARegistration, Topic> READ_ROW = new Function<JPARegistration, Topic>() {
       
		@Override
		public Topic apply(JPARegistration input) {
			return new Topic(input.getTopic());
		}
    };
    
    public JPAMailboxPathRegisterMapper(int timeOutInSeconds) {
        this.timeOutInSeconds = timeOutInSeconds;
        this.entityManager = Persistence.createEntityManagerFactory("global").createEntityManager();
    }

    @Override
    public Set<Topic> getTopics(MailboxPath mailboxPath) {
    	return FluentIterable.from(entityManager.createNamedQuery("retriveAllTopicsForMailbox", JPARegistration.class)
    			.setParameter("idMailboxPath", mailboxPath.asString()).getResultList())
    			.filter(new Predicate <JPARegistration>() {
    				public boolean apply(JPARegistration input) {
    					return input.getExpireDate().after(new Date());
    				}
    			})
    			.transform(READ_ROW)
    			.toSet();
    }

    @Override
    public void doRegister(MailboxPath mailboxPath, Topic topic) {
    	entityManager.getTransaction().begin();
    	
    	JPARegistration jpaRegistration = new JPARegistration(mailboxPath.asString(), topic.getValue(), computeExpireDate());
    	entityManager
    		.merge(jpaRegistration);
    	entityManager.getTransaction().commit();
    }
    
    private Date computeExpireDate() {
    	 Date now = new Date();
    	 now.setSeconds(now.getSeconds() + timeOutInSeconds);
    	 return now;
    }

    @Override
    public void doUnRegister(MailboxPath mailboxPath, Topic topic) {
        try {
            entityManager.getTransaction().begin();
            entityManager.createNamedQuery("deleteRegistration")
            	.setParameter("idMailboxPath", mailboxPath.asString())
            	.setParameter("idTopic", topic.getValue())
            	.executeUpdate();
            entityManager.getTransaction().commit();
        } catch (NoResultException e) {
        	
        	
        } catch (PersistenceException pe) {
            throw Throwables.propagate(pe);
        }
    }
}