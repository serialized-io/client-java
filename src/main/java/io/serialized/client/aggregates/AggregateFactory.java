package io.serialized.client.aggregates;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class AggregateFactory<A, T> {

  private final StateBuilder<T> stateBuilder;
  private final Function<T, A> initializer;

  public AggregateFactory(Builder<A, T> builder) {
    this.stateBuilder = builder.stateBuilder;
    this.initializer = builder.initializer;
  }

  public A fromCommands(List<Command<A>> commands) {
    List<Event> events = new ArrayList<>();
    for (Command<A> command : commands) {
      State<T> aggregateState = stateBuilder.buildState(events, 0);
      A a = initializer.apply(aggregateState.data());
      List<Event> apply = command.apply(a);
      events.addAll(apply);
    }
    State<T> lastState = stateBuilder.buildState(events, 0);
    return initializer.apply(lastState.data());
  }

  /**
   * Builds an aggregate root instance from a number of commands, useful for scenario testing.
   *
   * @param commands the commands to apply to build the aggregate root
   * @return aggregate root with the state from applying all commands in sequence.
   */
  @SafeVarargs
  public final A fromCommands(Command<A>... commands) {
    return fromCommands(asList(commands));
  }

  public static <A, T> Builder<A, T> newFactory(Class<A> aggregateClass, StateBuilder<T> stateBuilder) {
    return new Builder<>(aggregateClass, stateBuilder);
  }

  public static class Builder<A, T> {

    private Class<A> aggregateClass;
    private final StateBuilder<T> stateBuilder;
    private Function<T, A> initializer;

    public Builder(Class<A> aggregateClass, StateBuilder<T> stateBuilder) {
      this.aggregateClass = aggregateClass;
      this.stateBuilder = stateBuilder;
    }

    Builder<A, T> initializer(Function<T, A> initializer) {
      this.initializer = initializer;
      return this;
    }

    public AggregateFactory<A, T> build() {
      return new AggregateFactory<>(this);
    }
  }

}
