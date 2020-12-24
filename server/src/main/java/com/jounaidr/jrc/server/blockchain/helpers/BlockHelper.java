package com.jounaidr.jrc.server.blockchain.helpers;

import java.time.Instant;

public class BlockHelper {

    /**
     * Return the number of leading zeros
     * in a byte array binary string
     *
     * @param input byte array input
     * @return int of leading zeros
     */
    public static int getByteArrayLeadingZeros(byte[] input){
        int leadingZeros;
        String inputAsString = getBinaryString(input);

        leadingZeros = inputAsString.length() - inputAsString.replaceAll("^0+", "").length();

        return leadingZeros;
    }

    /**
     * Converts a byte array to a binary string
     *
     * @param input byte array input
     * @return binary string equivalent to byte array input
     */
    public static String getBinaryString(byte[] input)
    {
        StringBuilder binaryString = new StringBuilder(input.length * Byte.SIZE);

        for( int i = 0; i < Byte.SIZE * input.length; i++ ){
            binaryString.append((input[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        }

        return binaryString.toString();
    }

    /**
     * Calc the difference in time between
     * two blocks in seconds as long
     *
     * @param currentBlockTs  the current blocks time stamp
     * @param previousBlockTs the previous blocks time stamp to calc time diff from
     * @return seconds between current and previous block as long
     */
    public static long calcBlockTimeDiff(String currentBlockTs, String previousBlockTs){
        Instant previousBlockTimeStamp = Instant.parse(previousBlockTs);
        Instant currentTimeStamp = Instant.parse(currentBlockTs);

        return currentTimeStamp.getEpochSecond() - previousBlockTimeStamp.getEpochSecond();
    }
}
