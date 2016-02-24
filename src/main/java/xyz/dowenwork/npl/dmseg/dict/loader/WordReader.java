package xyz.dowenwork.npl.dmseg.dict.loader;

import xyz.dowenwork.npl.dmseg.dict.DictWord;

import java.util.NoSuchElementException;

/**
 * 字典词读取器
 * <p>create at 16-2-23</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public interface WordReader {
    /**
     * 是否存在下一词记录
     *
     * @return 是 {@code true} ，否 {@code false}
     */
    boolean hasNextWord();

    /**
     * 取下一词记录
     *
     * @return 下一词记录
     * @throws NoSuchElementException 不存在下一词记录
     */
    DictWord nextWord() throws NoSuchElementException;
}
