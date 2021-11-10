package de.klg71.keycloakmigration.changeControl

import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.validateMockitoUsage
import org.mockito.kotlin.verify
import de.klg71.keycloakmigration.KoinLogger
import de.klg71.keycloakmigration.changeControl.actions.ActionTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.Realm
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mockito
import org.slf4j.LoggerFactory

class RealmCheckerTest :KoinTest{

    private val logger = LoggerFactory.getLogger(ActionTest::class.java)!!
    private val client = mock<KeycloakClient>()

    @Before
    fun setup() {
        reset(client)
        startKoin {
            logger(KoinLogger(logger))
            modules(module {
                single { client }
            })
        }
    }

    @After
    fun tearDown() {
        validateMockitoUsage()
        stopKoin()
    }

    @Test
    fun testCheck() {
        val realmChecker = RealmChecker()
        val mockRealm = mockk<Realm>()
        every{
            mockRealm.id
        } returns "test"

        Mockito.`when`(client.realms()).thenReturn(listOf(mockRealm))

        assertThat(realmChecker.check("test")).isEqualTo("test")

        verify(client, times(1)).realms()
    }

    @Test
    fun testCheckCache() {
        val realmChecker = RealmChecker(listOf("test"))
        val mockRealm = mockk<Realm>()
        every{
            mockRealm.id
        } returns "test"

        Mockito.`when`(client.realms()).thenReturn(listOf(mockRealm))

        assertThat(realmChecker.check("test")).isEqualTo("test")

        verify(client, times(0)).realms()
    }

    @Test
    fun testCheckException() {
        val realmChecker = RealmChecker()

        Mockito.`when`(client.realms()).thenReturn(listOf())

        assertThatThrownBy {
            realmChecker.check("test")
        }.isInstanceOf(MigrationException::class.java).hasMessage("Realm with id: test does not exist!")

        verify(client, times(1)).realms()
    }
}
