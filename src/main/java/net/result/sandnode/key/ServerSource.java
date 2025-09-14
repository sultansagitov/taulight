package net.result.sandnode.key;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.util.Address;

import java.time.ZonedDateTime;

public class ServerSource extends Source {
    @JsonProperty
    public Address address;

    public ServerSource() {}

    public ServerSource(Address address) {
        super(ZonedDateTime.now());
        this.address = address;
    }
}
