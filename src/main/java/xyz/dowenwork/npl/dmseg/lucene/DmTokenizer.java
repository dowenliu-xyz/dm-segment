package xyz.dowenwork.npl.dmseg.lucene;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.util.AttributeFactory;
import xyz.dowenwork.npl.dmseg.core.Segmenter;
import xyz.dowenwork.npl.dmseg.core.SegmenterHolder;
import xyz.dowenwork.npl.dmseg.core.Token;

import java.io.IOException;
import java.util.Iterator;

/**
 * Lucene Tokenizer封装
 * <p>create at 16-2-24</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public class DmTokenizer extends Tokenizer {
    private final CharTermAttribute charTermAttribute;
    private final OffsetAttribute offsetAttribute;
    private final PositionIncrementAttribute positionIncrementAttribute;
    private final PositionLengthAttribute positionLengthAttribute;
    private final TypeAttribute typeAttribute;
    private Iterator<Token> tokenIterator;
    private final DmTokenizerType type;
    private final SegmenterHolder segmenterHolder;

    public DmTokenizer(AttributeFactory factory, DmTokenizerType type, SegmenterHolder segmenterHolder) {
        super(factory);
        this.charTermAttribute = addAttribute(CharTermAttribute.class);
        this.offsetAttribute = addAttribute(OffsetAttribute.class);
        this.positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
        this.positionLengthAttribute = addAttribute(PositionLengthAttribute.class);
        this.typeAttribute = addAttribute(TypeAttribute.class);
        this.type = type;
        this.segmenterHolder = segmenterHolder;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (this.tokenIterator == null) {
            Segmenter segmenter = this.segmenterHolder.getSegmenter();
            if (segmenter == null) {
                return false;
            }
            this.tokenIterator = this.type.iterate(segmenter, this.input);
        }
        if (this.tokenIterator.hasNext()) {
            Token token = this.tokenIterator.next();
            this.charTermAttribute.setEmpty();
            this.charTermAttribute.append(token.getValue());
            this.offsetAttribute.setOffset(token.getOffset(), token.getEnd());
            this.positionIncrementAttribute.setPositionIncrement(token.getPositionIncrement());
            this.positionLengthAttribute.setPositionLength(1);
            this.typeAttribute.setType(token.getType().name().toLowerCase());
            return true;
        }
        this.tokenIterator = null;
        return false;
    }
}
