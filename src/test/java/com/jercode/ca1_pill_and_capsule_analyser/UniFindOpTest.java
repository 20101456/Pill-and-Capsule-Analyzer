package com.jercode.ca1_pill_and_capsule_analyser;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UniFindOpTest {
    private UniFindOp uf;

    @BeforeEach
    void setUp() {
        uf = new UniFindOp(10);  //start with 10 elements
    }

    @Test
    void testInitialParentLinks() {
        for (int i = 0; i < 10; i++) {
            assertEquals(i, uf.find(i), "Element " + i + " should be its own parent initially");
        }
    }

    @Test
    void testUnionAndFind() {
        //Union some pairs and test the find method
        uf.union(0, 1);
        uf.union(2, 3);
        uf.union(1, 2);

        assertEquals(uf.find(0), uf.find(1));
        assertEquals(uf.find(1), uf.find(2));
        assertEquals(uf.find(2), uf.find(3));

        assertNotEquals(uf.find(0), uf.find(4));  //there is no p4, should not be the same
    }

    @Test
    void testComponentSizeAfterUnion() {
        // check size updates correctly after union
        uf.union(0, 1);
        uf.union(0, 2);
        int expectedSize = 3; //elements 0, 1, 2 are now connected
        assertEquals(expectedSize, uf.size[uf.find(0)], "Component size after union should be updated correctly");
    }

    @Test
    void testIsSignificant() {
        uf.union(0, 1);  // Component size is now 2
        assertTrue(uf.isSignificant(0, 1, 3), "Component size 2 should be significant between 1 and 3");
        assertFalse(uf.isSignificant(0, 3, 5), "Component size 2 should not be significant between 3 and 5");
    }

    @Test
    void testPathCompression() {
        uf.union(0, 1);
        uf.union(1, 2);
        uf.union(2, 3);

        // Initial finds to trigger path compression
        uf.find(3);

        assertEquals(uf.find(0), uf.find(1));
        assertEquals(uf.find(1), uf.find(2));
        assertEquals(uf.find(2), uf.find(3));
        assertEquals(0, uf.find(3), "All elements should point to the root 0 after path compression");
    }

}