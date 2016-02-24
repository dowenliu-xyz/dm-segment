package xyz.dowenwork.npl.dmseg.dict.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.dowenwork.npl.dmseg.dict.RamHashedDictionary;

import java.io.Reader;

/**
 * @author liufl
 * @since 1.0.0
 */
public class RamHashedDictionaryLoader extends AbstractDictionaryLoader {
    private static RamHashedDictionaryLoader instance;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static synchronized AbstractDictionaryLoader getInstance() {
        if (instance == null) {
            instance = new RamHashedDictionaryLoader();
        }
        return instance;
    }

    public WordReader wrapReader(Reader reader) {
        return new SimpleLineWordReader(reader);
    }

    @Override
    public RamHashedDictionary readIn(Reader reader) {
        RamHashedDictionary dictionary = new RamHashedDictionary();
        try {
            this.apply(dictionary, reader);
        } catch (Throwable throwable) {
            logger.error("读取字典" + reader.toString() + "失败", throwable);
        }
        return dictionary;
    }
}
