package com.jounaidr.jrc.node.crypto;

import com.jounaidr.Cryptonight;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
class CryptonightTest {
    List<String> inputData = Arrays
            .asList("This is a test",
                    "",
                    "oioi im brit-ish",
                    "abc",
                    "x123!$$£*^(%)$$$$");

    List<String> validHashes = Arrays
            .asList("c1d6521259b6a9d29eb19df3895c601bcb9ae1811ec3dd175a4a9c2949af14fe",
                    "e34985722288be50a2068f973f02248d62e7bc6a0a0dfca2eb84909724857a72",
                    "72bf63a5503919c9d25b9eaaaa50eac6fc1a93e852c4a1fced9b8246da2a22ab",
                    "15b5d19b1a77580c49be0560154d94aace754e6640388e2d738a0ab77f3f2c07",
                    "b29d5abcb136383eefef46d8f1142f8f7787156f15ae6823c5c9d5eef19cce35");

    @Test //Test taken from current in use Cryptonight component: https://github.com/jounaidr/CryptoNightJNI
    public void testCryptonightHashesAreCorrect(){
        for (int i = 0; i < inputData.size(); i++) {
            Cryptonight cryptonight = new Cryptonight(inputData.get(i));
            Assertions.assertEquals(validHashes.get(i),new String(Hex.encode(cryptonight.returnHash())));
        }
    }
}