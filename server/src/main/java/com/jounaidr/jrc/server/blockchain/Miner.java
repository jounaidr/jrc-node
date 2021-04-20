package com.jounaidr.jrc.server.blockchain;

import com.jounaidr.jrc.server.peers.Peers;
import com.jounaidr.jrc.server.peers.peer.Peer;
import com.jounaidr.jrc.server.peers.peer.PeerClient;
import com.jounaidr.jrc.server.peers.peer.util.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Miner implements Runnable{
    private Blockchain blockchain; //The blockchain bean instance for this node, injected through spring
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public Miner(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    private String getRandomData(){
        byte[] array = new byte[7];
        new Random().nextBytes(array);

        return new String(array, StandardCharsets.UTF_8);
    }


    public void start() {
        executor.scheduleAtFixedRate(this, 20000, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        Block nextBlock = new Block().mineBlock(blockchain.getLastBlock(), getRandomData());

        try {
            blockchain.addBlock(nextBlock); //Add a block with random data
        } catch (NullPointerException | InvalidObjectException e) { //TODO: Could replace with ReflectionTestUtils, see: https://www.baeldung.com/spring-reflection-test-utils
            //Since there is no peers bean, the peers.broadcastBlockToPeers() method call will fail, catch this and fail silently as its not relevant to this test
        }
    }
}
