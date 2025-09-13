package net.result.sandnode.key;

import net.result.sandnode.link.SandnodeLinkRecord;
import org.jetbrains.annotations.NotNull;

public class LinkSource extends Source {
    public final SandnodeLinkRecord link;

    public LinkSource(@NotNull SandnodeLinkRecord link) {
        this.link = link;
    }
}
