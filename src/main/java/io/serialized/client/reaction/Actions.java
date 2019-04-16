package io.serialized.client.reaction;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.serialized.client.reaction.ReactionDefinition.Action.newAction;
import static java.util.Collections.emptyMap;

public class Actions {

  public static HttpActionBuilder httpAction(URI targetUri) {
    return new HttpActionBuilder(targetUri);
  }

  public static SlackActionBuilder slackAction(URI targetUri) {
    return new SlackActionBuilder(targetUri);
  }

  public static class HttpActionBuilder {

    private final URI targetUri;
    private final Map<String, Object> httpHeaders = new LinkedHashMap<>();

    HttpActionBuilder(URI targetUri) {
      this.targetUri = targetUri;
    }

    public HttpActionBuilder addHeader(String key, String value) {
      this.httpHeaders.put(key, value);
      return this;
    }

    public HttpActionBuilder addHeaders(Map<String, String> headers) {
      this.httpHeaders.putAll(headers);
      return this;
    }

    public ReactionDefinition.Action build() {
      return newAction("HTTP_POST", targetUri, httpHeaders, null);
    }
  }

  public static class SlackActionBuilder {

    private final URI webHookUrl;
    private String text;

    SlackActionBuilder(URI webHookUrl) {
      this.webHookUrl = webHookUrl;
    }

    public SlackActionBuilder text(String text) {
      this.text = text;
      return this;
    }

    public ReactionDefinition.Action build() {
      return newAction("SLACK_POST", webHookUrl, emptyMap(), text);
    }
  }

}
