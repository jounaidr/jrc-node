package com.jounaidr.jrc.node.blockchain.helpers;

public class BlockHelper {

    public static int getBinaryStringLeadingZeros(byte[] input){
        int leadingZeros;
        String inputAsString = getBinaryString(input);

        leadingZeros = inputAsString.length() - inputAsString.replaceAll("^0+", "").length();

        return leadingZeros;
    }

    public static String getBinaryString(byte[] input)
    {
        StringBuilder binaryString = new StringBuilder(input.length * Byte.SIZE);

        for( int i = 0; i < Byte.SIZE * input.length; i++ ){
            binaryString.append((input[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        }

        return binaryString.toString();
    }
}
