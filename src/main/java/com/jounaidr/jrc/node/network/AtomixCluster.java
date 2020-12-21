package com.jounaidr.jrc.node.network;

import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.core.profile.Profile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
//Component
public class AtomixCluster {

    private Atomix atomixCluster;

    private static final String ATOMIX_PORT = "8081";

    @Value("${server.address}")
    private String nodeAddress;

    @Value("${peer.address}")
    private String peerAddress;

    @PostConstruct
    public void initAtomix() {
        log.info("Initialising node with network with address: {}:{} ...",nodeAddress,ATOMIX_PORT);
        this.atomixCluster = Atomix.builder()

                .withAddress(String.format("%s:%s",nodeAddress,ATOMIX_PORT)) //Initialise atomix cluster with local address
                .withMembershipProvider(BootstrapDiscoveryProvider.builder()
                        .withNodes(
                                Node.builder() //TODO: THIS CAN BE MULTIPLEIZED BY MAKING A LIST OF NODES BEFORE IN FOR LOOP
                                        .withId("peer-1")
                                        .withAddress(String.format("%s:%s",peerAddress,ATOMIX_PORT))
                                        .build())
                        .build())
                .withMulticastEnabled() //Multicast will dynamically locate and add peers
                .withProfiles(Profile.dataGrid())
                .build();

        this.atomixCluster.start().join();
        log.info("... Successfully joined network!");
    }
}
