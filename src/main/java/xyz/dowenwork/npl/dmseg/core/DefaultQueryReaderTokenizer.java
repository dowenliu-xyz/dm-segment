package xyz.dowenwork.npl.dmseg.core;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.Reader;
import java.util.List;

/**
 * 默认的查询用分词器。
 * <br/>切分规则如下：
 * <ul>
 * <li>全文标准切分</li>
 * <li>正向最大匹配切分</li>
 * <li>
 * 字典未匹配部分：
 * <ul>
 * <li>标准切分</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author liufl
 * @since 1.0.0
 */
public class DefaultQueryReaderTokenizer extends ReaderTokenizer {
    /**
     * 创建一个分词提取器
     *
     * @param reader     输入流
     * @param bufferSize 缓冲区长度。
     *                   此长度会影响非字典分区匹配结果，使预想的结果被切分（如果预想结果长度超过缓冲区长度）。
     */
    public DefaultQueryReaderTokenizer(Reader reader, int bufferSize) {
        super(reader, bufferSize);
        this.matchers.add(new AlphaNumMatcher());
        this.matchers.add(new DecimalMatcher());
        this.matchers.add(new StandartCnMatcher());
        this.matchers.add(new UnknownMatcher());
    }

    @Override
    protected void filter() {
        List<Token> tokens = Lists.newArrayList();
        for (Token preToken : this.preTokens) {
            if (this.ctx.lastLongestWordToken != null) {
                if (this.ctx.lastLongestWordToken.getEnd() >= preToken.getEnd()) {
                    continue;
                }
            }
            switch (preToken.getType()) {
                case WORD:
                    this.ctx.lastLongestWordToken = preToken;
                    tokens.add(preToken);
                case ALPHANUM:
                    if (this.ctx.lastAlphaNumToken != null) {
                        if (this.ctx.lastAlphaNumToken.getEnd() >= this.streamOffset) {
                            // 已经被匹配了
                            continue;
                        }
                    }
                    this.ctx.lastAlphaNumToken = preToken;
                    if (NumberUtils.isNumber(preToken.getValue())) {
                        if (this.ctx.lastDecimalToken != null) {
                            if (this.ctx.lastDecimalToken.getEnd() >= preToken.getEnd()) {
                                // 已经被匹配了
                                continue;
                            }
                        }
                        this.ctx.lastDecimalToken = preToken;
                    }
                    tokens.add(preToken);
                case DECIMAL:
                    if (this.ctx.lastDecimalToken != null) {
                        if (this.ctx.lastDecimalToken.getEnd() >= this.streamOffset) {
                            // 已经被匹配了
                            continue;
                        }
                    }
                    this.ctx.lastDecimalToken = preToken;
                    tokens.add(preToken);
                case CN:
                    if (this.ctx.lastAlphaNumToken != null) {
                        if (this.ctx.lastAlphaNumToken.getEnd() >= this.streamOffset) {
                            // 已经被匹配了
                            continue;
                        }
                    }
                    if (this.ctx.lastDecimalToken != null) {
                        if (this.ctx.lastDecimalToken.getEnd() >= this.streamOffset) {
                            // 已经被匹配了
                            continue;
                        }
                    }
                    if (this.ctx.lastLongestWordToken != null) {
                        if (this.ctx.lastLongestWordToken.getEnd() >= this.streamOffset) {
                            // 已经属性一个字典词
                            continue;
                        }
                    }
                    tokens.add(preToken);
                default:
                    tokens.add(preToken);
            }
        }
        this.preTokens.clear();
        this.preTokens.addAll(tokens);
    }
}
