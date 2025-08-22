package net.result.sandnode.message;

import net.result.sandnode.error.Errors;
import net.result.sandnode.message.types.ErrorMessage;
import org.jetbrains.annotations.NotNull;

public class SpecialErrorMessage extends ErrorMessage {
    public final String special;

    public SpecialErrorMessage(@NotNull RawMessage response) {
        super(response);
        special = response.headers().getValue("special");
    }

    public SpecialErrorMessage(String special) {
        super(Errors.SPECIAL);
        headers().setValue("special", special);
        this.special = special;
    }
}
