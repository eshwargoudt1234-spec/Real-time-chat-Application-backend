package com.chatapp.repository;

import com.chatapp.entity.User;
import com.chatapp.entity.UserStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /** Login accepts either username or email */
    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);

    /** All users except me */
    List<User> findByUsernameNotOrderByNameAsc(String username);

    /** Search by name or username, except me */
    @Query("select u from User u where u.username <> :me and " +
           "(lower(u.name) like lower(concat('%', :q, '%')) or lower(u.username) like lower(concat('%', :q, '%'))) " +
           "order by u.name")
    List<User> search(@Param("me") String me, @Param("q") String q);

    /** Used on startup: nobody can be online before the server starts */
    @Modifying
    @Query("update User u set u.status = :status")
    int updateAllStatus(@Param("status") UserStatus status);
}
