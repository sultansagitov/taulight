package net.result.main.setting;

import net.result.sandnode.util.Container;
import net.result.taulight.entity.ReactionPackageEntity;
import net.result.taulight.repository.ReactionPackageRepository;
import net.result.taulight.repository.ReactionTypeRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CreateReactions {
    private static final Logger LOGGER = LogManager.getLogger(CreateReactions.class);

    public CreateReactions(Container container) {
        ReactionPackageRepository reactionPackageRepo = container.get(ReactionPackageRepository.class);
        ReactionTypeRepository reactionTypeRepo = container.get(ReactionTypeRepository.class);

        try {
            if (reactionPackageRepo.find("taulight").isEmpty()) {
                ReactionPackageEntity rp = reactionPackageRepo.create("taulight", "Main package of taulight");
                reactionTypeRepo.create(rp, List.of("fire", "like", "laugh", "wow", "sad", "angry"));
            }
        } catch (Exception e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }
}
