package xyz.dowenwork.npl.dmseg.core;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.dowenwork.npl.dmseg.dict.Dictionary;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 基于Hash 的默认实现
 */
public class DefaultSegmenter implements Segmenter {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<Dictionary> dictionaries = Collections
            .synchronizedList(Lists.<Dictionary>newArrayList());

    @Override
    public Iterator<Token> indexTokens(Reader reader) throws IOException {
        DefaultIndexReaderTokenizer indexReaderTokenizer = new DefaultIndexReaderTokenizer(reader, 128);
        for (Dictionary dictionary : dictionaries) {
            indexReaderTokenizer.appendDictionary(dictionary);
        }
        indexReaderTokenizer.init();
        return new Itr(indexReaderTokenizer);
    }

    @Override
    public Iterator<Token> queryTokens(Reader reader) throws IOException {
        DefaultQueryReaderTokenizer queryReaderTokenizer = new DefaultQueryReaderTokenizer(reader, 128);
        for (Dictionary dictionary : dictionaries) {
            queryReaderTokenizer.appendDictionary(dictionary);
        }
        queryReaderTokenizer.init();
        return new Itr(queryReaderTokenizer);
    }

    @Override
    public void appendDictionary(Dictionary dictionary) {
        this.dictionaries.add(dictionary);
    }

    /**
     * ReaderTokenizer的迭代器封装
     *
     * @author liufl
     * @since 1.0.0
     */
    class Itr implements Iterator<Token> {

        private final ReaderTokenizer readerTokenizer;
        private Token next;

        Itr(ReaderTokenizer readerTokenizer) {
            this.readerTokenizer = readerTokenizer;
        }

        @Override
        public boolean hasNext() {
            if (next != null) {
                return true;
            }
            if (this.readerTokenizer.hasNextToken()) {
                try {
                    next = this.readerTokenizer.nextToken();
                } catch (IOException e) {
                    logger.warn("切词失败", e);
                    next = null;
                }
            }
            return next != null;
        }

        @Override
        public Token next() {
            if (this.hasNext()) {
                Token _next = next;
                next = null;
                return _next;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
