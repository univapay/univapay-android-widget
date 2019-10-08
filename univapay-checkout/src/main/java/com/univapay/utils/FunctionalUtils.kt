package com.univapay.utils

/**
 * Custom implementation of the functional <code>Either</code> pattern.
 */
object FunctionalUtils {

    sealed class Either<out A, out B>{
        class Left<A>(val value: A): Either<A, Nothing>()
        class Right<B>(val value: B): Either<Nothing, B>()
    }

    val noop: (v: Any)-> Unit = {}
}
