package xyz.dowenwork.npl.dmseg.dict;

import org.apache.commons.lang3.Validate;
import xyz.dowenwork.npl.dmseg.Fragment;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 字典中的词
 * <p>create at 16-2-23</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public class DictWord implements Fragment, Serializable {
    /**
     * 字面值
     */
    protected final String value;
    /**
     * 词类型，可能是词性标注
     */
    protected final Set<String> types = Collections.synchronizedSet(new HashSet<String>(2, 0.75F));
    protected WordPath path = null;

    /**
     * 构造一个词
     *
     * @param value 词的字面值
     */
    public DictWord(String value) {
        this.value = Validate.notBlank(value, "字典词的字面值不能为空");
    }

    /**
     * 构造一个词
     *
     * @param value 词的字面值
     * @param types 词类型标注
     */
    public DictWord(String value, Set<String> types) {
        this(value);
        if (types != null) {
            this.types.addAll(types);
        }
    }


    /**
     * 获取词的字面值
     *
     * @return 字面值
     */
    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * 词类型标注
     *
     * @return 词类型标注
     */
    @Override
    public Set<String> getTypes() {
        return this.types;
    }

    /**
     * 获取词路径
     *
     * @return 词路径
     */
    public WordPath getPath() {
        return path;
    }

    /**
     * 设置词路径
     *
     * @param path 词路径
     */
    public void setPath(WordPath path) {
        this.path = path;
    }
}
