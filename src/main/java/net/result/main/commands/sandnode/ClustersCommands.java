package net.result.main.commands.sandnode;

import net.result.main.commands.LoopCondition;
import net.result.main.commands.ConsoleContext;
import net.result.sandnode.hubagent.ClientProtocol;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ClustersCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("clusters", ClustersCommands::clusters);
        commands.put("addCluster", ClustersCommands::addCluster);
        commands.put("rmCluster", ClustersCommands::rmCluster);
    }

    private static void clusters(List<String> ignored, ConsoleContext context) throws Exception {
        Collection<String> clusters = ClientProtocol.getClusters(context.client);
        System.out.printf("Your clusters: %s%n", clusters);
    }

    private static void addCluster(List<String> clusters, ConsoleContext context) throws Exception {
        Collection<String> clustersAfterAdding = ClientProtocol.addToClusters(context.client, clusters);
        System.out.printf("Your clusters now (after adding): %s%n", clustersAfterAdding);
    }

    private static void rmCluster(List<String> clusters, ConsoleContext context) throws Exception {
        Collection<String> clustersAfterRemoving = ClientProtocol.removeFromClusters(context.client, clusters);
        System.out.printf("Your clusters now (after removing): %s%n", clustersAfterRemoving);
    }
}
