package xyz.dowenwork.npl.dmseg.dict.loader;

/**
 * 字典重载监听器
 * <p>create at 16-2-24</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public interface DictReloadListener {
    void dictReload(DictResource dictResource, boolean initial);
}
