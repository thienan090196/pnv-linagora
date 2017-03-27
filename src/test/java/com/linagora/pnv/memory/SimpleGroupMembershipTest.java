package com.linagora.pnv.memory;

import com.linagora.pnv.GroupMembershipResolver;
import com.linagora.pnv.GroupMembershipResolverTest;

public class SimpleGroupMembershipTest extends GroupMembershipResolverTest {

    @Override
    protected GroupMembershipResolver getGroupMembershipResolver() {
        return new SimpleGroupMembershipResolver();
    }
}
