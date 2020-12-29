import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kordex.ext.mappings.MappingsExtension
import com.kotlindiscord.kordex.ext.mappings.extMappings

val bot = ExtensibleBot(
    token = System.getenv("TOKEN"),
    prefix = "!",
    addSentryExtension = false
)

suspend fun main() {
    bot.extMappings()
    bot.start()
}
