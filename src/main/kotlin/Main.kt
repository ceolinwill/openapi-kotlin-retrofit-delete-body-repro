import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import repro.client.apis.DefaultApi
import repro.client.infrastructure.ApiClient
import repro.client.models.DeleteItemsRequest

fun main() = runBlocking {
    MockWebServer().use { server ->
        server.enqueue(MockResponse.Builder().code(204).build())
        server.start()

        val api = ApiClient(baseUrl = server.url("/").toString()).createService(DefaultApi::class.java)
        api.deleteItems(DeleteItemsRequest(ids = listOf("a", "b")))

        val request = server.takeRequest()
        println("${request.method} ${request.url.encodedPath} ${request.body?.utf8()}")
    }
}
