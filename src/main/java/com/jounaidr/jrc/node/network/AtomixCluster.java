package com.jounaidr.jrc.node.network;

import io.atomix.core.Atomix;
import io.atomix.core.profile.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AtomixCluster {

    private Atomix atomixCluster;

    private static final String ATOMIX_PORT = "8081";

    @Value("${server.address}")
    private String nodeAddress;

    @PostConstruct
    public void initAtomix() {
        this.atomixCluster = Atomix.builder()
                .withAddress(String.format("%s:%s",nodeAddress,ATOMIX_PORT)) //Initialise atomix cluster with local address
                .withMulticastEnabled() //Multicast will dynamically locate and add peers
                .withProfiles(Profile.dataGrid())
                .build();

        this.atomixCluster.start().join();
    }
}
