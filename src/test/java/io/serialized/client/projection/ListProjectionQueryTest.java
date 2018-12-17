package io.serialized.client.projection;

import okhttp3.HttpUrl;
import org.junit.Test;

import java.util.Map;

import static io.serialized.client.projection.query.ProjectionQueries.list;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ListProjectionQueryTest {

  private static final HttpUrl ROOT_URL = HttpUrl.get("https://api.serialized.io");

  @Test
  public void listSimple() {
    HttpUrl httpUrl = list("game").build(Map.class).constructUrl(ROOT_URL);
    assertThat(httpUrl.pathSegments(), hasItems("projections", "single", "game"));
    assertThat(httpUrl.queryParameter("limit"), nullValue());
    assertThat(httpUrl.queryParameter("sort"), nullValue());
  }

  @Test
  public void listWithLimit() {
    HttpUrl httpUrl = list("game").limit(10).build(Map.class).constructUrl(ROOT_URL);
    assertThat(httpUrl.pathSegments(), hasItems("projections", "single", "game"));
    assertThat(httpUrl.queryParameter("limit"), is("10"));
    assertThat(httpUrl.queryParameter("sort"), nullValue());
  }

  @Test
  public void listWithLimitAndDescSort() {
    HttpUrl httpUrl = list("game").limit(10).sortDescending("startTime").build(Map.class).constructUrl(ROOT_URL);
    assertThat(httpUrl.pathSegments(), hasItems("projections", "single", "game"));
    assertThat(httpUrl.queryParameter("limit"), is("10"));
    assertThat(httpUrl.queryParameter("sort"), is("-startTime"));
  }

}