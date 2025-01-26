package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
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

        GroupRequest request = new GroupRequest(new GroupRequest(inputGroupNames));

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
