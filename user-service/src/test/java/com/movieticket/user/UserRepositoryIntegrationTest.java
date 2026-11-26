package com.movieticket.user;
import org.junit.jupiter.api.Test;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest class UserRepositoryIntegrationTest {@Autowired UserRepository repository; @Test void persistsAndLooksUpUserByEmail(){repository.saveAndFlush(new User("person@example.com","encoded"));assertThat(repository.findByEmail("person@example.com")).isPresent();}}
