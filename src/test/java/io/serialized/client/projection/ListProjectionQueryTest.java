package io.serialized.client.projection;

import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.serialized.client.projection.query.ProjectionQueries.list;
import static org.assertj.core.api.Assertions.assertThat;

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
  public void listWithLimitAndDescSort() {
    HttpUrl httpUrl = list("game").withLimit(10).withSortDescending("startTime").build(Map.class).constructUrl(ROOT_URL);
    assertThat(httpUrl.pathSegments()).contains("projections", "single", "game");
    assertThat(httpUrl.queryParameter("limit")).isEqualTo("10");
    assertThat(httpUrl.queryParameter("sort")).isEqualTo("-startTime");
  }

}
