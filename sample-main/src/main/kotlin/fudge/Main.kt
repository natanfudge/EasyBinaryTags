package fudge

import fudge.Generated_Hello
import fudge.minecraft.CompoundTag

@GenName
class Hello

@Kson
data class User(val name: String,
                val email: String)

fun main() {
    println("Hello ${Generated_Hello().getName()}")
    println("User to JSON")
    val user = User(
            name = "Test",
            email = "test@email.com"
    )
    println("User: $user")
    println("Json: ${user.toTag()}")

}