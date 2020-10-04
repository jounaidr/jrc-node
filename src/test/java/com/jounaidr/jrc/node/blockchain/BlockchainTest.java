package com.jounaidr.jrc.node.blockchain;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
class BlockchainTest {

    @Test
    public void testBlockchainInitiatesWithGenesisBlock(){
        //Given
        Blockchain testChain;
        Block genesisBlock = new Block().genesis();

        //When
        testChain = new Blockchain();

        //Then
        assertEquals("failure - hash of the first block in the blockchain does not equal the genesis hash", genesisBlock.getHash(), testChain.getBlock(0).getHash());
        assertEquals("failure - data of the first block in the blockchain does not equal the genesis data", genesisBlock.getData(), testChain.getBlock(0).getData());
        assertEquals("failure - previous hash of the first block in the blockchain does not equal the genesis previous hash", genesisBlock.getPreviousHash(), testChain.getBlock(0).getPreviousHash());
        assertEquals("failure - timestamp of the first block in the blockchain does not equal the genesis timestamp", genesisBlock.getTimeStamp(), testChain.getBlock(0).getTimeStamp());
    }

    @Test
    public void testBlockchainAddsBlockCorrectly(){
        //Given
        Blockchain testChain= new Blockchain();

        //When
        testChain.addBlock("Hi, im the second block");
        testChain.addBlock("Sup second block im the third block");
        testChain.addBlock("And im the fourth block =)");

        //Then
        //Check that each block in the chain references the previous block hash correctly
        assertEquals("failure - previous hash of the second block does not reference hash of genesis block", testChain.getBlock(0).getHash(), testChain.getBlock(1).getPreviousHash());
        assertEquals("failure - previous hash of the third block does not reference hash of second block", testChain.getBlock(1).getHash(), testChain.getBlock(2).getPreviousHash());
        assertEquals("failure - previous hash of the fourth block does not reference hash of third block", testChain.getBlock(2).getHash(), testChain.getBlock(3).getPreviousHash());
        //Check that each block has the correct data
        assertEquals("failure - data in the second block is incorrect", "Hi, im the second block", testChain.getBlock(1).getData());
        assertEquals("failure - data in the second block is incorrect", "Sup second block im the third block", testChain.getBlock(2).getData());
        assertEquals("failure - data in the second block is incorrect", "And im the fourth block =)", testChain.getBlock(3).getData());
    }
}