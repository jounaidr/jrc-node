package com.jounaidr.jrc.node.crypto;

import com.jounaidr.Cryptonight;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
class CryptonightTest {
    List<String> inputData = Arrays
            .asList("This is a test This is a test This is a test",
                    "Lorem ipsum dolor sit amet, consectetur adipiscing",
                    "elit, sed do eiusmod tempor incididunt ut labore",
                    "et dolore magna aliqua. Ut enim ad minim veniam,",
                    "quis nostrud exercitation ullamco laboris nisi",
                    "ut aliquip ex ea commodo consequat. Duis aute",
                    "irure dolor in reprehenderit in voluptate velit",
                    "esse cillum dolore eu fugiat nulla pariatur.",
                    "Excepteur sint occaecat cupidatat non proident,",
                    "sunt in culpa qui officia deserunt mollit anim id est laborum.",
                    //Above 10 hash tests are taken from Monero CryptoNightV2 implementation (which the CryptoNightJNI component is based off: https://github.com/monero-project/monero/commit/f3cd51a12b202875bd8191668aceb8a4f810ecd4)
                    "This is a test",
                    "",
                    "oioi im brit-ish",
                    "abc",
                    "x123!$$£*^(%)$$$$");

    List<String> validHashes = Arrays
            .asList("353fdc068fd47b03c04b9431e005e00b68c2168a3cc7335c8b9b308156591a4f",
                    "72f134fc50880c330fe65a2cb7896d59b2e708a0221c6a9da3f69b3a702d8682",
                    "410919660ec540fc49d8695ff01f974226a2a28dbbac82949c12f541b9a62d2f",
                    "4472fecfeb371e8b7942ce0378c0ba5e6d0c6361b669c587807365c787ae652d",
                    "577568395203f1f1225f2982b637f7d5e61b47a0f546ba16d46020b471b74076",
                    "f6fd7efe95a5c6c4bb46d9b429e3faf65b1ce439e116742d42b928e61de52385",
                    "422f8cfe8060cf6c3d9fd66f68e3c9977adb683aea2788029308bbe9bc50d728",
                    "512e62c8c8c833cfbd9d361442cb00d63c0a3fd8964cfd2fedc17c7c25ec2d4b",
                    "12a794c1aa13d561c9c6111cee631ca9d0a321718d67d3416add9de1693ba41e",
                    "2659ff95fc74b6215c1dc741e85b7a9710101b30620212f80eb59c3c55993f9d",
                    //Above 10 hash tests are taken from Monero CryptoNightV2 implementation (which the CryptoNightJNI component is based off: https://github.com/monero-project/monero/commit/f3cd51a12b202875bd8191668aceb8a4f810ecd4)
                    "c1d6521259b6a9d29eb19df3895c601bcb9ae1811ec3dd175a4a9c2949af14fe",
                    "e34985722288be50a2068f973f02248d62e7bc6a0a0dfca2eb84909724857a72",
                    "72bf63a5503919c9d25b9eaaaa50eac6fc1a93e852c4a1fced9b8246da2a22ab",
                    "15b5d19b1a77580c49be0560154d94aace754e6640388e2d738a0ab77f3f2c07",
                    "b29d5abcb136383eefef46d8f1142f8f7787156f15ae6823c5c9d5eef19cce35");

    @Test //Test duplicated in use Cryptonight component: https://github.com/jounaidr/CryptoNightJNI
    public void testCryptonightHashesAreCorrect(){
        Cryptonight cryptonight;
        String out;

        //Inorder for test to run in CI, each hash check must be done sequentially (not in for loop)
        cryptonight = new Cryptonight(inputData.get(0));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(0)),validHashes.get(0),out);

        cryptonight = new Cryptonight(inputData.get(1));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(1)),validHashes.get(1),out);

        cryptonight = new Cryptonight(inputData.get(2));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(2)),validHashes.get(2),out);

        cryptonight = new Cryptonight(inputData.get(3));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(3)),validHashes.get(3),out);

        cryptonight = new Cryptonight(inputData.get(4));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(4)),validHashes.get(4),out);

        cryptonight = new Cryptonight(inputData.get(5));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(5)),validHashes.get(5),out);

        cryptonight = new Cryptonight(inputData.get(6));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(6)),validHashes.get(6),out);

        cryptonight = new Cryptonight(inputData.get(7));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(7)),validHashes.get(7),out);

        cryptonight = new Cryptonight(inputData.get(8));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(8)),validHashes.get(8),out);

        cryptonight = new Cryptonight(inputData.get(9));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(9)),validHashes.get(9),out);

        cryptonight = new Cryptonight(inputData.get(10));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(10)),validHashes.get(10),out);

        cryptonight = new Cryptonight(inputData.get(11));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(11)),validHashes.get(11),out);

        cryptonight = new Cryptonight(inputData.get(12));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(12)),validHashes.get(12),out);

        cryptonight = new Cryptonight(inputData.get(13));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(13)),validHashes.get(13),out);

        cryptonight = new Cryptonight(inputData.get(14));
        out = new String(Hex.encode(cryptonight.returnHash()));
        assertEquals(String.format("The following message was incorrectly hashed: %s ", inputData.get(14)),validHashes.get(14),out);
    }
}