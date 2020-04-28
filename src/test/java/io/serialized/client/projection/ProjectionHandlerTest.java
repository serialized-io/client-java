package io.serialized.client.projection;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static io.serialized.client.projection.Functions.add;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ProjectionHandlerTest {

  @Test
  public void testCannotAddFunctionIfFunctionUriIsPresent() {
    ProjectionHandler.Builder builder = new ProjectionHandler.Builder("myType");
    URI functionUri = URI.create("https://example.com");

    builder.withFunctionUri(functionUri);

    try {
      builder.addFunction(add().build());
      fail("Illegal combination");
    } catch (Exception e) {
      // expected
    }

    assertThat(builder.build().functionUri()).isEqualTo(functionUri);
  }

  @Test
  public void testCannotAddFunctionUriIfFunctionsArePresent() {
    ProjectionHandler.Builder builder = new ProjectionHandler.Builder("myType");

    builder.addFunction(add().build());

    try {
      builder.withFunctionUri(URI.create("https://example.com"));
      fail("Illegal combination");
    } catch (Exception e) {
      // expected
    }

    assertThat(builder.build().functions().iterator().next().function()).isEqualTo("add");
  }

}
