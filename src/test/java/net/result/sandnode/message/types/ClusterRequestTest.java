package net.result.sandnode.message.types;

import net.result.sandnode.message.RawMessage;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClusterRequestTest {

    @Test
    void testClusterMessageInitializationAndRetrieval() {
        var inputClusterNames = Set.of(
                "cluster1", "CLUSTER2", "cluster_3", "invalid-name", "cluster_4", "cluster_5 ", "123Cluster",
                "_underscore", "cluster with spaces", "cluster$special", "UPPERCASE", "", "  ", "что_то_на_русском"
        );

        var r = new ClusterRequest(inputClusterNames);
        var raw = new RawMessage(r.headers(), r.getBody());

        var request = new ClusterRequest(raw);

        var expectedClusterNames = Set.of(
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
