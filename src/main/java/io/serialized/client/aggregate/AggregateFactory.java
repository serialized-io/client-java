package io.serialized.client.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class AggregateFactory<A, T> {

  private final StateBuilder<T> stateBuilder;
  private final Function<T, A> initializer;

  public AggregateFactory(Function<T, A> initializer, StateBuilder<T> stateBuilder) {
    this.stateBuilder = stateBuilder;
    this.initializer = initializer;
  }

  public A fromCommands(List<Command<A>> commands) {
    List<Event<?>> events = new ArrayList<>();
    for (Command<A> command : commands) {
      T aggregateState = stateBuilder.buildState(events);
      A a = initializer.apply(aggregateState);
      List<Event<?>> apply = command.apply(a);
      events.addAll(apply);
    }
    T lastState = stateBuilder.buildState(events);
    return initializer.apply(lastState);
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

  public static <A, T> AggregateFactory<A, T> newFactory(Function<T, A> initializer, StateBuilder<T> stateBuilder) {
    return new AggregateFactory<>(initializer, stateBuilder);
  }

}


