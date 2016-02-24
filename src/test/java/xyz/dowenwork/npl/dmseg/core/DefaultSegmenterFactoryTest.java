package xyz.dowenwork.npl.dmseg.core;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.dowenwork.npl.dmseg.dict.loader.*;

import java.io.*;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * create at 15-4-30
 *
 * @author liufl
 * @since 1.0.0
 */
public class DefaultSegmenterFactoryTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private DefaultSegmenterFactory segmenterFactory;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        this.segmenterFactory = new DefaultSegmenterFactory();
        DictWordReaderFactory wordReaderFactory = new DictWordReaderFactory() {
            @Override
            public Reader createReader(String resource) throws IOException {
                InputStream resourceAsStream = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(resource);
                return new InputStreamReader(resourceAsStream);
            }
        };
        this.segmenterFactory.getDictSources()
                .add(new DictResource("dict.txt", "local", wordReaderFactory, new RamHashedDictionaryLoader()));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
//    @Ignore
    public void testCreate() {
        Segmenter segmenter = segmenterFactory.create();
        assertNotNull(segmenter);
        int c = 0;
        int p = 0;
        Iterator<Token> tokenIterator = null;
        try {
            StringReader reader;
            reader = new StringReader("Dmseg是一个优秀的流式中文分词器。2015年于大北京");

//            tokenIterator = segmenter.indexTokens(reader);
            tokenIterator = segmenter.queryTokens(reader);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }
        assertNotNull(tokenIterator);
        while (tokenIterator.hasNext()) {
            Token token = tokenIterator.next();
            c++;
            p += token.getPositionIncrement();
            logger.info(String.format("%2d", p) + " : " + token.toString());
        }
        assertTrue(c > 0);
    }
}
