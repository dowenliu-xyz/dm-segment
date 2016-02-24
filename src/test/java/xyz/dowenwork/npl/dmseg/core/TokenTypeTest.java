package xyz.dowenwork.npl.dmseg.core;

import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author liufl
 * @since 1.0.0
 */
public class TokenTypeTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCharType() {
        assertTrue(Token.Type.charType('a').contains(Token.Type.ALPHANUM));
        assertEquals(1, Token.Type.charType('a').size());
        assertTrue(Token.Type.charType('1').contains(Token.Type.ALPHANUM));
        assertTrue(Token.Type.charType('1').contains(Token.Type.DECIMAL));
        assertEquals(2, Token.Type.charType('1').size());
        assertTrue(Token.Type.charType('中').contains(Token.Type.CN));
        assertEquals(1, Token.Type.charType('中').size());
        assertTrue(Token.Type.charType('一').contains(Token.Type.CN));
        assertTrue(Token.Type.charType('一').contains(Token.Type.DECIMAL));
        assertEquals(2, Token.Type.charType('一').size());
        assertTrue(Token.Type.charType('-').contains(Token.Type.OTHER));
        assertEquals(1, Token.Type.charType('-').size());
    }
}
