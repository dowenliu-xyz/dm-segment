package xyz.dowenwork.npl.dmseg.core;

import com.google.common.collect.Lists;
import xyz.dowenwork.npl.dmseg.dict.loader.ClasspathDictWordReaderFactory;
import xyz.dowenwork.npl.dmseg.dict.loader.DictReloadListener;
import xyz.dowenwork.npl.dmseg.dict.loader.DictResource;
import xyz.dowenwork.npl.dmseg.dict.loader.RamHashedDictionaryLoader;

import java.util.List;

/**
 * 分词器容器
 * <p>create at 16-2-24</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public class SegmenterHolder implements DictReloadListener {
    private static final String CORE_DICT_FILE = "dict.txt";
    private final List<DictResource> dictResources = Lists.newArrayList();
    private Segmenter segmenter;

    public SegmenterHolder() {
        this.dictResources.add(new DictResource(CORE_DICT_FILE, "core", ClasspathDictWordReaderFactory.getInstance(),
                RamHashedDictionaryLoader.getInstance()));
    }

    public List<DictResource> getDictResources() {
        return dictResources;
    }

    public Segmenter getSegmenter() {
        if (segmenter == null) {
            buildSegmenter();
        }
        return segmenter;
    }

    private synchronized void buildSegmenter() {
        DefaultSegmenterFactory factory = new DefaultSegmenterFactory();
        factory.setDictSources(dictResources);
        this.segmenter = factory.create();
    }

    @Override
    public void dictReload(DictResource dictResource, boolean initial) {
        this.buildSegmenter();
    }
}
