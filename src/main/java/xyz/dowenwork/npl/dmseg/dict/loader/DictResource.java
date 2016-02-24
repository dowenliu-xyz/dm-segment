package xyz.dowenwork.npl.dmseg.dict.loader;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.dowenwork.npl.dmseg.dict.Dictionary;

import java.io.IOException;

/**
 * 字典资源描述，包括资源引用和加载方式
 * <p>create at 15-9-25</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public class DictResource {
    Logger logger = LoggerFactory.getLogger(getClass());
    private final String dictResource;
    private final String typeDescription;
    private final DictWordReaderFactory wordReaderFactory;
    private final AbstractDictionaryLoader<Dictionary> dictionaryLoader;
    private Dictionary dictionary;

    /**
     * @param dictResource      字典资源名
     * @param typeDescription   类型
     * @param wordReaderFactory Reader 工厂
     * @param dictionaryLoader  字典加载器
     */
    public DictResource(String dictResource, String typeDescription, DictWordReaderFactory wordReaderFactory,
            AbstractDictionaryLoader<Dictionary> dictionaryLoader) {
        this.dictResource = Validate.notNull(dictResource);
        this.typeDescription = Validate.notBlank(typeDescription);
        this.wordReaderFactory = Validate.notNull(wordReaderFactory);
        this.dictionaryLoader = Validate.notNull(dictionaryLoader);
    }

    /**
     * 字典资源名
     *
     * @return 字典资源名
     */
    public String getDictResource() {
        return this.dictResource;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public Dictionary getDictionary() throws IOException {
        if (this.dictionary == null) {
            loadDictionary();
        }
        return this.dictionary;
    }

    public synchronized void loadDictionary() throws IOException {
        logger.info("load " + getTypeDescription() + " dict :" + getDictResource());
        this.dictionary = this.dictionaryLoader.readIn(this.wordReaderFactory.createReader(this.dictResource));
    }
}
