package io.serialized.client.reaction;

import org.apache.commons.lang3.Validate;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static io.serialized.client.reaction.ReactionDefinition.Action.newAction;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;

public class Actions {

  public static HttpActionBuilder httpAction(URI targetUri) {
    return new HttpActionBuilder(targetUri);
  }

  public static SlackActionBuilder slackAction(URI targetUri) {
    return new SlackActionBuilder(targetUri);
  }

  public static IftttActionBuilder iftttAction(URI targetUri) {
    return new IftttActionBuilder(targetUri);
  }

  public static AutomateActionBuilder automateAction(URI targetUri) {
    return new AutomateActionBuilder(targetUri);
  }

  public static ZapierActionBuilder zapierAction(URI targetUri) {
    return new ZapierActionBuilder(targetUri);
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
      return newAction("HTTP_POST", targetUri, httpHeaders, null, null);
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
      return newAction("SLACK_POST", webHookUrl, emptyMap(), text, null);
    }
  }

  public static class IftttActionBuilder {

    private final Set<String> allowedKeys = Stream.of("value1", "value2", "value3").collect(toSet());
    private final Map<String, String> valueMap = new LinkedHashMap<>();

    private final URI webHookUrl;

    IftttActionBuilder(URI webHookUrl) {
      this.webHookUrl = webHookUrl;
    }

    public IftttActionBuilder add(String key, String value) {
      Validate.isTrue(allowedKeys.contains(key), "Illegal key name: " + key);
      this.valueMap.put(key, value);
      return this;
    }

    public ReactionDefinition.Action build() {
      return newAction("IFTTT_POST", webHookUrl, emptyMap(), null, valueMap);
    }
  }

  public static class AutomateActionBuilder {

    private final Set<String> allowedKeys = Stream.of("value1", "value2", "value3", "value4", "value5", "value6", "value7", "value8", "value9").collect(toSet());
    private final Map<String, String> valueMap = new LinkedHashMap<>();

    private final URI webHookUrl;

    AutomateActionBuilder(URI webHookUrl) {
      this.webHookUrl = webHookUrl;
    }

    public AutomateActionBuilder add(String key, String value) {
      Validate.isTrue(allowedKeys.contains(key), "Illegal key name: " + key);
      this.valueMap.put(key, value);
      return this;
    }

    public ReactionDefinition.Action build() {
      return newAction("AUTOMATE_POST", webHookUrl, emptyMap(), null, valueMap);
    }
  }

  public static class ZapierActionBuilder {

    private final Set<String> allowedKeys = Stream.of("value1", "value2", "value3", "value4", "value5", "value6", "value7", "value8", "value9").collect(toSet());
    private final Map<String, String> valueMap = new LinkedHashMap<>();

    private final URI webHookUrl;

    ZapierActionBuilder(URI webHookUrl) {
      this.webHookUrl = webHookUrl;
    }

    public ZapierActionBuilder add(String key, String value) {
      Validate.isTrue(allowedKeys.contains(key), "Illegal key name: " + key);
      this.valueMap.put(key, value);
      return this;
    }

    public ReactionDefinition.Action build() {
      return newAction("ZAPIER_POST", webHookUrl, emptyMap(), null, valueMap);
    }
  }

}
