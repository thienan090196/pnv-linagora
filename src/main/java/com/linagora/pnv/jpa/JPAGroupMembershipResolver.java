package com.linagora.pnv.jpa;

import javax.persistence.EntityManager;

import com.linagora.pnv.GroupMembershipResolver;

/*
TODO fill the gaps
 */
public class JPAGroupMembershipResolver implements GroupMembershipResolver {

    private final EntityManager entityManager;

    public JPAGroupMembershipResolver(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void addMembership(String group, String user) {

    }

    @Override
    public void removeMembership(String group, String user) {

    }

    @Override
    public boolean isMember(String user, String group) {
        return false;
    }
}
