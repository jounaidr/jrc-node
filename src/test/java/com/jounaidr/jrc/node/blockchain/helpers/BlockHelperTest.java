package com.jounaidr.jrc.node.blockchain.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

class BlockHelperTest {

    @Test
    void testGetBinaryStringLeadingZeros() {
        //given
        byte[] in = new byte[] { (byte) 0x00, (byte)0x48, (byte)0x00, (byte)0x03}; //Initialise binary array with 9 leading zeros

        int out = BlockHelper.getBinaryStringLeadingZeros(in);

        assertEquals("failure - Binary String output is incorrect", 9, out);
    }

    @Test
    void testGetBinaryString() {
        //Given
        String expected = "0000000001001000";
        byte[] in = new byte[] { (byte) 0x00, (byte)0x48}; //Initialise binary array with expected binary number

        //When
        String out = BlockHelper.getBinaryString(in);

        //Then
        assertEquals("failure - Binary String has incorrect length", 16, out.length());
        assertEquals("failure - Binary String output is incorrect", expected, out);
    }
}