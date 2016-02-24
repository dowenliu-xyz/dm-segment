package xyz.dowenwork.npl.dmseg.lucene;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.utils.CloseableUtils;
import xyz.dowenwork.npl.dmseg.dict.loader.AbstractDictionaryLoader;
import xyz.dowenwork.npl.dmseg.dict.loader.DictResource;

import java.io.Closeable;
import java.io.IOException;

/**
 * 存储在ZooKeeper上的字典资源
 * <p>create at 16-2-24</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public class ZkDictResource extends DictResource implements NodeCacheListener, Closeable {
    private final CuratorFramework client;
    private final String dictPath;
    private final NodeCache dictCache;

    /**
     * @param dictResource     字典资源名
     * @param dictionaryLoader 字典加载器
     * @param client           ZooKeeper Curator客户端
     * @param dictPath         字典ZNode父ZNode路径
     */
    public ZkDictResource(String dictResource, AbstractDictionaryLoader dictionaryLoader, CuratorFramework client,
            String dictPath) throws Exception {
        super(dictResource, "zk", new ZkDictWordReaderFactory(client, dictPath), dictionaryLoader);
        this.client = Validate.notNull(client, "ZK Curator Client 不能为null");
        if (client.getState() == CuratorFrameworkState.LATENT) {
            client.start();
        }
        dictPath = dictPath == null ? "/" : dictPath.trim();
        if (!dictPath.startsWith("/")) {
            dictPath = "/" + dictPath;
        }
        if (!dictPath.endsWith("/")) {
            dictPath += "/";
        }
        this.dictPath = dictPath;
        String dictZNodePath = this.dictPath + dictResource;
        dictCache = new NodeCache(this.client, dictZNodePath);
        dictCache.getListenable().addListener(this); // 如果发生变化，需要重新建立分词器对象
        dictCache.start();
    }

    @Override
    public void nodeChanged() throws Exception {
        if (this.dictionary != null) {
            this.loadDictionary();
        }
    }

    @Override
    public void close() throws IOException {
        CloseableUtils.closeQuietly(this.dictCache);
    }
}
