package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GroupRequestTest {

    @Test
    void testGroupMessageInitializationAndRetrieval() throws ExpectedMessageException {
        Collection<String> inputGroupNames = Set.of(
                "group1", "GROUP2", "group_3", "invalid-name", "group_4", "group_5 ", "123Group", "_underscore",
                "group with spaces", "group$special", "UPPERCASE", "", "  ", "что_то_на_русском"
        );

        GroupRequest r = new GroupRequest(inputGroupNames);
        RawMessage raw = new RawMessage(r.headers(), r.getBody());

        GroupRequest request = new GroupRequest(raw);

        Collection<String> expectedGroupNames = Set.of(
                "#group1",
                "#group2",
                "#group_3",
                "#group_4",
                "#group_5",
                "#123group",
                "#_underscore",
                "#uppercase"
        );

        assertEquals(expectedGroupNames, request.getGroupsID());
    }
}
