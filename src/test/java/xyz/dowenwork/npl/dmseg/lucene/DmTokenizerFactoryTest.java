package xyz.dowenwork.npl.dmseg.lucene;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.ClasspathResourceLoader;
import org.apache.zookeeper.CreateMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class DmTokenizerFactoryTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private DmTokenizerFactory factory;
    private TestingServer server;

    @Before
    public void setUp() throws Exception {
        server = new TestingServer(32181, true);
        String zkAddress = server.getConnectString();
        logger.info("Testing ZK server address: {}", zkAddress);
        String dictPath = "/xyz/dowenliu/npl/dm/dicts";
        String dict = "test.txt";
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zkAddress)
                .retryPolicy(new ExponentialBackoffRetry(1000, 5)).build();
        client.start();
        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                .forPath(dictPath + "/" + dict, "王王王王".getBytes());
        CloseableUtils.closeQuietly(client);
        Map<String, String> args = new HashMap<>();
        args.put("type", "index");
        args.put("zkDictProfile", "test.zdp");
        this.factory = new DmTokenizerFactory(args);
    }

    @After
    public void tearDown() throws Exception {
        CloseableUtils.closeQuietly(server);
    }

    @Test
    public void test() throws Exception {
        TokenStream tokenStream;
        this.factory.inform(new ClasspathResourceLoader(getClass().getClassLoader()));
        Tokenizer tokenizer = this.factory.create();
        tokenizer.setReader(new StringReader("Dmseg是一个优秀的流式中文分词器。2015年于北京王王王王"));
        tokenStream = tokenizer;
//        tokenStream = new CJKBigramFilter(tokenStream); // bigram处理
        tokenStream.reset();
        int p = 0;
        while (tokenStream.incrementToken()) {
            p += tokenStream.getAttribute(PositionIncrementAttribute.class).getPositionIncrement();
            StringBuilder sb = new StringBuilder();
            OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
            sb.append(String.format("%2d", p)).append(" : ")
                    .append(tokenStream.getAttribute(CharTermAttribute.class).toString()).append(" [")
                    .append(offsetAttribute.startOffset()).append(',').append(offsetAttribute.endOffset()).append(") ")
                    .append(tokenStream.getAttribute(TypeAttribute.class).type());
            logger.info(sb.toString());
        }
    }
}