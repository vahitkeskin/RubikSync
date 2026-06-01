package com.vahitkeskin.rubiksync

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform