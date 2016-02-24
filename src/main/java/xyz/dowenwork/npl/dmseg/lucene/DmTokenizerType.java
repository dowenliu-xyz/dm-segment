package xyz.dowenwork.npl.dmseg.lucene;

import xyz.dowenwork.npl.dmseg.core.Segmenter;
import xyz.dowenwork.npl.dmseg.core.Token;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

/**
 * Tokenizer使用类型，分为索引时用和查询时用两种
 * <p>create at 16-2-24</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public enum DmTokenizerType {
    /**
     * 索引用
     */
    INDEX {
        @Override
        public Iterator<Token> iterate(Segmenter segmenter, Reader reader) throws IOException {
            return segmenter.indexTokens(reader);
        }
    },
    /**
     * 查询用
     */
    QUERY {
        @Override
        public Iterator<Token> iterate(Segmenter segmenter, Reader reader) throws IOException {
            return segmenter.queryTokens(reader);
        }
    };

    public abstract Iterator<Token> iterate(Segmenter segmenter, Reader reader) throws IOException;
}
