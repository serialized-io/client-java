package io.serialized.client.projection;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ProjectionHandlerTest {

  @Test
  public void testCannotAddFunctionIfFunctionUriIsPresent() {
    ProjectionHandler.Builder builder = new ProjectionHandler.Builder("myType");
    URI functionUri = URI.create("https://example.com");

    builder.withFunctionUri(functionUri);

    try {
      builder.addFunction(Function.add().build());
      fail("Illegal combination");
    } catch (Exception e) {
      // expected
    }

    assertThat(builder.build().getFunctionUri(), is(functionUri));
  }

  @Test
  public void testCannotAddFunctionUriIfFunctionsArePresent() {
    ProjectionHandler.Builder builder = new ProjectionHandler.Builder("myType");

    builder.addFunction(Function.add().build());

    try {
      builder.withFunctionUri(URI.create("https://example.com"));
      fail("Illegal combination");
    } catch (Exception e) {
      // expected
    }

    assertThat(builder.build().getFunctions().iterator().next().getFunction(), is("add"));
  }

}