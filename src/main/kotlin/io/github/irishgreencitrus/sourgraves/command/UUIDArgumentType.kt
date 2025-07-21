package io.github.irishgreencitrus.sourgraves.command

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import java.util.*


class UUIDArgumentType : ArgumentType<UUID> {
    private val invalidUuid = DynamicCommandExceptionType { o: Any ->
        LiteralMessage(
            "Invalid uuid: $o"
        )
    }

    companion object {
        fun uuid(): UUIDArgumentType {
            return UUIDArgumentType()
        }

        fun <S> getUuid(name: String, context: CommandContext<S>): UUID {
            return context.getArgument(name, UUID::class.java)
        }
    }


    @Throws(CommandSyntaxException::class)
    override fun parse(reader: StringReader): UUID {
        val argBeginning = reader.cursor;
        if (!reader.canRead()) {
            reader.skip()
        }
        while (reader.canRead() && (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '-')) { // peek provides the character at the current cursor position.
            reader.skip()
        }

        val uuidString = reader.string.substring(argBeginning, reader.cursor)
        try {
            val uuid = UUID.fromString(uuidString)
            return uuid
        } catch (ex: IllegalArgumentException) {
            reader.cursor = argBeginning
            throw invalidUuid.createWithContext(reader, ex.message)
        }
    }

    override fun getExamples(): MutableCollection<String> {
        return mutableListOf(
            "765e5d33-c991-454f-8775-b6a7a394c097",
            "069a79f4-44e9-4726-a5be-fca90e38aaf5",
            "61699b2e-d327-4a01-9f1e-0ea8c3f06bc6"
        )
    }
}