package net.result.sandnode;

import net.result.sandnode.util.Container;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GlobalTestState {
    public static final Container container = new Container();
}
