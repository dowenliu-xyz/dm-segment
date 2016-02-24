package xyz.dowenwork.npl.dmseg.dict;

import org.junit.*;

import java.util.Deque;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author liufl
 * @since 1.0.0
 */
public class WordPathTest {
    private MockWordPath parent;
    private WordPath path;
    private WordPath samePath;
    private WordPath diffPath;

    private class MockWordPath extends WordPath {
        public MockWordPath(MockWordPath parentPath, char fork) {
            super(parentPath, fork);
        }

        @Override
        public Map<Character, WordPath> getBranches() {
            throw new UnsupportedOperationException();
        }
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        this.parent = new MockWordPath(null, 't');
        this.path = new MockWordPath(new MockWordPath(new MockWordPath(parent, 'e'), 's'), 't');
        this.path.wordFinish("v");
        this.samePath = new MockWordPath(new MockWordPath(new MockWordPath(parent, 'e'), 's'), 't');
        this.samePath.wordFinish();
        this.diffPath = new MockWordPath(new MockWordPath(new MockWordPath(parent, 'e'), 'x'), 't');
        this.diffPath.wordFinish();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetPathValue() {
        assertEquals("test", this.path.getPathValue());
    }

    @Test
    public void testisWord() {
        assertTrue(this.path.isWord());
        assertFalse(this.parent.isWord());
    }

    @Test
    public void testGetWord() {
        assertNull(this.parent.getWord());
        DictWord word = this.path.getWord();
        assertEquals("test", word.getValue());
        assertSame(this.path, word.getPath());
        assertTrue(word.getTypes().contains("v") && word.getTypes().size() == 1);
    }

    @Test
    public void testGetPathQueue() {
        Deque<WordPath> pathDeque = this.path.getPathQueue();
        assertEquals(4, pathDeque.size());
        assertEquals(this.parent, pathDeque.getFirst());
        assertTrue(pathDeque.pop() == pathDeque.pop().getParentPath());
    }
}
