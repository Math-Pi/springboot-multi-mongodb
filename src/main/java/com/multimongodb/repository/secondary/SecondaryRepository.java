package com.multimongodb.repository.secondary;

import com.multimongodb.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SecondaryRepository extends MongoRepository<User, String> {
}
