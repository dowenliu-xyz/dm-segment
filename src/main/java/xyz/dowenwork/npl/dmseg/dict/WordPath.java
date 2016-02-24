package xyz.dowenwork.npl.dmseg.dict;

import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;

/**
 * 词路径。不论何种语言，词由字组成。字的排列状态构成了词路径。
 * 在大量词路径的集合中显然存在大量的分叉，每一个分叉是一个字。路径的结尾总是一个词的结尾。一个词的路径中可能包含的一个或多个词的路径。
 * 在词路径的分叉处标记了词结尾，以便从词路径集合中恢复一个词。
 * <p>语言中所有词的词路径集合将会发生大量的前部重叠现象，这种情况称之为前缀现象。
 * 显然有许多词成为了其他词的前缀。一个词的与它本身仅相差末尾一个字的前缀称之为紧邻前缀，
 * 如 a 是 an 的紧邻前缀， an 又是 and 的紧邻前缀。
 * 有些词在语言中不存在紧邻前缀词，如： world， 其紧邻前缀的部分为 worl ，是一个无意义的字串。
 * 定义词的紧邻前缀的词路径（不论它是不是一个词）是其词路径的父路径。</p>
 * <p>create at 16-2-23</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public abstract class WordPath implements BifurcateStructure<Character, WordPath>, Serializable {
    protected final WordPath parentPath;
    protected Set<String> wordTypes = null;
    protected final char branchChar;

    public WordPath(WordPath parentPath, char branchChar) {
        this.parentPath = parentPath;
        this.branchChar = branchChar;
    }

    /**
     * 获取此词路径的父路径
     *
     * @return 返回其父词路径。没有则返回 {@code null}
     */
    public WordPath getParentPath() {
        return parentPath;
    }

    /**
     * 判断当前路径是否代表一个词的边界
     *
     * @return 是 {@code true}，否 {@code false}
     */
    public boolean isWord() {
        return this.wordTypes != null;
    }

    /**
     * 标记当前路径代表一个词的边界
     *
     * @param speeches 词性列表
     */
    public void wordFinish(String... speeches) {
        this.wordTypes = Sets.newHashSet(speeches);
    }

    public DictWord getWord() {
        if (this.isWord()) {
            DictWord dictWord = new DictWord(this.getPathValue(), this.wordTypes);
            dictWord.setPath(this);
            return dictWord;
        }
        return null;
    }

    /**
     * 当前路径的分叉值。
     *
     * @return 分叉值
     */
    public char getBranchChar() {
        return this.branchChar;
    }

    public String getPathValue() {
        StringBuilder rePath = new StringBuilder();
        rePath.append(this.getBranchChar());
        WordPath parent = this.getParentPath();
        while (parent != null) {
            rePath.append(parent.getBranchChar());
            parent = parent.getParentPath();
        }
        return rePath.reverse().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WordPath wordPath = (WordPath) o;

        if (branchChar != wordPath.branchChar) {
            return false;
        }
        //noinspection SimplifiableIfStatement
        if (parentPath != null ? !parentPath.equals(wordPath.parentPath) : wordPath.parentPath != null) {
            return false;
        }
        return wordTypes != null ? wordTypes.equals(wordPath.wordTypes) : wordPath.wordTypes == null;

    }

    @Override
    public int hashCode() {
        int result = parentPath != null ? parentPath.hashCode() : 0;
        result = 31 * result + (wordTypes != null ? wordTypes.hashCode() : 0);
        result = 31 * result + (int) branchChar;
        return result;
    }

    public Deque<WordPath> getPathQueue() {
        Deque<WordPath> queue = new LinkedList<>();
        for (WordPath path = this; path != null; path = path.getParentPath()) {
            queue.addFirst(path);
        }
        return queue;
    }
}
