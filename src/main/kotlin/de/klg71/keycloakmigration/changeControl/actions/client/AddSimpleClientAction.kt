package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AddSimpleClient
import de.klg71.keycloakmigration.rest.extractLocationUUID
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class AddSimpleClientAction(
        private val realm: String,
        private val clientId: String,
        private val enabled: Boolean = true,
        private val attributes: Map<String, List<String>> = mapOf(),
        private val protocol: String = "openid-connect",
        private val redirectUris: List<String> = emptyList()) : Action() {

    private lateinit var clientUuid: UUID

    private val addUser = addClient()

    private fun addClient() = AddSimpleClient(clientId, enabled, attributes, protocol, redirectUris)

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(realm)
                append(clientId)
                append(enabled)
                for ((key, value) in attributes) {
                    append(key)
                    value.forEach {
                        append(it)
                    }
                }
                append(protocol)
                redirectUris.forEach {
                    append(it)
                }
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = hash


    override fun execute() {
        client.addSimpleClient(addUser, realm).run {
            clientUuid = extractLocationUUID()
        }
    }

    //FIXME: This fails if delete clients undo operation on the same client happend before, cause the uuids are no longer valid
    override fun undo() {
        client.deleteClient(clientUuid, realm)
    }

    override fun name() = "AddSimpleClient $clientId"

}