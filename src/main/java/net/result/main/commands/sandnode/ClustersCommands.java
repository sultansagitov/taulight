package net.result.main.commands.sandnode;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.sandnode.hubagent.ClientProtocol;

import java.util.Collection;
import java.util.List;

public class ClustersCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo("clusters", "List all clusters", "Clusters", ClustersCommands::clusters));
        registry.register(new CommandInfo("addCluster", "Add clusters", "Clusters", ClustersCommands::addCluster));
        registry.register(new CommandInfo("rmCluster", "Remove clusters", "Clusters", ClustersCommands::rmCluster));
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
