package xyz.dowenwork.npl.dmseg.core;

import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * @author liufl
 * @since 1.0.0
 */
public class TokenTest {
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
    public void test() {
        Token token = new Token(Token.Type.WORD, 0);
        token.setPositionIncrement(1);
        assertEquals(1, token.getPositionIncrement());
    }
}
