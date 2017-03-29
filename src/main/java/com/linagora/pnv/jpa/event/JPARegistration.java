package com.linagora.pnv.jpa.event;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/*
 * TODO
 *
 * Modify this class to store it this JPA.
 *
 * Use it in jpa.event.JPAMailboxPathRegisterMapper
 */

@Entity(name="JPARegistration")
@Table(name="JPA_REGISTRATION")
@NamedQueries({
	@NamedQuery(name = "retriveAllTopicsInMailbox", query = "SELECT registration FROM JPARegistration registration WHERE registration.mailboxPath = :idMailboxPath")
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
			final int prime = 31;
			int result = 1;
			result = prime * result + ((mailboxPath == null) ? 0 : mailboxPath.hashCode());
			result = prime * result + ((topic == null) ? 0 : topic.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JPARegistrationId other = (JPARegistrationId) obj;
			if (mailboxPath == null) {
				if (other.mailboxPath != null)
					return false;
			} else if (!mailboxPath.equals(other.mailboxPath))
				return false;
			if (topic == null) {
				if (other.topic != null)
					return false;
			} else if (!topic.equals(other.topic))
				return false;
			return true;
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

	public void setMailboxPath(String mailboxPath) {
		this.mailboxPath = mailboxPath;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

    public String getTopic() {
    	return topic;
    }
    
	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expireDate == null) ? 0 : expireDate.hashCode());
		result = prime * result + ((mailboxPath == null) ? 0 : mailboxPath.hashCode());
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JPARegistration other = (JPARegistration) obj;
		if (expireDate == null) {
			if (other.expireDate != null)
				return false;
		} else if (!expireDate.equals(other.expireDate))
			return false;
		if (mailboxPath == null) {
			if (other.mailboxPath != null)
				return false;
		} else if (!mailboxPath.equals(other.mailboxPath))
			return false;
		if (topic == null) {
			if (other.topic != null)
				return false;
		} else if (!topic.equals(other.topic))
			return false;
		return true;
	}
}
