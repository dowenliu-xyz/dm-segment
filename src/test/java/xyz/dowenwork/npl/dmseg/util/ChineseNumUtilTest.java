package xyz.dowenwork.npl.dmseg.util;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * @author liufl
 * @since 1.0.0
 */
public class ChineseNumUtilTest {
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
    public void testChineseNumToInt() {
        assertEquals(1000, ChineseNumUtil.chineseNumToInt(new char[]{'一', '千'}));
        assertEquals(1000, ChineseNumUtil.chineseNumToInt(new char[]{'1', '千'}));
    }

    @Test
    public void testIsChineseNum() {
        assertTrue(ChineseNumUtil.isChineseNum('1'));
        assertTrue(ChineseNumUtil.isChineseNum('一'));
        assertFalse(ChineseNumUtil.isChineseNum('g'));
        assertFalse(ChineseNumUtil.isChineseNum('王'));
    }
}
