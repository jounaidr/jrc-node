package com.jounaidr.jrc.server.blockchain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.jounaidr.jrc.server.blockchain.helpers.BlockHelper;
import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;
import org.junit.Assert;
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
        String expectedTimeStamp = "2020-11-07T19:40:57.585581100Z";
        String expectedNonce = "dummydata";
        String expectedDifficulty = "3";
        String expectedProofOfWork = "dummyPOW";

        String expectedHash = "6034f08ebe09268c00b3144673bc0a1ce787c2e992545e3ec276b38cbebd57b6";

        Block genesisBlock = new Block();

        //When
        genesisBlock.genesis();

        //Then
        assertEquals("failure - previousHash of the genesis block is incorrect", expectedPreviousHash, genesisBlock.getPreviousHash());
        assertEquals("failure - data of the genesis block is incorrect", expectedData, genesisBlock.getData());
        assertEquals("failure - timeStamp of the genesis block is incorrect", expectedTimeStamp, genesisBlock.getTimeStamp());
        assertEquals("failure - nonce of the genesis block is incorrect", expectedNonce, genesisBlock.getNonce());
        assertEquals("failure - difficulty of the genesis block is incorrect", expectedDifficulty, genesisBlock.getDifficulty());
        assertEquals("failure - proofOfWork of the genesis block is incorrect", expectedProofOfWork, genesisBlock.getProofOfWork());

        assertEquals("failure - generated genesis block hash is incorrect", expectedHash, genesisBlock.getHash());
    }

    @Test //Test that .mineBlock() correctly sets previousHash value of a newly mined block to the hash value of the previous block, and that the block data is correct
    public void testMinedBlockIsGeneratedCorrectly(){
        //Given
        String genesisHash = "6034f08ebe09268c00b3144673bc0a1ce787c2e992545e3ec276b38cbebd57b6";

        Block genesisBlock = new Block();
        genesisBlock.genesis();

        Block secondBlock = new Block();
        Block thirdBlock = new Block();

        //When
        secondBlock.mineBlock(genesisBlock,"secondBlockData");
        thirdBlock.mineBlock(secondBlock,"thirdBlockData");

        //Then (note: cannot assert generated hash of second block as this will always be different due to changing timestamp)
        assertEquals("failure - previousHash of second block does not equal genesis hash", genesisHash, secondBlock.getPreviousHash());
        assertEquals("failure - data of second block is incorrect", "secondBlockData", secondBlock.getData());

        assertEquals("failure - previousHash of third block does not equal second block hash", secondBlock.getHash(), thirdBlock.getPreviousHash());
        assertEquals("failure - previousHash of third block does not equal second block hash", "thirdBlockData", thirdBlock.getData());
        assertNotEquals("failure - hash of second block is the same as third block",secondBlock.getHash(),thirdBlock.getHash());
    }

    @Test //Test difficulty is adjusted during .mineBlock() method
    public void testMinedBlockProofOfWorkMeetsDifficulty(){
        //Given
        Block genesisBlock = new Block();
        genesisBlock.genesis();

        Block secondBlock = new Block();
        Block thirdBlock = new Block();

        //When
        secondBlock.mineBlock(genesisBlock,"Difficulty should reduce");
        thirdBlock.mineBlock(secondBlock,"Difficulty should increase");

        long secondBlockDiff = BlockHelper.calcBlockTimeDiff(secondBlock.getTimeStamp(),genesisBlock.getTimeStamp()); //Time taken between genesis block was mined, and second block was mined
        long thirdBlockDiff = BlockHelper.calcBlockTimeDiff(thirdBlock.getTimeStamp(),secondBlock.getTimeStamp()); //Time taken between second block was mined, and third block was mined

        //Then
        assertEquals("failure - difficulty of the genesis block is incorrect", "3", genesisBlock.getDifficulty());

        assertThat("Time taken between genesis block and second block should be greater than 120 seconds", secondBlockDiff, greaterThan(120L));
        assertEquals(String.format("failure - Difference between second block and previous block is: %d , therefore difficulty should have decreased", secondBlockDiff) , "2", secondBlock.getDifficulty());

        //Below assert can fail correctly, however this is incredibly unlikely unless (as noted in failure reason) the cpu that this is ran on is unbelievably slow
        assertThat("Time taken between genesis block and third block should be less than 120 seconds, although you might just have a realllllllly bad cpu =p", thirdBlockDiff, lessThan(120L));
        assertEquals(String.format("failure - Difference between third block and previous block is: %d , therefore difficulty should have increased", thirdBlockDiff) , "3", thirdBlock.getDifficulty());
    }

    @Test //Test that if the previous difficulty is negative, the next block difficulty is set to one
    public void testNegativeDifficultyIsSetToOne(){
        //Given
        Block negativeDifficultyBlock = new Block("test", "test", "test", "2020-11-07T19:40:57.585581100Z", "69", "-5", "test");
        Block nextBlock = new Block();

        //When
        nextBlock.mineBlock(negativeDifficultyBlock, "difficulty should be set to one");

        //Then
        assertEquals("failure - difficulty of the genesis block is incorrect", "1", nextBlock.getDifficulty());
    }

    @Test //Test the proof of work validation method correctly verifies POW strings
    public void testProofOfWorkValidation(){
        //Given
        Block validProofOfWorkBlock = new Block("test", "test", "test", "2020-11-07T19:40:57.585581100Z", "69", "3", "1101101001000110011001111011000111011100110110010000100101110000110100000000011010000101111001001011000111001110010111101101011110000000000010111011001011000010010110000011110111111101011111101101011101001100100010000111000010000110100000110011111000001110");
        Block invalidProofOfWorkBlock = new Block("test", "test", "test", "2020-11-07T19:40:57.585581100Z", "69", "3", "invalid");

        //When
        Boolean shouldBeValid = validProofOfWorkBlock.isProofOfWorkValid();
        Boolean shouldBeInvalid = invalidProofOfWorkBlock.isProofOfWorkValid();

        //Then
        Assert.assertTrue("Valid proof of work is being verified as invalid", shouldBeValid);
        Assert.assertFalse("Invalid proof of work is being verified as valid", shouldBeInvalid);
    }
}