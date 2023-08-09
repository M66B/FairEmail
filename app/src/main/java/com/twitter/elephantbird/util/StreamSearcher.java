package com.twitter.elephantbird.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * An efficient stream searching class based on the Knuth-Morris-Pratt algorithm.
 * For more on the algorithm works see: https://www.inf.fh-flensburg.de/lang/algorithmen/pattern/kmpen.htm.
 */
public class StreamSearcher {

  protected byte[] pattern_;
  protected int[] borders_;

  // An upper bound on pattern length for searching. Results are undefined for longer patterns.
  public static final int MAX_PATTERN_LENGTH = 1024;

  public StreamSearcher(byte[] pattern) {
    setPattern(pattern);
  }

  /**
   * Sets a new pattern for this StreamSearcher to use.
   * @param pattern
   *          the pattern the StreamSearcher will look for in future calls to search(...)
   */
  public void setPattern(byte[] pattern) {
    pattern_ = Arrays.copyOf(pattern, pattern.length);
    borders_ = new int[pattern_.length + 1];
    preProcess();
  }

  /**
   * Searches for the next occurrence of the pattern in the stream, starting from the current stream position. Note
   * that the position of the stream is changed. If a match is found, the stream points to the end of the match -- i.e. the
   * byte AFTER the pattern. Else, the stream is entirely consumed. The latter is because InputStream semantics make it difficult to have
   * another reasonable default, i.e. leave the stream unchanged.
   *
   * @return bytes consumed if found, -1 otherwise.
   * @throws IOException
   */
  public long search(InputStream stream) throws IOException {
    long bytesRead = 0;

    int b;
    int j = 0;

    while ((b = stream.read()) != -1) {
      bytesRead++;

      while (j >= 0 && (byte)b != pattern_[j]) {
        j = borders_[j];
      }
      // Move to the next character in the pattern.
      ++j;

      // If we've matched up to the full pattern length, we found it.  Return,
      // which will automatically save our position in the InputStream at the point immediately
      // following the pattern match.
      if (j == pattern_.length) {
        return bytesRead;
      }
    }

    // No dice, Note that the stream is now completely consumed.
    return -1;
  }

  /**
   * Builds up a table of longest "borders" for each prefix of the pattern to find. This table is stored internally
   * and aids in implementation of the Knuth-Moore-Pratt string search.
   * <p>
   * For more information, see: https://www.inf.fh-flensburg.de/lang/algorithmen/pattern/kmpen.htm.
   */
  protected void preProcess() {
    int i = 0;
    int j = -1;
    borders_[i] = j;
    while (i < pattern_.length) {
      while (j >= 0 && pattern_[i] != pattern_[j]) {
        j = borders_[j];
      }
      borders_[++i] = ++j;
    }
  }
}
