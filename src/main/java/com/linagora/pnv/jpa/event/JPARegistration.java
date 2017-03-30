package com.linagora.pnv.jpa.event;

import java.io.Serializable;

import java.util.Date;

import java.lang.Object;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.google.common.base.Objects;


@Entity(name="JPARegistration")
@Table(name="JPA_REGISTRATION")
@NamedQueries({
	@NamedQuery(name = "deleteRegistration", 
			query = "DELETE FROM JPARegistration registration "
					+ "WHERE registration.mailboxPath = :idMailboxPath AND registration.topic = :idTopic"),
	@NamedQuery(name = "retriveAllTopicsForMailbox", 
			query = "SELECT registration FROM JPARegistration registration "
					+ "WHERE registration.mailboxPath = :idMailboxPath"),
})
@IdClass(JPARegistration.JPARegistrationId.class)
public class JPARegistration {

	public static final class JPARegistrationId implements Serializable {
		
		private String mailboxPath;
		private String topic;
		
		public JPARegistrationId() {
		}

		public JPARegistrationId(String mailboxPath, String topic) {
			this.mailboxPath = mailboxPath;
			this.topic = topic;
		}

		public String getMailboxPath() {
			return mailboxPath;
		}

		public String getTopic() {
			return topic;
		}

		
		
		
		@Override
		public int hashCode() {
			return Objects.hashCode(mailboxPath, topic);
		}

		@Override
		public boolean equals(Object obj) {
			if  (obj instanceof JPARegistrationId) {
			    JPARegistrationId that = (JPARegistrationId ) obj;

			    return Objects.equal(this.topic, that.topic) 
			    	&& Objects.equal(this.mailboxPath, that.mailboxPath);
			}
			return false;
		}
	}
	
	public static final String MAILBOXPATH = "MAILBOXPATH";
	public static final String TOPIC = "TOPIC";
	public static final String EXPIREDATE = "EXPIREDATE";
	
	@Id
	@Column(name = MAILBOXPATH, length = 200)
    private String mailboxPath;

	@Id
	@Column(name = TOPIC, length = 200)
    private String topic;

	@Basic
	@Column(name = EXPIREDATE)
    private Date expireDate;
	

	public JPARegistration() {
    }
	
    public JPARegistration(String mailboxPath, String topic, Date expireDate) {
        this.mailboxPath = mailboxPath;
        this.topic = topic;
        this.expireDate = expireDate;
    }

    public String getMailboxPath() {
		return mailboxPath;
	}

	public Date getExpireDate() {
		return expireDate;
	}

    public String getTopic() {
    	return topic;
    }

	@Override
	public int hashCode() {
		return Objects.hashCode(mailboxPath, topic);
	}

	@Override
	public boolean equals(Object obj) {
		if  (obj instanceof JPARegistration) {
		    JPARegistration that = (JPARegistration) obj;

		    return Objects.equal(this.topic, that.topic) 
		    	&& Objects.equal(this.mailboxPath, that.mailboxPath);
		}
		return false;
	}
}
