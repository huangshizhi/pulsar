package fun.platonic.pulsar.boilerpipe.extractors;

import fun.platonic.pulsar.boilerpipe.document.TextDocument;
import fun.platonic.pulsar.boilerpipe.filters.simple.MinClauseWordsFilter;
import fun.platonic.pulsar.boilerpipe.filters.simple.SplitParagraphBlocksFilter;
import fun.platonic.pulsar.boilerpipe.utils.ProcessingException;

/**
 * A full-text extractor which is tuned towards extracting sentences from news articles.
 */
public final class ArticleSentencesExtractor implements TextExtractor {
  public static final ArticleSentencesExtractor INSTANCE = new ArticleSentencesExtractor();

  /**
   * Returns the singleton instance for {@link ArticleSentencesExtractor}.
   */
  public static ArticleSentencesExtractor getInstance() {
    return INSTANCE;
  }

  public boolean process(TextDocument doc) throws ProcessingException {
    return

        ArticleExtractor.INSTANCE.process(doc) | SplitParagraphBlocksFilter.INSTANCE.process(doc)
            | MinClauseWordsFilter.INSTANCE.process(doc);
  }

}
