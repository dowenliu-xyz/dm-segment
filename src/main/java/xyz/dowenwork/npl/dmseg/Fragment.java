package xyz.dowenwork.npl.dmseg;

import java.util.Set;

/**
 * 小（文本）片段
 * <p>create at 16-2-23</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public interface Fragment {
    /**
     * 值
     *
     * @return 值
     */
    String getValue();

    /**
     * 类型，类型标注或词性标注
     *
     * @return 类型
     */
    Set<String> getTypes();
}
