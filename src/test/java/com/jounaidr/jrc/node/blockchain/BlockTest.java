package com.jounaidr.jrc.node.blockchain;

import org.junit.jupiter.api.Test;
import org.meanbean.test.BeanTester;

class BlockTest {

    @Test
    public void testMeanBean(){
        BeanTester tester = new BeanTester();
        tester.testBean(Block.class);
    }
}