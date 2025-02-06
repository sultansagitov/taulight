package net.result.taulight.db;

import java.time.ZonedDateTime;

public record ChatMessage(String chatID, String content, ZonedDateTime ztd, String memberID) {
}
