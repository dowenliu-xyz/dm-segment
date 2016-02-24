package xyz.dowenwork.npl.dmseg.lucene;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.dowenwork.npl.dmseg.dict.loader.DictWordReaderFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * ZooKeeper字典词Reader工厂
 * <p>create at 16-2-24</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public class ZkDictWordReaderFactory implements DictWordReaderFactory {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CuratorFramework client;
    private final String dictPath;

    /**
     * @param client   ZooKeeper Curator客户端
     * @param dictPath 字典ZNode父ZNode路径
     */
    public ZkDictWordReaderFactory(CuratorFramework client, String dictPath) {
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
    }

    @Override
    public Reader createReader(String resource) throws IOException {
        String dictZNodePath = this.dictPath + resource;
        try {
            if (this.client.checkExists().forPath(dictZNodePath) == null) {
                logger.debug("dict ZNode {} not exists, create one with no data.", dictZNodePath);
                try {
                    this.client.create().creatingParentsIfNeeded().forPath(dictZNodePath, "".getBytes());// 明确指定词典节点内容为空
                } catch (Exception e) {
                    if (this.client.checkExists().forPath(dictZNodePath) == null) {
                        throw e;
                    }
                }
            }
            byte[] bytes = this.client.getData().forPath(dictZNodePath);
            ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
            return new InputStreamReader(bin);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("与ZK交互失败", e);
        }
    }
}
