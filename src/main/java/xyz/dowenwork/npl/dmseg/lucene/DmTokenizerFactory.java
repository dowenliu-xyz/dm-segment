package xyz.dowenwork.npl.dmseg.lucene;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.dowenwork.npl.dmseg.core.SegmenterHolder;
import xyz.dowenwork.npl.dmseg.dict.loader.DictResource;
import xyz.dowenwork.npl.dmseg.dict.loader.DictWordReaderFactory;
import xyz.dowenwork.npl.dmseg.dict.loader.RamHashedDictionaryLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Lucene/Solr {@link DmTokenizer}对应的工厂类，负责加载配置、创建Tokenizer
 * <p>create at 16-2-24</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public class DmTokenizerFactory extends TokenizerFactory implements ResourceLoaderAware {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Map<String, SegmenterHolder> segmenterHolders = Maps.newConcurrentMap();
    private final DmTokenizerType type;
    private Set<String> extDicts;
    private final String zkDictProfile;
    private String holdingKey;

    /**
     * Initialize this factory via a set of key-value pairs.
     *
     * @param args 参数，至少要指定:
     *             <ul>
     *             <li>type 参数，INDEX或QUERY，大小写不限</li>
     *             </ul>
     *             若不指定zkDictProfile参数、指定的文件无法加载或指定的文件中配置无法使用，ZK字典将不使用。
     */
    protected DmTokenizerFactory(Map<String, String> args) {
        super(args);
        String _type = this.get(args, "type");
        this.type = DmTokenizerType.valueOf(_type.toUpperCase());
        extDicts = this.getSet(args, "extDicts");
        if (extDicts == null) {
            extDicts = Collections.emptySet();
        }
        zkDictProfile = this.get(args, "zkDictProfile", "");
    }

    @Override
    public void inform(final ResourceLoader loader) throws IOException {
        holdingKey = StringUtils.join(this.extDicts, '_');
        Properties p = new Properties();
        String zkAddress = null;
        String dictPath = null;
        String zkExtDicts = null;
        boolean zk = true;
        try {
            p.load(loader.openResource(zkDictProfile));
            zkAddress = Validate.notEmpty(p.getProperty("zkAddress"), "zkAddress is essential!");
            dictPath = Validate.notEmpty(p.getProperty("dictPath"), "dictPath should not be empty!");
            zkExtDicts = p.getProperty("extDicts");
            holdingKey += '-' +
                    zkAddress.replaceAll("[\\.:]", "_") + '-' +
                    dictPath.replaceAll("/", "_") + '-' +
                    zkExtDicts.replaceAll("[,\\.]", "_");
        } catch (Exception e) {
            logger.warn("加载zkDictProfile[{}]失败，将禁用ZK字典加载", zkDictProfile, e);
            zk = false;
        }
        synchronized (this) {
            SegmenterHolder segmenterHolder = segmenterHolders.get(holdingKey);
            if (segmenterHolder == null) {
                logger.debug("Didn't found SegmenterHolder {}, create", holdingKey);
                segmenterHolder = new SegmenterHolder();
                DictWordReaderFactory wordReaderFactory = new DictWordReaderFactory() {
                    @Override
                    public Reader createReader(String resource) throws IOException {
                        return new InputStreamReader(loader.openResource(resource));
                    }
                };
                for (String extDict : this.extDicts) {
                    segmenterHolder.getDictResources().add(new DictResource(extDict, "local", wordReaderFactory,
                            RamHashedDictionaryLoader.getInstance()));
                }
                if (zk) {
                    try {
                        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zkAddress)
                                .retryPolicy(new ExponentialBackoffRetry(1000, 5)).build();
                        logger.debug("connect to ZooKeeper {} ...", zkAddress);
                        client.start();
                        Stat stat = client.checkExists().forPath(dictPath);
                        if (stat == null) {
                            logger.debug("dict path ZNode {} not exists, create.", dictPath);
                            client.create().creatingParentsIfNeeded().forPath(dictPath, "".getBytes());// 明确指定新建节点内容为空
                        }
                        for (String zkExtDict : zkExtDicts.trim().split(",")) {
                            segmenterHolder.getDictResources()
                                    .add(new ZkDictResource(zkExtDict, RamHashedDictionaryLoader.getInstance(), client,
                                            dictPath));
                        }
                        segmenterHolders.put(holdingKey, segmenterHolder);
                    } catch (Exception e) {
                        logger.warn("与ZooKeeper交互失败，将禁用ZK字典加载", e);
                    }
                }
            }
        }
    }

    @Override
    public Tokenizer create(AttributeFactory factory) {
        logger.debug("create DmTokenizer, type {}, holdingKey {}", this.type, this.holdingKey);
        return new DmTokenizer(factory, this.type, segmenterHolders.get(holdingKey));
    }
}
