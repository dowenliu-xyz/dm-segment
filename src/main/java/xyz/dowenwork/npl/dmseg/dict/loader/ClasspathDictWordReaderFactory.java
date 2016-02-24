package xyz.dowenwork.npl.dmseg.dict.loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * CLASSPATH 字典词Reader工厂
 * <p>create at 16-2-24</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public class ClasspathDictWordReaderFactory implements DictWordReaderFactory {
    private static DictWordReaderFactory instance;

    public static synchronized DictWordReaderFactory getInstance() {
        if (instance == null) {
            instance = new ClasspathDictWordReaderFactory();
        }
        return instance;
    }

    @Override
    public Reader createReader(String resource) throws IOException {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resource);
        return new InputStreamReader(resourceAsStream);
    }
}
