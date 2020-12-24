package com.jounaidr.jrc.node.blockchain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class BlockchainConfig {
    @Bean
    public Blockchain blockchain(){
        return new Blockchain(new ArrayList<>());
    }
}
