package com.linagora.pnv.jpa;

import javax.persistence.Persistence;

import com.linagora.pnv.GroupMembershipResolver;
import com.linagora.pnv.GroupMembershipResolverTest;
import com.linagora.pnv.memory.SimpleGroupMembershipResolver;

public class JPAGroupMembershipTest extends GroupMembershipResolverTest {

    @Override
    protected GroupMembershipResolver getGroupMembershipResolver() {
        return new JPAGroupMembershipResolver(Persistence.createEntityManagerFactory("global").createEntityManager());
    }
}
