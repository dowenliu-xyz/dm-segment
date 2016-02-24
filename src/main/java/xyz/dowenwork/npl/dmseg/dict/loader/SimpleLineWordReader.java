package xyz.dowenwork.npl.dmseg.dict.loader;

import xyz.dowenwork.npl.dmseg.dict.DictWord;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.NoSuchElementException;

/**
 * 字典词简单按行读取器。每行一个词。不包含词性信息。行中按空白切分，第一个非空部分认为是有效词记录，其他部分被抛弃。
 *
 * @author liufl
 * @since 1.0.0
 */
public class SimpleLineWordReader implements WordReader {
    private String preRead = null;
    private final LineNumberReader reader;

    public SimpleLineWordReader(Reader reader) {
        this.reader = new LineNumberReader(reader);
    }

    @Override
    public boolean hasNextWord() {
        preRead();
        return this.preRead != null && !"".equals(this.preRead);
    }

    private void preRead() {
        String line = null;
        while (line == null) {
            try {
                line = this.reader.readLine();
            } catch (IOException e) {
                this.preRead = null;
                return;
            }
            if (line == null) {
                this.preRead = null;
                return;
            }
            line = line.trim();
            if ("".equals(line)) {
                line = null;
            }
        }
        line = line.split("\\s+")[0];
        this.preRead = line;
    }

    public DictWord nextWord() throws NoSuchElementException {
        DictWord word = new DictWord(this.preRead);
        this.preRead = null;
        return word;
    }
}
