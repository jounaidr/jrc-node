package com.jounaidr.jrc.server;

import com.jounaidr.jrc.server.api.implementation.BlockchainApiDelegateImpl;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.Peers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class JrcServerConfig {
    @Value("${peers.max}")
    private Integer PEERS_MAX;

    @Value("${peers.sockets}")
    private String PEERS_SOCKETS;

    @Autowired
    Blockchain blockchain;

    @Bean
    public Blockchain blockchain(){
        // Initialise an empty blockchain instance for this node
        return new Blockchain(new ArrayList<>());
    }

    @Bean
    public BlockchainApiDelegateImpl blockchainApiDelegateImpl(){
        // Initialise the blockchain API implementation for this node
        return new BlockchainApiDelegateImpl(blockchain);
    }

    @Bean
    public Peers peers() {
        // Initialise the peers for this node
        return new Peers(blockchain, PEERS_MAX, PEERS_SOCKETS);
    }
}
