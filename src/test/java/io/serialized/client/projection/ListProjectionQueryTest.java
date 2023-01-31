package io.serialized.client.projection;

import com.google.common.collect.ImmutableList;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.serialized.client.projection.query.ProjectionQueries.list;
import static io.serialized.client.projection.query.ProjectionQueries.search;
import static io.serialized.client.projection.query.SearchString.string;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ListProjectionQueryTest {

  private static final HttpUrl ROOT_URL = HttpUrl.get("https://api.serialized.io");

  @Test
  public void listSimple() {
    HttpUrl httpUrl = list("game").build(Map.class).constructUrl(ROOT_URL);
    assertThat(httpUrl.pathSegments()).contains("projections", "single", "game");
    assertThat(httpUrl.queryParameter("limit")).isNull();
    assertThat(httpUrl.queryParameter("sort")).isNull();
  }

  @Test
  public void listWithLimit() {
    HttpUrl httpUrl = list("game").withLimit(10).build(Map.class).constructUrl(ROOT_URL);
    assertThat(httpUrl.pathSegments()).contains("projections", "single", "game");
    assertThat(httpUrl.queryParameter("limit")).isEqualTo("10");
    assertThat(httpUrl.queryParameter("sort")).isNull();
  }

  @Test
  public void listWithSkip() {
    HttpUrl httpUrl = list("game").withSkip(5).build(Map.class).constructUrl(ROOT_URL);
    assertThat(httpUrl.pathSegments()).contains("projections", "single", "game");
    assertThat(httpUrl.queryParameter("skip")).isEqualTo("5");
    assertThat(httpUrl.queryParameter("sort")).isNull();
  }

  @Test
  public void listWithIds() {
    ImmutableList<String> ids = ImmutableList.of("a", "b", "c");
    HttpUrl httpUrl = list("game").withIds(ids).build(Map.class).constructUrl(ROOT_URL);
    assertThat(httpUrl.pathSegments()).contains("projections", "single", "game");
    assertThat(httpUrl.queryParameterValues("id")).isEqualTo(ids);
  }

  @Test
  public void listWithLimitAndDescSort() {
    HttpUrl httpUrl = list("game").withLimit(10).withSortDescending("startTime").build(Map.class).constructUrl(ROOT_URL);
    assertThat(httpUrl.pathSegments()).contains("projections", "single", "game");
    assertThat(httpUrl.queryParameter("limit")).isEqualTo("10");
    assertThat(httpUrl.queryParameter("sort")).isEqualTo("-startTime");
  }

  @Test
  public void searchForString() {
    HttpUrl httpUrl = search("game", string("test")).build(Map.class).constructUrl(ROOT_URL);
    assertThat(httpUrl.pathSegments()).contains("projections", "single", "game");
    assertThat(httpUrl.queryParameter("search")).isEqualTo("test");
  }

  @Test
  public void searchForEmptyStringNotAllowed() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
        search("game", string("")).build(Map.class).constructUrl(ROOT_URL)
    );
    assertThat(exception.getMessage()).isEqualTo("Search string cannot be empty");
  }

}
