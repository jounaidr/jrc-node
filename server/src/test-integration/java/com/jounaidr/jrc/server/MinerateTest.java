package com.jounaidr.jrc.server;

import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.blockchain.helpers.BlockHelper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Thread.sleep;

@RunWith(SpringJUnit4ClassRunner.class)
class MinerateTest {
    private final Thread thisThread = Thread.currentThread();
    private final int timeToRun = 1800000; // 30 minutes;

    @Test //This test will add blocks to the chain for 30 mins inorder to test difficulty is adjusted correctly around the minerate, turn logging level to info in logback.xml so console isn't flooded
    public void testMinerate(){
        Blockchain testChain = new Blockchain(new ArrayList<>()); //Initialise new blockchain

        new Thread(new Runnable() {
            @SneakyThrows
            public void run() {
                sleep(timeToRun);
                thisThread.interrupt();
            }
        }).start(); //Sleep current thread for timeToRun ms

        while (!Thread.interrupted()) { //Loop whilst thread is interrupted
            testChain.addBlock(getRandomData()); //Add a block with random data
            long diffSeconds = BlockHelper.calcBlockTimeDiff(testChain.getChain().get(testChain.getChain().size() - 1).getTimeStamp(),testChain.getChain().get(testChain.getChain().size() - 2).getTimeStamp()); //Difference in seconds between the block currently being mined, and the previously mined block

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