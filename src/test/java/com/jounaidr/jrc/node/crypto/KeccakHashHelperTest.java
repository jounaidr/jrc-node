package com.jounaidr.jrc.node.crypto;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
class KeccakHashHelperTest {

    @Test //Test that KeccakHashHelper returns a correct SHA3 hash, verified using: https://emn178.github.io/online-tools/keccak_256.html
    public void testKeccakHashHelperProducesSHA3Hash(){
        //Given
        String testMessage = "testmessage123";
        KeccakHashHelper testHashHelper = new KeccakHashHelper(testMessage);

        //When
        String testDigest = testHashHelper.returnHash();

        //Then
        assertEquals("failure - keccak hash helper has returned the wrong hash", "7fcae3cd5c246bf2f5dbc8d43d2b2ccc2574472ec87368800a4177e35d57cabe", testDigest);
    }
}