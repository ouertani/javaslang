/*     / \____  _    ______   _____ / \____   ____  _____
 *    /  \__  \/ \  / \__  \ /  __//  \__  \ /    \/ __  \   Javaslang
 *  _/  // _\  \  \/  / _\  \\_  \/  // _\  \  /\  \__/  /   Copyright 2014-2015 Daniel Dietrich
 * /___/ \_____/\____/\_____/____/\___\_____/_/  \_/____/    Licensed under the Apache License, Version 2.0
 */
package javaslang.algebra;

/*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
   G E N E R A T O R   C R A F T E D
\*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/

import java.util.function.Consumer;
import javaslang.Kind;
import javaslang.control.Try.CheckedConsumer;
import javaslang.control.Try.CheckedFunction;
import javaslang.control.Try.CheckedPredicate;

/**
 * Defines a CheckedMonad by generalizing the flatMap function.
 * <p>
 * All instances of the CheckedMonad interface should obey the three control laws:
 * <ul>
 *     <li><strong>Left identity:</strong> {@code unit(a).flatMap(f) ≡ f a}</li>
 *     <li><strong>Right identity:</strong> {@code m.flatMap(unit) ≡ m}</li>
 *     <li><strong>Associativity:</strong> {@code m.flatMap(f).flatMap(g) ≡ m.flatMap(x -> f.apply(x).flatMap(g)}</li>
 * </ul>
 * given
 * <ul>
 * <li>an object {@code m} of type {@code Kind<M, T>}</li>
 * <li>an object {@code a} of type T</li>
 * <li>a constructor {@code unit} taking an {@code a} and producing an object of type {@code M}</li>
 * <li>a function {@code f: T → M}
 * </ul>
 *
 * To read further about monads in Java please refer to
 * <a href="http://java.dzone.com/articles/whats-wrong-java-8-part-iv">What's Wrong in Java 8, Part IV: Monads</a>.
 *
 * @param <M> placeholder for the type that implements this
 * @param <T> component type of this checked monad
 * @since 1.1.0
 */
public interface CheckedMonad<M extends CheckedMonad<M, ?>, T> extends Kind<M, T>, CheckedFunctor<T> {

    /**
     * Returns the result of applying f to M's value of type T and returns a new M with value of type U.
     *
     * @param <U> component type of the resulting monad
     * @param mapper a checked function that maps the monad value to a new monad instance
     * @return a new CheckedMonad instance of component type U and container type M
     * @throws NullPointerException if {@code mapper} is null
     */
    <U> CheckedMonad<M, U> flatMap(CheckedFunction<? super T, ? extends Kind<M, U>> mapper);

    /**
     * Flattens a nested, monadic structure using a function.
     * <p>
     * A vivid example showing a simple container type:
     * <pre>
     * <code>
     * // given a monad M&lt;T&gt;
     * [a,[b,c],d].flatten( Match
     *    .when((M m) -&gt; m)
     *    .when((T t) -&gt; new M(t))
     * ) = [a,b,c,d]
     * </code>
     * </pre>
     *
     * @param <U> component type of the resulting {@code Monad}
     * @param f a function which maps elements of this monad to monads of the same kind
     * @return A monadic structure containing flattened elements.
     * @throws NullPointerException if {@code f} is null
     */
    <U> CheckedMonad<M, U> flatten(CheckedFunction<? super T, ? extends Kind<M, U>> f);

    /**
     * Checks, if an element exists such that the predicate holds.
     *
     * @param predicate A Predicate
     * @return true, if predicate holds for an element of this, false otherwise
     * @throws NullPointerException if {@code predicate} is null
     */
    boolean exists(CheckedPredicate<? super T> predicate);

    /**
     * Checks, if the given predicate holds for all elements of this.
     *
     * @param predicate A Predicate
     * @return true, if the predicate holds for all elements of this, false otherwise
     * @throws NullPointerException if {@code predicate} is null
     */
    boolean forAll(CheckedPredicate<? super T> predicate);

    /**
     * Performs an action on each element of this monad.
     *
     * @param action A {@code Consumer}
     * @throws NullPointerException if {@code action} is null
     */
    void forEach(Consumer<? super T> action);

    /**
     * Performs an action on each element of this monad.
     * @param action A {@code Consumer}
     * @return An instance of this monad type
     * @throws NullPointerException if {@code action} is null
     */
    CheckedMonad<M, T> peek(CheckedConsumer<? super T> action);

    @Override
    <U> CheckedMonad<M, U> map(CheckedFunction<? super T, ? extends U> mapper);
}