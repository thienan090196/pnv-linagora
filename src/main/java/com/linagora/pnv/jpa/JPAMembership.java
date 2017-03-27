package com.linagora.pnv.jpa;

/*
 TODO implement Persistance logic
 */
public class JPAMembership {

    private String groupName;
    private String userName;

    public JPAMembership(String groupName, String userName) {
        this.groupName = groupName;
        this.userName = userName;
    }

    public JPAMembership() {
    }

    /*
    TODO implements equals and hashcode
     */
}
