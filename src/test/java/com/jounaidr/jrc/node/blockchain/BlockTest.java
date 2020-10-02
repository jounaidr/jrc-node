package com.jounaidr.jrc.node.blockchain;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.meanbean.test.BeanTester;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
class BlockTest {

    @Test
    public void testPOJOWithMeanBean(){
        BeanTester tester = new BeanTester();
        tester.testBean(Block.class);
    }

    @Test
    public void testGenesisBlockHashGeneratedCorrectly(){
        //Given
        String expectedPreviousHash = "dummyhash";
        String expectedData = "dummydata";
        String expectedTimeStamp = "1";

        String expectedHash = "a08b215c09b7fe19efec514986dcc3566da094743e1c6e0d9d6ca70ed51231bc";

        Block genesisBlock = new Block();

        //When
        genesisBlock.genesis();

        //Then
        assertEquals("failure - previousHash of the genesis block is incorrect", expectedPreviousHash, genesisBlock.getPreviousHash());
        assertEquals("failure - data of the genesis block is incorrect", expectedData, genesisBlock.getData());
        assertEquals("failure - timeStamp of the genesis block is incorrect", expectedTimeStamp, genesisBlock.getTimeStamp());

        assertEquals("failure - generated genesis block hash is incorrect", expectedHash, genesisBlock.getHash());
    }
}