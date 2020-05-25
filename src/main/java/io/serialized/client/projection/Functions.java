package io.serialized.client.projection;

public class Functions {

  public static Function.Builder handlerFunction(String function) {
    return new Function.Builder(function);
  }

  public static Function clearref() {
    return new Function.Builder("clearref").build();
  }

  public static Function delete() {
    return new Function.Builder("delete").build();
  }

  public static Function clear() {
    return new Function.Builder("clear").build();
  }

  public static Function.Builder setref() {
    return new Function.Builder("setref");
  }

  public static Function.Builder inc() {
    return new Function.Builder("inc");
  }

  public static Function.Builder dec() {
    return new Function.Builder("dec");
  }

  public static Function.Builder push() {
    return new Function.Builder("push");
  }

  public static Function.Builder add() {
    return new Function.Builder("add");
  }

  public static Function.Builder subtract() {
    return new Function.Builder("subtract");
  }

  public static Function.Builder set() {
    return new Function.Builder("set");
  }

  public static Function.Builder unset() {
    return new Function.Builder("unset");
  }

  public static Function.Builder merge() {
    return new Function.Builder("merge");
  }

  public static Function.Builder remove() {
    return new Function.Builder("remove");
  }

  public static Function.Builder append() {
    return new Function.Builder("append");
  }

  public static Function.Builder prepend() {
    return new Function.Builder("prepend");
  }

}
