package net.result.sandnode.serverclient;

import net.result.sandnode.message.RawMessage;

public record SequencedRawMessage(long sequence, RawMessage message) {
}
