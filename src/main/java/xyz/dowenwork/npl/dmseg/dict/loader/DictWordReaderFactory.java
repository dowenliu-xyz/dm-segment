package xyz.dowenwork.npl.dmseg.dict.loader;

import java.io.IOException;
import java.io.Reader;

/**
 * 字典词Reader工厂接口声明
 * create at 15-4-29
 *
 * @author liufl
 * @since 1.0.0
 */
public interface DictWordReaderFactory {
    Reader createReader(String resource) throws IOException;
}
