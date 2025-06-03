package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClusterRequestTest {

    @Test
    void testClusterMessageInitializationAndRetrieval() throws ExpectedMessageException {
        Collection<String> inputClusterNames = Set.of(
                "cluster1", "CLUSTER2", "cluster_3", "invalid-name", "cluster_4", "cluster_5 ", "123Cluster", "_underscore",
                "cluster with spaces", "cluster$special", "UPPERCASE", "", "  ", "что_то_на_русском"
        );

        ClusterRequest r = new ClusterRequest(inputClusterNames);
        RawMessage raw = new RawMessage(r.headers(), r.getBody());

        ClusterRequest request = new ClusterRequest(raw);

        Collection<String> expectedClusterNames = Set.of(
                "#cluster1",
                "#cluster2",
                "#cluster_3",
                "#cluster_4",
                "#cluster_5",
                "#123cluster",
                "#_underscore",
                "#uppercase"
        );

        assertEquals(expectedClusterNames, request.getClustersID());
    }
}
