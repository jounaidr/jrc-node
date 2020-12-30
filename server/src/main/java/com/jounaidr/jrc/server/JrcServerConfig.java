package com.jounaidr.jrc.server;

import com.jounaidr.jrc.server.api.implementation.BlockchainApiDelegateImpl;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.peer.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class JrcServerConfig {
    @Value("${peer.sockets}")
    private String PEER_SOCKETS;

    @Autowired
    Blockchain blockchain;

    @Bean
    public Blockchain blockchain(){
        // Initialise an empty blockchain instance for this node
        return new Blockchain(new ArrayList<>());
    }

    @Bean
    public BlockchainApiDelegateImpl blockchainApiDelegate(){
        return new BlockchainApiDelegateImpl(blockchain);
    }

    @Bean
    public Peer peer() {
        String peerUrl = "http://" + PEER_SOCKETS;
        return new Peer(blockchain, peerUrl);
    }
}
