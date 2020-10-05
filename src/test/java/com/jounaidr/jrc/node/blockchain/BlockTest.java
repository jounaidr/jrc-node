package com.jounaidr.jrc.node.blockchain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.meanbean.test.BeanTester;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
class BlockTest {

    @Test //MeanBean POJO tester will test getters/setters
    public void testPOJOWithMeanBean(){
        BeanTester tester = new BeanTester();
        tester.testBean(Block.class);
    }

    @Test //Test ensures any new variables added to Block will be added to toString method
    public void testToString(){
        ToStringVerifier.forClass(Block.class).withClassName(NameStyle.SIMPLE_NAME).verify();
    }

    @Test //Test that a block has all the correct genesis data when .genesis() method is called
    public void testGenesisBlockIsGeneratedCorrectly(){
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

    @Test //Test that .mineBlock() correctly sets previousHash value of a newly mined block to the hash value of the previous block, and that the block data is correct
    public void testMinedBlockIsGeneratedCorrectly(){
        //Given
        String genesisHash = "a08b215c09b7fe19efec514986dcc3566da094743e1c6e0d9d6ca70ed51231bc";

        Block genesisBlock = new Block();
        genesisBlock.genesis();

        Block secondBlock = new Block();
        Block thirdBlock = new Block();

        //When
        secondBlock.mineBlock(genesisBlock,"secondBlockData");
        thirdBlock.mineBlock(secondBlock,"thirdBlockData");

        //Then (note: cannot assert generated hash of second block as this will allways be different due to changing timestamp)
        assertEquals("failure - previousHash of second block does not equal genesis hash", genesisHash, secondBlock.getPreviousHash());
        assertEquals("failure - data of second block is incorrect", "secondBlockData", secondBlock.getData());

        assertEquals("failure - previousHash of third block does not equal second block hash", secondBlock.getHash(), thirdBlock.getPreviousHash());
        assertEquals("failure - previousHash of third block does not equal second block hash", "thirdBlockData", thirdBlock.getData());
        assertNotEquals("failure - hash of second block is the same as third block",secondBlock.getHash(),thirdBlock.getHash());
    }
}