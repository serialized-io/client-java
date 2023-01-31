package io.serialized.client.projection.query;

import org.apache.commons.lang3.Validate;

public class SearchString {

  public final String string;

  private SearchString(String string) {
    this.string = string;
  }

  public static SearchString string(String string) {
    Validate.notBlank(string, "Search string cannot be empty");
    return new SearchString(string);
  }

}
