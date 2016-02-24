package xyz.dowenwork.npl.dmseg.core;

import xyz.dowenwork.npl.dmseg.dict.Dictionary;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

/**
 * 分词器接口
 *
 * @author liufl
 * @since 1.0.0
 */
public interface Segmenter {
    /**
     * 使用索引规则分词结果。结果中可能大量包含单字，不适合做关键词提取。
     *
     * @param reader 源
     * @return 索引分词结果，通常此切分结果应尽可能枚举源中可取出词。
     * @throws IOException 如果发生了IO异常
     */
    Iterator<Token> indexTokens(Reader reader) throws IOException;

    /**
     * 使用查询规则分词结果。此分词结果可用于关键词提取。
     *
     * @param reader 源
     * @return 查询规则分词结果。通常此切分结果应尽可能过滤被长词完全包裹的短词块。
     * @throws IOException 如果发生了IO异常
     */
    Iterator<Token> queryTokens(Reader reader) throws IOException;

    /**
     * 增加分词词库字典
     *
     * @param dictionary 字典
     */
    void appendDictionary(Dictionary dictionary);
}
