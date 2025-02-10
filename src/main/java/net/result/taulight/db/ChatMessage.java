package net.result.taulight.db;

import java.time.ZonedDateTime;

public record ChatMessage(String id, String chatID, String content, ZonedDateTime ztd, String memberID) {
}
