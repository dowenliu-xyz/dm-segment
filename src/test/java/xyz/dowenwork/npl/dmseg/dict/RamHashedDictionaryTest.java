package xyz.dowenwork.npl.dmseg.dict;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author liufl
 * @since 1.0.0
 */
public class RamHashedDictionaryTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private RamHashedDictionary dict;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        dict = new RamHashedDictionary();
        dict.setNodeInitialCapacity(16);
        dict.setNodeLoadFactor(0.75F);
        dict.add("and");
        dict.add("ant", "n");
        dict.add("but");
        dict.add("button");
        dict.add("cute");
        dict.add("cute");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNodeInitialCapacity() {
        dict.setNodeInitialCapacity(8);
        assertEquals(8, dict.getNodeInitialCapacity());
    }

    @Test
    public void testNodeLoadFactor() {
        dict.setNodeLoadFactor(0.8F);
        assertEquals(0.8F, dict.getNodeLoadFactor(), 0);
    }

    @Test
    public void testBookTag() {
        dict.setBookTag("TEST");
        assertEquals("TEST", dict.dictionaryBookTag());
    }

    @Test
    public void testSize() {
        assertEquals(5, dict.size());
    }

    @Test
    public void testContains() {
        assertTrue(dict.contains("ant"));
        assertFalse(dict.contains("done"));
    }

    @Test
    public void testAdd() {
        dict.add("an");
        assertTrue(dict.contains("an"));
        assertEquals(6, dict.size());
    }

    private class MockWordPath extends WordPath {
        final Map<Character, WordPath> branches;

        public MockWordPath(MockWordPath parentPath, char fork) {
            super(parentPath, fork);
            this.branches = Collections.synchronizedMap(new HashMap<Character, WordPath>());
        }

        @Override
        public Map<Character, WordPath> getBranches() {
            return this.branches;
        }
    }

    @Test
    public void testWithPrefix() {
        assertEquals(1, dict.withPrefix(new MockWordPath(null, 'c')).size());
        assertEquals('u', dict.withPrefix(new MockWordPath(null, 'c')).iterator().next().getBranchChar());
        assertEquals(1, dict.withPrefix(new MockWordPath(new MockWordPath(null, 'c'), 'u')).size());
        assertNull(dict.withPrefix(new MockWordPath(null, 'd')));
    }

    @Test
    public void testIterator() {
        for (WordPath path : dict) {
            assertNotNull(path);
            StringBuilder sb = new StringBuilder();
            sb.append("path:").append(path.toString());
            if (path.isWord()) {
                sb.append(", get word:").append(path.getWord().getValue());
                if (!path.getWord().getTypes().isEmpty()) {
                    for (String speech : path.getWord().getTypes()) {
                        sb.append('[').append(speech).append(']');
                    }
                }
            }
            logger.info(sb.toString());
        }
    }

    @Test
    public void testDictMatch() throws IOException {
        String source = "anbuttonrefertoantgourp";
        CharBuffer buffer = CharBuffer.wrap(source);
        List<DictWord> words = this.dict.dictMatch(buffer);
        assertEquals(0, words.size());
        buffer.rewind();
        buffer.get();
        buffer.get();
        words = this.dict.dictMatch(buffer);
        assertEquals(2, words.size());
    }
}
