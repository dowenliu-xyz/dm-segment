package xyz.dowenwork.npl.dmseg.dict;

import java.util.Map;

/**
 * 分叉结构
 * <p>create at 16-2-23</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public interface BifurcateStructure<K, T> {
    Map<K, T> getBranches();
}
