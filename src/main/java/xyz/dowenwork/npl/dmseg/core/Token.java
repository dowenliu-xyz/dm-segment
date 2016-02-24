package xyz.dowenwork.npl.dmseg.core;

import org.apache.commons.lang3.Validate;
import xyz.dowenwork.npl.dmseg.Fragment;
import xyz.dowenwork.npl.dmseg.util.ChineseNumUtil;

import java.util.*;

/**
 * 被切分出的块
 *
 * @author liufl
 * @since 1.0.0
 */
public class Token implements Fragment {
    /**
     * 切块类型集
     */
    public enum Type {
        /**
         * 匹配词
         */
        WORD,
        /**
         * 英文和阿拉伯数字
         */
        ALPHANUM,
        /**
         * 数字，包括中文数字（即使是混用）
         */
        DECIMAL,
        /**
         * 中文可能是匹配词
         */
        CN,
        /**
         * 其他
         */
        OTHER;

        /**
         * 在无字典干预的状态下，判断字的类型
         *
         * @param input 要判断的字
         * @return 类型集合
         */
        public static Collection<Type> charType(char input) {
            if (Character.isDigit(input)) {
                return Arrays.asList(ALPHANUM, DECIMAL);
            }
            if (('a' <= input && input <= 'z') || ('A' <= input && input <= 'Z')) {
                return Collections.singletonList(ALPHANUM);
            }
            Character.UnicodeBlock charBlock = Character.UnicodeBlock.of(input);
            if (charBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS // 4E00-9FBF：CJK统一表意符号
                    || charBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS // F900-FAFF：CJK兼容象形文字
                    || charBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A // 3400-4DBF：CJK统一表意符号扩展A
                    || charBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B // CJK统一表意符号扩展B
                    ) { // 中文
                List<Type> types = new LinkedList<>();
                types.add(CN);
                if (ChineseNumUtil.isChineseNum(input)) {
                    types.add(DECIMAL);
                }
                return types;
            }
            return Collections.singletonList(OTHER);
        }
    }

    final Type type;
    protected String value;
    protected final Set<String> types = Collections.synchronizedSet(new HashSet<String>(5, 0.75F));
    protected final int offset;
    protected int end; // end处的字不属性此Token
    protected int positionIncrement = 0; // 在被ReaderTokenizer取出后应将offset与之前取出的Token对象比较，有增加则此处设为1

    public Token(Type type, int offset) {
        this.type = type;
        this.offset = offset;
    }

    /**
     * 获取当前切块的类型
     *
     * @return 当前切块的类型
     */
    public Type getType() {
        return type;
    }

    /**
     * 获取切块的值
     *
     * @return 字面值
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置切块的值
     *
     * @param value 切块字面值
     */
    public void setValue(String value) {
        this.value = Validate.notNull(value);
    }

    /**
     * 获取词性集合。
     *
     * @return 词性集合的引用。操作此集合会直接操作词的词性。可能发生线程安全问题。
     */
    @Override
    public Set<String> getTypes() {
        return types;
    }

    /**
     * 获取切块在字串中的头部偏移量
     *
     * @return 偏移量
     */
    public int getOffset() {
        return offset;
    }

    /**
     * 获取当前切块的结尾索引。{@link #end} 处的字不属于此切块
     *
     * @return 切块的结尾索引
     */
    public int getEnd() {
        return end;
    }

    /**
     * 设置当前切块的结尾索引。通常应为 {@link #offset} 与 {@link #value}.length() 的和
     *
     * @param end 切块的结尾索引
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * 获取位置增量
     *
     * @return 位置增量
     */
    public int getPositionIncrement() {
        return positionIncrement;
    }

    /**
     * 设置位置增量
     *
     * @param positionIncrement 位置增量
     */
    public void setPositionIncrement(int positionIncrement) {
        this.positionIncrement = positionIncrement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Token token = (Token) o;

        return end == token.end && offset == token.offset && value.equals(token.value);

    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + offset;
        result = 31 * result + end;
        return result;
    }

    /**
     * 复制词为Token<br/>
     * 你需要为复制等到的Token设置positionIncrement，否则无增量
     *
     * @param value  源词
     * @param offset Token头部偏移量
     * @return 复制的Token
     */
    public static Token copyFrom(Fragment value, Type type, int offset) {
        Token copy = new Token(type, offset);
        for (String type_ : value.getTypes()) {
            copy.getTypes().add(type_);
        }
        String _value = value.getValue();
        copy.setEnd(offset + _value.length());
        copy.setValue(_value);
        return copy;
    }

    @Override
    public String toString() {
        return this.getValue() + " [" + this.getOffset() + ',' + this.getEnd() + ')' + ' ' + this.getType().name();
    }
}
