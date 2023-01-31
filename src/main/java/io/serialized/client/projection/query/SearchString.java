package io.serialized.client.projection.query;

import org.apache.commons.lang3.Validate;

public class SearchString {

  public final String string;

  private SearchString(String string) {
    this.string = string;
  }

  /**
   * <pre>
   * '+' specifies AND operation: token1+token2
   * '|' specifies OR operation: token1|token2
   * '-' negates a single token: -token0
   * '"' creates phrases of terms: "term1 term2 ..."
   * '*' at the end of terms specifies prefix query: term*
   * '(' and '' specifies precedence: token1 + (token2 | token3)
   * '~N' at the end of terms specifies fuzzy query: term~1
   * '~N' at the end of phrases specifies near/slop query: "term1 term2"~5
   * </pre>
   */
  public static SearchString string(String string) {
    Validate.notBlank(string, "Search string cannot be empty");
    return new SearchString(string);
  }

}
