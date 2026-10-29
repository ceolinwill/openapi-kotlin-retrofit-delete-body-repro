# openapi-generator: kotlin `jvm-retrofit2` DELETE with a request body fails at runtime

For a DELETE operation with a `requestBody`, the `kotlin` generator with `library = jvm-retrofit2` emits `@DELETE("path")` plus an `@Body` parameter. The code compiles, but Retrofit rejects the method on the first call with `IllegalArgumentException: Non-body HTTP method cannot contain @Body.` OpenAPI 3 allows a body on DELETE, so the generated client can't call this operation at all.

## Reproduction

Needs a JDK 17 (the Gradle wrapper is included).

```sh
./gradlew run -q
```

The spec is [`openapi.yaml`](openapi.yaml): one `DELETE /items` with a required JSON body. [`Main.kt`](src/main/kotlin/Main.kt) calls it against MockWebServer.

Generated code (`build/generated/src/main/kotlin/repro/client/apis/DefaultApi.kt`):

```kotlin
@DELETE("items")
suspend fun deleteItems(@Body deleteItemsRequest: DeleteItemsRequest): Response<Unit>
```

Output:

```
Exception in thread "main" java.lang.IllegalArgumentException: Non-body HTTP method cannot contain @Body.
    for method DefaultApi.deleteItems
	at retrofit2.Utils.methodError(Utils.java:56)
	at retrofit2.Utils.methodError(Utils.java:44)
	at retrofit2.RequestFactory$Builder.build(RequestFactory.java:220)
	at retrofit2.RequestFactory.parseAnnotations(RequestFactory.java:67)
	at retrofit2.ServiceMethod.parseAnnotations(ServiceMethod.java:26)
	at retrofit2.Retrofit.loadServiceMethod(Retrofit.java:235)
	at retrofit2.Retrofit$1.invoke(Retrofit.java:177)
	at jdk.proxy1/jdk.proxy1.$Proxy0.deleteItems(Unknown Source)
	at MainKt$main$1.invokeSuspend(Main.kt:14)
	...
```

With the suggested template change ([`fix/libraries/jvm-retrofit2/api.mustache`](fix/libraries/jvm-retrofit2/api.mustache), the 7.26.0 template with one line changed):

```sh
./gradlew run -q -PwithFix
```

```
DELETE /items {"ids":["a","b"]}
```

## Expected

The generated method sends the DELETE with its JSON body. Retrofit's documented way is `@HTTP(method = "DELETE", path = "items", hasBody = true)`.

## Actual

`kotlin-client/libraries/jvm-retrofit2/api.mustache` always emits `@{{httpMethod}}("{{{path}}}")` (line 158 in v7.26.0 and on master). Retrofit's `RequestFactory.Builder.build` throws when a method annotated `@DELETE` (or `@GET`, `@HEAD`, `@OPTIONS`) has an `@Body` parameter.

Suggested change, on that line:

```mustache
{{#bodyParam}}@HTTP(method = "{{httpMethod}}", path = "{{{path}}}", hasBody = true){{/bodyParam}}{{^bodyParam}}@{{httpMethod}}("{{{path}}}"){{/bodyParam}}
```

## Environment

- openapi-generator Gradle plugin 7.26.0 (latest release), generator `kotlin`, library `jvm-retrofit2`, `serializationLibrary=kotlinx_serialization`, `useCoroutines=true`
- Retrofit 3.0.0, OkHttp 5.5.1, Kotlin 2.4.21, kotlinx.serialization 1.12.0, kotlinx.coroutines 1.11.0
- Gradle 9.8.2, Temurin JDK 17, macOS 27.2 (arm64)
