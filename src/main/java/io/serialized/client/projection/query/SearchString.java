package io.serialized.client.projection.query;

public class SearchString {

  public final String string;

  private SearchString(String string) {
    this.string = string;
  }

  public static SearchString exact(String string) {
    return new SearchString(string);
  }

  public static SearchString startsWith(String string) {
    return new SearchString(string + "*");
  }

}
