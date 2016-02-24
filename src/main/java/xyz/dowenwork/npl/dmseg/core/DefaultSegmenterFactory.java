package xyz.dowenwork.npl.dmseg.core;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.dowenwork.npl.dmseg.dict.Dictionary;
import xyz.dowenwork.npl.dmseg.dict.loader.AbstractDictionaryLoader;
import xyz.dowenwork.npl.dmseg.dict.loader.DictResource;
import xyz.dowenwork.npl.dmseg.dict.loader.RamHashedDictionaryLoader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 默认分词器工厂类
 * <p>create at 15-9-25</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public class DefaultSegmenterFactory implements SegmenterFactory {
    private final static AbstractDictionaryLoader LOADER = new RamHashedDictionaryLoader();
    Logger logger = LoggerFactory.getLogger(getClass());
    private final List<DictResource> dictSources = Collections.synchronizedList(Lists.<DictResource>newArrayList());

    public List<DictResource> getDictSources() {
        return dictSources;
    }

    public void setDictSources(List<DictResource> dictSources) {
        this.dictSources.clear();
        this.dictSources.addAll(dictSources);
    }

    @Override
    public Segmenter create() {
        DefaultSegmenter segmenter = new DefaultSegmenter();
        for (DictResource dictSource : dictSources) {
            Dictionary dictionary;
            try {
                dictionary = dictSource.getDictionary();
            } catch (IOException e) {
                logger.warn("加载字典资源失败：" + dictSource, e);
                dictionary = null;
            }
            if (dictionary != null) {
                segmenter.appendDictionary(dictionary);
            }
        }
        return segmenter;
    }
}
