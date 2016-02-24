package xyz.dowenwork.npl.dmseg.dict;

import java.nio.CharBuffer;
import java.util.Collection;
import java.util.List;

/**
 * 字典表接口。维护和管理词集合。不能进行对象间比较，非同一对象认为两对象不同。
 * <p>默认使用RAM中的hash表实现快速读写，速度快但需要消耗较多内存。
 * 在字典极大时(应该在字典词数在百万到千万数级别时)可能会发生内存溢出，此时应考虑自定义可持久化快查方案，
 * 如使用Redis或Berkeley DB这样可快速读写的key-value数据库实现。</p>
 * <p>不建议使用tree map实现，相比hash map，内存使用节省不明显，但查找效率随字典词数等级增加下降明显。
 * 参考<a href="http://epy.iteye.com/blog/1975264">Java中TreeMap VS HashMap</a></p>
 *
 * @author liufl
 * @since 1.0.0
 */
public interface Dictionary extends BifurcateStructure<Character, WordPath>, Iterable<WordPath> {
    /**
     * 字典中的词数
     *
     * @return 收录的词数
     */
    int size();

    /**
     * 此字典中是否包含词
     *
     * @param word 要判断的词
     * @return 包含 {@code true}，否则 {@code false}
     */
    boolean contains(String word);

    /**
     * 将词加入字典。加入已有的词将不会对字典产生任何影响。
     *
     * @param word     要加入的词。
     * @param speeches 词性列表
     * @return 通常就始终返回 {@code true}，但实现类可能返回 {@code false}，
     * 取决于实现类的实现方式，如使用数据库或网络缓存等实现时I/O或权限错误等。
     */
    boolean add(String word, String... speeches);

    /**
     * 将词加入字典。加入已有的词将不会对字典产生任何影响。
     *
     * @param word 要加入的词。
     * @return 通常就始终返回 {@code true}，但实现类可能返回 {@code false}，
     * 取决于实现类的实现方式，如使用数据库或网络缓存等实现时I/O或权限错误等。
     */
    boolean add(DictWord word);

    /**
     * 取出具有指定紧邻前缀的词路径
     *
     * @param path 指定紧邻前缀
     * @return 如果不存在匹配前缀，返回 {@code null}。
     * 如果 {@code path} 是一个词，且字典中不存在此前缘的词，返回空集合。
     */
    Collection<WordPath> withPrefix(WordPath path);

    /**
     * 找出字符缓冲区中齐头匹配的词
     *
     * @param charBuffer 字符缓冲区
     * @return 缓冲区第一个字开头的词
     */
    List<DictWord> dictMatch(CharBuffer charBuffer);

    /**
     * 返回此字典书名标记。用于分词结果标记（记录在词性上）
     *
     * @return 字典书名标记
     */
    String dictionaryBookTag();
}
