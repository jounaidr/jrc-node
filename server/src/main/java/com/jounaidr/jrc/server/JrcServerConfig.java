package com.jounaidr.jrc.server;

import com.jounaidr.jrc.server.api.BlockchainApiDelegateImpl;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;

@Configuration
public class JrcServerConfig {
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
    public Peer peer() throws IOException, JSONException {
        String peerUrl = "http://54.86.229.200:8080";
        return new Peer(blockchain, peerUrl);
    }
}
