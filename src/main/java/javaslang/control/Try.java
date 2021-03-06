/*     / \____  _    ______   _____ / \____   ____  _____
 *    /  \__  \/ \  / \__  \ /  __//  \__  \ /    \/ __  \   Javaslang
 *  _/  // _\  \  \/  / _\  \\_  \/  // _\  \  /\  \__/  /   Copyright 2014-2015 Daniel Dietrich
 * /___/ \_____/\____/\_____/____/\___\_____/_/  \_/____/    Licensed under the Apache License, Version 2.0
 */
package javaslang.control;

import javaslang.algebra.CheckedMonad;
import javaslang.Kind;
import javaslang.control.Valences.Bivalent;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An implementation similar to Scala's Try control.
 *
 * @param <T> Value type in the case of success.
 */
public interface Try<T> extends Kind<Try<?>, T>, CheckedMonad<Try<?>, T>, Bivalent<T, Throwable>, Iterable<T> {

    /**
     * Creates a Try of a CheckedSupplier.
     *
     * @param supplier A checked supplier
     * @param <T>      Component type
     * @return {@code Success(supplier.get())} if no exception occurs, otherwise {@code Failure(throwable)} if an
     * exception occurs calling {@code supplier.get()}.
     */
    static <T> Try<T> of(CheckedSupplier<T> supplier) {
        try {
            return new Success<>(supplier.get());
        } catch (Throwable t) {
            return new Failure<>(t);
        }
    }

    /**
     * Creates a Try of a CheckedRunnable.
     *
     * @param runnable A checked runnable
     * @return {@code Success(null)} if no exception occurs, otherwise {@code Failure(throwable)} if an exception occurs
     * calling {@code runnable.run()}.
     */
    static Try<Void> run(CheckedRunnable runnable) {
        try {
            runnable.run();
            return new Success<>(null); // null represents the absence of an value, i.e. Void
        } catch (Throwable t) {
            return new Failure<>(t);
        }
    }

    /**
     * Checks if this is a Failure.
     *
     * @return true, if this is a Failure, otherwise false, if this is a Success
     */
    boolean isFailure();

    /**
     * Checks if this is a Success.
     *
     * @return true, if this is a Success, otherwise false, if this is a Failure
     */
    boolean isSuccess();

    /**
     * Returns {@code this}, if this is a Success, otherwise tries to recover the exception of the failure with {@code f},
     * i.e. calling {@code Try.of(() -> f.apply(throwable))}.
     *
     * @param f A recovery function taking a Throwable
     * @return a new Try
     */
    Try<T> recover(CheckedFunction<Throwable, ? extends T> f);

    /**
     * Returns {@code this}, if this is a Success, otherwise tries to recover the exception of the failure with {@code f},
     * i.e. calling {@code f.apply(cause.getCause())}. If an error occurs recovering a Failure, then the new Failure is
     * returned.
     *
     * @param f A recovery function taking a Throwable
     * @return a new Try
     */
    Try<T> recoverWith(CheckedFunction<Throwable, Try<T>> f);

    /**
     * Returns {@code Success(throwable)} if this is a {@code Failure(throwable)}, otherwise
     * a {@code Failure(new UnsupportedOperationException("Success.failed()"))} if this is a Success.
     *
     * @return a new Try
     */
    Try<Throwable> failed();

    /**
     * Consumes the throwable if this is a Failure, otherwise returns this Success.
     *
     * @param f A Consumer
     * @return a new Failure, if this is a Failure and the consumer throws, otherwise this, which may be a Success or
     * a Failure.
     */
    Try<T> onFailure(CheckedConsumer<Throwable> f);

    /**
     * <p>Returns {@code this} if this is a Failure or this is a Success and the value satisfies the predicate.</p>
     * <p>Returns a new Failure, if this is a Success and the value does not satisfy the Predicate or an exception
     * occurs testing the predicate.</p>
     *
     * @param predicate A predicate
     * @return a new Try
     */
    Try<T> filter(CheckedPredicate<? super T> predicate);

    /**
     * Flattens a nested, monadic structure using a function.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Match&lt;Try&lt;U&gt;&gt; f = Match
     *    .when((Try&lt;U&gt; o) -&gt; o)
     *    .when((U u) -&gt; new Success&lt;&gt;(u));
     * new Success&lt;&gt;(1).flatten(f);                                  // = Success(1)
     * new Success&lt;&gt;(new Success&lt;&gt;(1)).flatten(f);             // = Success(1)
     * new Success&lt;&gt;(new Failure&lt;&gt;(new Error(""))).flatten(f); // = Failure("Error")
     * new Failure&lt;&gt;(new Error("")).flatten(f);                      // = Failure("Error")
     * </code>
     * </pre>
     *
     * @param <U>   component type of the result {@code Try}
     * @param f     a function which maps elements of this {@code Try} to {@code Try}s
     * @return a new {@code Try}
     * @throws NullPointerException if {@code f} is null
     */
    @SuppressWarnings("unchecked")
    @Override
    default <U> Try<U> flatten(CheckedFunction<? super T, ? extends Kind<Try<?>, U>> f) {
        Objects.requireNonNull(f, "f is null");
        if (isFailure()) {
            return (Failure<U>) this;
        } else {
            try {
                return (Try<U>) f.apply(get());
            } catch (Throwable t) {
                return new Failure<>(t);
            }
        }
    }

    @Override
    default boolean exists(CheckedPredicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        try {
            return isSuccess() && predicate.test(get());
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    default boolean forAll(CheckedPredicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        try {
            return isSuccess() && predicate.test(get());
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Applies the action to the value of a Success or does nothing in the case of a Failure.
     *
     * @param action A Consumer
     */
    @Override
    void forEach(Consumer<? super T> action);

    /**
     * Applies the action to the value of a Success or does nothing in the case of a Failure.
     *
     * @param action A Consumer
     * @return this Try
     */
    @Override
    Try<T> peek(CheckedConsumer<? super T> action);

    /**
     * Maps the value of a Success or returns a Failure.
     *
     * @param mapper A mapper
     * @param <U>    The new component type
     * @return a new Try
     */
    @Override
    <U> Try<U> map(CheckedFunction<? super T, ? extends U> mapper);

    /**
     * FlatMaps the value of a Success or returns a Failure.
     *
     * @param mapper A mapper
     * @param <U>    The new component type
     * @return a new Try
     */
    @Override
    <U> Try<U> flatMap(CheckedFunction<? super T, ? extends Kind<Try<?>, U>> mapper);

    @Override
    default Iterator<T> iterator() {
        if (isSuccess()) {
            return Collections.singleton(get()).iterator();
        } else {
            return Collections.emptyIterator();
        }
    }

    /**
     * Runs the given runnable if this is a {@code Success}, otherwise returns this {@code Failure}.
     * Shorthand for {@code flatMap(ignored -> Try.run(runnable))}.
     * The main use case is chaining runnables using method references:
     *
     * <pre>
     * <code>
     * Try.run(A::methodRef).andThen(B::methodRef).andThen(C::methodRef);
     * </code>
     * </pre>
     *
     * Please note that these lines are semantically the same:
     *
     * <pre>
     * <code>
     * Try.run(() -&gt; { doStuff(); })
     *    .andThen(() -&gt; { doMoreStuff(); })
     *    .andThen(() -&gt; { doEvenMoreStuff(); });
     *
     * Try.run(() -&gt; {
     *     doStuff();
     *     doMoreStuff();
     *     doEvenMoreStuff();
     * });
     * </code>
     * </pre>
     *
     * @param runnable A checked runnable
     * @return a new {@code Try}
     */
    default Try<Void> andThen(CheckedRunnable runnable) {
        return flatMap(ignored -> Try.run(runnable));
    }

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();

    /**
     * A {@linkplain java.util.function.Function} which may throw.
     *
     * @param <T> the type of the input of the operation
     * @param <R> the type of results supplied by this supplier
     */
    @FunctionalInterface
    interface CheckedFunction<T, R> {

        /**
         * Applies this function to the given argument.
         *
         * @param t the function argument
         * @return the function result
         * @throws Throwable if an error occurs
         */
        R apply(T t) throws Throwable;

        /**
         * Creates the checked identity function.
         *
         * @param <T> the type of the input of the operation
         * @return The identity function
         */
        static <T> CheckedFunction<T, T> identity() {
            return t -> t;
        }
    }

    /**
     * A {@linkplain java.util.function.Consumer} which may throw.
     *
     * @param <T> the type of the input of the operation
     */
    @FunctionalInterface
    interface CheckedConsumer<T> {

        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         * @throws Throwable if an error occurs
         */
        void accept(T t) throws Throwable;
    }

    /**
     * A {@linkplain java.util.function.Predicate} which may throw.
     *
     * @param <T> the type of the input of the predicate
     */
    @FunctionalInterface
    interface CheckedPredicate<T> {

        /**
         * Evaluates this predicate on the given argument.
         *
         * @param t the input argument
         * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
         * @throws Throwable if an error occurs
         */
        boolean test(T t) throws Throwable;
    }

    /**
     * A {@linkplain java.lang.Runnable} which may throw.
     */
    @FunctionalInterface
    interface CheckedRunnable {

        /**
         * Performs side-effects.
         *
         * @throws Throwable if an error occurs
         */
        void run() throws Throwable;
    }

    /**
     * A {@linkplain java.util.function.Supplier} which may throw.
     *
     * @param <R> the type of results supplied by this supplier
     */
    @FunctionalInterface
    interface CheckedSupplier<R> {

        /**
         * Gets a result.
         *
         * @return a result
         * @throws Throwable if an error occurs
         */
        R get() throws Throwable;
    }
}
