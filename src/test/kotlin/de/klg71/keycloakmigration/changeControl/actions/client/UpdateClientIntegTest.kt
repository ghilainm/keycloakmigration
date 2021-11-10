package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientById
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class UpdateClientIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testUpdateClient() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val redirectUris = listOf("http://localhost", "http://localhost1")
        val name = "newShinyName"
        UpdateClientAction(testRealm, "simpleClient", name = name, redirectUris = redirectUris).executeIt()

        assertThat(client.clientById("simpleClient", testRealm).name).isEqualTo(name)
        assertThat(client.clientById("simpleClient", testRealm).redirectUris).isEqualTo(redirectUris)
    }

    @Test
    fun testUpdateClient_clientDoesNotExist() {
        val redirectUris = listOf("http://localhost", "http://localhost1")
        val name = "newShinyName"

        assertThatThrownBy {
            UpdateClientAction(testRealm, "simpleClient", name = name, redirectUris = redirectUris).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Client with id: simpleClient does not exist in realm: $testRealm!")
    }

    @Test
    fun testUpdateClient_PublicClientServiceAccount() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        UpdateClientAction(testRealm, "simpleClient", serviceAccountsEnabled = true, publicClient = true).executeIt()

        val testClient = client.clientById("simpleClient", testRealm)
        assertThat(testClient.serviceAccountsEnabled).isEqualTo(true)
        assertThat(testClient.publicClient).isEqualTo(true)
    }

    @Test
    fun testUpdateClient_PublicClientWebOrigin() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val webOrigins = listOf("+")
        UpdateClientAction(testRealm, "simpleClient", webOrigins = webOrigins).executeIt()

        val testClient = client.clientById("simpleClient", testRealm)
        assertThat(testClient.webOrigins).isEqualTo(webOrigins)
    }
}
