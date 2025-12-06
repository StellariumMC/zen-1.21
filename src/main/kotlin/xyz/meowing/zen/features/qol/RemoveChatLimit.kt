package xyz.meowing.zen.features.qol

import xyz.meowing.zen.annotations.Module
import xyz.meowing.zen.features.Feature

@Module
object RemoveChatLimit : Feature(
    "removeChatLimit",
    "Remove chat history limit",
    "Removes the chat history limit",
    "QoL"
)