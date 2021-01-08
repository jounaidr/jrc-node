package com.jounaidr.jrc.server;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.blockchain.util.BlockUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InvalidObjectException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Thread.sleep;

@RunWith(SpringJUnit4ClassRunner.class)
class MinerateTest {
    private final Thread thisThread = Thread.currentThread();
    private final int timeToRun = 1800000; // 30 minutes;

    @Test //This test will add blocks to the chain for 30 mins inorder to test difficulty is adjusted correctly around the minerate, turn logging level to info in logback.xml so console isn't flooded
    public void testMinerate() throws InvalidObjectException{
        Blockchain testChain = new Blockchain(new ArrayList<>()); //Initialise new blockchain

        new Thread(new Runnable() {
            @SneakyThrows
            public void run() {
                sleep(timeToRun);
                thisThread.interrupt();
            }
        }).start(); //Sleep current thread for timeToRun ms

        while (!Thread.interrupted()) { //Loop whilst thread is interrupted
            Block nextBlock = new Block().mineBlock(testChain.getLastBlock(), getRandomData());

            try {
                testChain.addBlock(nextBlock); //Add a block with random data
            } catch (NullPointerException e) { //TODO: Could replace with ReflectionTestUtils, see: https://www.baeldung.com/spring-reflection-test-utils
                //Since there is no peers bean, the peers.broadcastBlockToPeers() method call will fail, catch this and fail silently as its not relevant to this test
            }

            long diffSeconds = BlockUtil.calcBlockTimeDiff(testChain.getLastBlock().getTimeStamp(),testChain.getChain().get(testChain.getChain().size() - 2).getTimeStamp()); //Difference in seconds between the block currently being mined, and the previously mined block

            System.out.println("Block took: " + diffSeconds + " seconds to mine...");
        }

        System.out.println("Blocks should take on average 120 seconds to mine as determined by the minerate, check the above console logs!");
    }

    private String getRandomData(){
        byte[] array = new byte[7];
        new Random().nextBytes(array);

        return new String(array, StandardCharsets.UTF_8);
    }
}