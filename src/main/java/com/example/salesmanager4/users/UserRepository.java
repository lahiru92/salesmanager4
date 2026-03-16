package com.example.salesmanager4.users;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserDTO, String> {

    @Modifying
    @Query("UPDATE users SET enabled = false WHERE username = :username")
    void deactivateUser(String username);

    @Modifying
    @Query("UPDATE users SET enabled = true WHERE username = :username")
    void activateUser(String username);

    @Query("SELECT * FROM users ORDER BY username")
    Iterable<UserDTO> findAllOrderByUsername();
}
