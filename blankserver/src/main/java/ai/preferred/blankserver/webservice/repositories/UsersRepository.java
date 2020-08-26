package ai.preferred.blankserver.webservice.repositories;

import ai.preferred.blankserver.webservice.models.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsersRepository extends MongoRepository<Users, String> {
    Users findBy_id(String _id);
}
