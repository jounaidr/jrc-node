package com.jounaidr.jrc.server.peers.peer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeerClientTest {
    PeerClient testClient;

    @BeforeEach
    void setUp() {
        testClient = new PeerClient("10.100.156.55:7090");
    }

    @Test
    void getPeerBlockchain() {
    }

    @Test
    void getPeerBlockchainSize() {
    }

    @Test
    void getPeerLastBlock() {
    }

    @Test
    void getPeerHealth() {
    }

    @Test
    void addBlockToPeer() {
    }

    @Test
    void getHealthySocketsList() {
    }
}