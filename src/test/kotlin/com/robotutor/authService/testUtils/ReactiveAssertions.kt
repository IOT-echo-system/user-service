package com.robotutor.authService.testUtils

import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.test.StepVerifierOptions
import reactor.util.context.Context

fun <T> assertWithContext(producer: Mono<T>, context: Context, assertions: () -> Unit) {
    fun subscriberContext(context: Context) =
        StepVerifierOptions.create().withInitialContext(context)

    StepVerifier.create(producer, subscriberContext(context))
        .then(assertions)
        .verifyComplete()
}

fun <T> assertNextWithContext(producer: Mono<T>, context: Context, assertions: ((t: T) -> Unit)) {
    fun subscriberContext(context: Context) =
        StepVerifierOptions.create().withInitialContext(context)

    StepVerifier.create(producer, subscriberContext(context))
        .consumeNextWith(assertions)
        .verifyComplete()
}

fun <T> assertNextWith(responseMono: Mono<T>, assertions: (t: T) -> Unit) {
    StepVerifier.create(responseMono)
        .consumeNextWith(assertions)
        .verifyComplete()
}

fun <T> assertErrorWith(responseMono: Mono<T>, assertions: (t: Throwable) -> Unit) {
    StepVerifier.create(responseMono)
        .consumeErrorWith(assertions)
        .verify()
}
