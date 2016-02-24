package xyz.dowenwork.npl.dmseg.core;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.dowenwork.npl.dmseg.Fragment;
import xyz.dowenwork.npl.dmseg.core.Token.Type;
import xyz.dowenwork.npl.dmseg.dict.DictWord;
import xyz.dowenwork.npl.dmseg.dict.Dictionary;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.*;

/**
 * 字符流上的分词提取器
 *
 * @author liufl
 * @since 1.0.0
 */
public abstract class ReaderTokenizer {
    Logger logger = LoggerFactory.getLogger(getClass());
    protected final Reader reader;
    protected final CharBuffer charBuffer;
    protected int streamOffset = 0;
    protected final LinkedList<Token> preTokens = new LinkedList<>();
    protected final LinkedList<FragmentMatcher> matchers = new LinkedList<>();
    private boolean init = false;
    private boolean hasDictionary = false;
    protected final TokenContext ctx = new TokenContext();

    /**
     * 创建一个分词提取器
     *
     * @param reader     输入流
     * @param bufferSize 缓冲区长度。
     *                   此长度会影响非字典分区匹配结果，使预想的结果被切分（如果预想结果长度超过缓冲区长度）。
     *                   会频繁的执行 {@link CharBuffer#compact()} 方法，如果缓冲区过长，会降低分词解析速度。
     *                   建议长度128个字符
     */
    public ReaderTokenizer(Reader reader,
            int bufferSize) {
        this.reader = reader; // 记住Reader
        this.charBuffer = CharBuffer.allocate(bufferSize);
    }

    /**
     * 加入字典
     *
     * @param dictionary 要使用的字典
     * @throws IllegalStateException 若此Tokenizer对象已经初始化。
     */
    public void appendDictionary(Dictionary dictionary) {
        if (this.init) {
            throw new IllegalStateException("应在初始化操作前加入字典！");
        }
        this.matchers.addLast(new WordMatcher(dictionary));
        this.hasDictionary = true;
    }

    /**
     * 初始化操作
     *
     * @throws IOException                     发生了IO错误
     * @throws IllegalStateException 若此Tokenizer对象已经初始化，或没有添加任何匹配器
     */
    public void init() throws IOException {
        if (!this.hasDictionary) {
            logger.warn("未加入字典！");
        }
        if (this.matchers.isEmpty()) {
            throw new IllegalStateException("没有任何匹配器！");
        }
        if (this.init) {
            throw new IllegalStateException("不能再次初始化！");
        }
        this.charBuffer.clear();
        this.reader.read(charBuffer); // 预读到缓冲区
        this.charBuffer.flip(); // 反转缓冲区
        this.init = true;
    }

    private void preTokenize() throws IOException {
        if (this.charBuffer.remaining() <= 0) {
            return;
        }
        while (this.charBuffer.remaining() > 0 && this.preTokens.isEmpty()) {
            CharBuffer readOnlyBuffer = this.charBuffer.asReadOnlyBuffer();
            List<Token> words = this.matchHead(readOnlyBuffer);
            if (words != null && !words.isEmpty()) { // 头部匹配成功
                // preTokens不再为空，将循环将结束
                this.preTokens.addAll(words);
            }
            // 移动缓冲区
            this.charBuffer.get();
            this.charBuffer.compact();
            this.reader.read(charBuffer);
            this.charBuffer.flip();
            this.streamOffset++;
            Collections.sort(this.preTokens, new Comparator<Token>() {
                @Override
                public int compare(Token x, Token y) {
                    int v = y.getEnd() - x.getEnd();
                    if (v != 0) {
                        return v;
                    }
                    v = x.getValue().compareTo(y.getValue());
                    if (v != 0) {
                        return v;
                    }
                    if (x.getType() == y.getType()) {
                        return v;
                    }
                    int[] vv = new int[]{1, 1};
                    Token[] tokens = {x, y};
                    for (int i = 0; i < 2; i++) {
                        Token token = tokens[i];
                        switch (token.getType()) {
                            case WORD: {
                                vv[i] *= 10000;
                                break;
                            }
                            case ALPHANUM: {
                                vv[i] *= 1000;
                                break;
                            }
                            case CN: {
                                vv[i] *= 1000;
                                break;
                            }
                            case DECIMAL: {
                                vv[i] *= 100;
                                break;
                            }
                        }
                    }
                    return vv[1] - vv[0];
                }
            });
            this.filter();
        }
        this.removeDuplicate();
        if (!this.preTokens.isEmpty()) {
            Iterator<Token> iterator = this.preTokens.iterator();
            int pi = 1;
            while (iterator.hasNext()) {
                iterator.next().setPositionIncrement(pi);
                pi = 0;
            }
        }
    }

    private void removeDuplicate() {
        Map<String, Token> map = new HashMap<>();
        for (Token token : this.preTokens) {
            if (map.containsKey(token.getValue())) {
                Token _token = map.get(token.getValue());
                _token.getTypes().addAll(token.getTypes());
                if (_token.getType() != token.getType()) {
                    _token.getTypes().add(token.getType().name());
                }
            } else {
                map.put(token.getValue(), token);
            }
        }
        this.preTokens.clear();
        this.preTokens.addAll(map.values());
    }

    protected List<Token> matchHead(CharBuffer charBuffer) {
        List<Token> tokens = new LinkedList<>();
        for (FragmentMatcher matcher : this.matchers) {
            CharBuffer _buffer = charBuffer.duplicate();
            List<Fragment> fragments = matcher.matchHead(_buffer);
            for (Fragment fragment : fragments) {
                Token token = Token.copyFrom(fragment, matcher.matchType(), this.streamOffset);
                if (matcher instanceof WordMatcher) {
                    WordMatcher wordMatcher = (WordMatcher) matcher;
                    token.getTypes().add(wordMatcher.getDictionary().dictionaryBookTag());
                }
                tokens.add(token);
            }
        }
        return tokens;
    }

    protected abstract void filter();

    /**
     * 是否存在下一分词
     *
     * @return 是 {@code true} ，否 {@code false}
     * @throws IllegalStateException 未初始化
     */
    public boolean hasNextToken() {
        if (!this.init) {
            throw new IllegalStateException("需要先初始化");
        }
        if (this.preTokens.isEmpty() && this.charBuffer.remaining() > 0) {
            try {
                this.preTokenize();
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
                return false;
            }
        }
        return !this.preTokens.isEmpty();
    }

    /**
     * 取出下一分词
     *
     * @return 下一分词
     * @throws NoSuchElementException 不存在下一分词
     * @throws IllegalStateException  未初始化
     */
    public Token nextToken() throws IOException {
        if (this.hasNextToken()) {
            return this.preTokens.pop();
        }
        throw new NoSuchElementException();
    }

    protected static class TokenContext {
        /**
         * 最后找到的 {@link Type#WORD} 类型切块。
         */
        public Token lastLongestWordToken = null;
        /**
         * 最近找到的 {@link Type#ALPHANUM} 类型切块。防止找出子块
         */
        public Token lastAlphaNumToken = null;
        /**
         * 最近找到的 {@link Type#DECIMAL} 类型切块。防止找出子块
         */
        public Token lastDecimalToken = null;
    }


    /**
     * 值片段匹配器
     *
     * @author liufl
     * @since 1.0.0
     */
    protected interface FragmentMatcher {
        /**
         * 匹配齐头片段
         *
         * @param charBuffer 要匹配的字符缓冲区
         * @return 匹配结果。
         */
        List<Fragment> matchHead(CharBuffer charBuffer);

        /**
         * 此匹配器匹配的类型
         *
         * @return 匹配的类型
         */
        Type matchType();
    }

    /**
     * 字典词匹配器
     *
     * @author liufl
     * @since 1.0.0
     */
    public class WordMatcher implements FragmentMatcher {
        private final Dictionary dictionary;

        public WordMatcher(Dictionary dictionary) {
            this.dictionary = dictionary;
        }

        public Dictionary getDictionary() {
            return dictionary;
        }

        @Override
        public List<Fragment> matchHead(CharBuffer charBuffer) {
            List<DictWord> dictWords = this.dictionary.dictMatch(charBuffer.asReadOnlyBuffer());
            List<Fragment> fragments = Lists.newArrayList();
            for (DictWord dictWord : dictWords) {
                fragments.add(dictWord);
            }
            return fragments;
        }

        @Override
        public Type matchType() {
            return Type.WORD;
        }
    }

    /**
     * 英文字母字符、数字区块匹配器
     */
    public class AlphaNumMatcher implements FragmentMatcher {
        @Override
        public List<Fragment> matchHead(CharBuffer charBuffer) {
            StringBuilder cache = new StringBuilder();
            char c;
            while (charBuffer.remaining() > 0) {
                c = charBuffer.get();
                Collection<Type> cTypes = Type.charType(c);
                if (cTypes.contains(Type.ALPHANUM)) { // 英文字母或阿拉伯数字
                    if (Character.isDigit(c)) {
                        if (cache.length() > 0 && '\'' == cache.charAt(cache.length() - 1)) {
                            // '后接数字，不能接受
                            break;
                        }
                    }
                    cache.append(c);
                } else {
                    if ("-_'".contains("" + c)) { // 是-、_或'等英文连接符号
                        if (cache.length() == 0) { // 不应该出现有开头
                            break;
                        }
                        boolean pureNum = true;
                        for (char _c : cache.toString().toCharArray()) {
                            if (!Character.isDigit(_c)) {
                                pureNum = false; // 不是纯数字
                                break;
                            }
                        }
                        if (pureNum && c == '\'') {
                            // 纯数字后接'，不接受
                            break;
                        }
                        cache.append(c);
                    } else {
                        break; // 不是英文字母、不是阿拉伯数字、也不是连接符号
                    }
                }
            }
            if (cache.length() > 0) {
                char lastChar = cache.charAt(cache.length() - 1);
                while ("-_'".contains("" + lastChar)) {
                    // 以连接符结尾
                    cache.deleteCharAt(cache.length() - 1);
                    lastChar = cache.charAt(cache.length() - 1);
                }
            }
            if (cache.length() > 0) {
                Token token = new Token(Type.ALPHANUM, streamOffset);
                token.setEnd(token.getOffset() + cache.length());
                token.setValue(cache.toString());
                ArrayList<Fragment> fragments = Lists.newArrayList();
                fragments.add(token);
                return fragments;
            }
            return Collections.emptyList();
        }

        @Override
        public Type matchType() {
            return Type.ALPHANUM;
        }
    }

    /**
     * 数字区块匹配器
     */
    public class DecimalMatcher implements FragmentMatcher {
        @Override
        public List<Fragment> matchHead(CharBuffer charBuffer) {
            StringBuilder cache = new StringBuilder();
            char c;
            while (charBuffer.remaining() > 0) {
                c = charBuffer.get();
                if (Type.charType(c).contains(Type.DECIMAL)) {
                    cache.append(c);
                } else {
                    break;
                }
            }
            if (cache.length() > 0) {
                Token token = new Token(Type.DECIMAL, streamOffset);
                token.setEnd(token.getOffset() + cache.length());
                token.setValue(cache.toString());
                ArrayList<Fragment> fragments = Lists.newArrayList();
                fragments.add(token);
                return fragments;
            }
            return Collections.emptyList();
        }

        @Override
        public Type matchType() {
            return Type.DECIMAL;
        }
    }

    /**
     * 标准单字CN匹配器
     */
    public class StandartCnMatcher implements FragmentMatcher {
        @Override
        public List<Fragment> matchHead(CharBuffer charBuffer) {
            char c = charBuffer.get();
            if (Type.charType(c).contains(Type.CN)) {
                Token token = new Token(Type.CN, streamOffset);
                token.setEnd(token.getOffset() + 1);
                token.setValue("" + c);
                ArrayList<Fragment> fragments = Lists.newArrayList();
                fragments.add(token);
                return fragments;
            }
            return Collections.emptyList();
        }

        @Override
        public Type matchType() {
            return Type.CN;
        }
    }

    /**
     * 不识别字符匹配器（不包括中英标点）
     */
    public class UnknownMatcher implements FragmentMatcher {
        @Override
        public List<Fragment> matchHead(CharBuffer charBuffer) {
            StringBuilder cache = new StringBuilder();
            char c;
            while (charBuffer.remaining() > 0) {
                c = charBuffer.get();
                if (Type.charType(c).contains(Type.OTHER)) {
                    if (Character.isWhitespace(c)) {
                        break; // 是空白
                    } else if (Character.UnicodeBlock.of(c).equals(Character.UnicodeBlock.GENERAL_PUNCTUATION)) {
                        break; // 英文标点
                    } else if (Character.UnicodeBlock.of(c)
                            .equals(Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION)) {
                        break; // 中文标点
                    }
                    cache.append(c);
                } else {
                    break;
                }
            }
            if (cache.length() > 0) {
                Token token = new Token(Type.OTHER, streamOffset);
                token.setEnd(token.getOffset() + cache.length());
                token.setValue(cache.toString());
                ArrayList<Fragment> fragments = Lists.newArrayList();
                fragments.add(token);
                return fragments;
            }
            return Collections.emptyList();
        }

        @Override
        public Type matchType() {
            return Type.OTHER;
        }
    }
}