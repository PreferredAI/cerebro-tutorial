package ai.preferred.blankserver.webservice.repositories;

import ai.preferred.blankserver.webservice.models.Items;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemsRepository extends MongoRepository<Items, String> {
    Items findBy_id(String _id);
}
