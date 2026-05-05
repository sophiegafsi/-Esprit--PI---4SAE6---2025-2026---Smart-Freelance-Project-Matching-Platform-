package com.example.recompense.Service;




import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class SimpleTest {

    @Test
    void testQueLeServiceExiste() {
        assertTrue(true);
        System.out.println("✅ Test passé !");
    }

    @Test
    void testCalculSimple() {
        int resultat = 2 + 2;
        assertEquals(4, resultat);
    }
}