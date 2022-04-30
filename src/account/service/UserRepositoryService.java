package account.service;

import account.constant.Constant;
import account.entity.User;
import account.enums.Role;
import account.exception.AdministratorLockException;
import account.exception.RemoveAdministratorException;
import account.exception.UserExistException;
import account.exception.UserNotFoundException;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class UserRepositoryService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    @Autowired
    public UserRepositoryService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public User save(User user) {
        checkIfExist(user);
        setPasswordAndEmail(user);
        setRoles(user);
        return repository.save(user);
    }

    private void checkIfExist(User user) {
        if (repository.findUserByEmailIgnoreCase(user.getEmail()).isPresent())
            throw new UserExistException();
    }

    private void setPasswordAndEmail(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setEmail(user.getEmail().toLowerCase(Locale.ROOT));
    }

    private void setRoles(User user){
        if (repository.findAll().isEmpty())
            user.setRoles(Set.of(Role.ADMINISTRATOR.getAuthority()));
        else
            user.setRoles(Set.of(Role.USER.getAuthority()));
    }

    public User update(User user) {
        if (repository.findUserByEmailIgnoreCase(user.getEmail()).isPresent()) {
            return repository.save(user);
        }
        throw new UserNotFoundException();
    }

    public User findUserByEmail(String email) {
        return repository.findUserByEmailIgnoreCase(email).orElseThrow(UserNotFoundException::new);
    }

    public Optional<User> findUserByEmailOptional(String email) {
        return repository.findUserByEmailIgnoreCase(email);
    }

    public void deleteUserByEmail(String email) {
        User user = findUserByEmail(email);
        if (user.getRoles().contains(Role.ADMINISTRATOR.getAuthority()))
            throw new RemoveAdministratorException();
        repository.delete(user);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public void lock(String email) {
        User user = findUserByEmail(email);
        if (user.getRoles().contains(Role.ADMINISTRATOR.getAuthority()))
            throw new AdministratorLockException();
        user.setFailedAttempts(Constant.MAX_FAILED_ATTEMPTS + 1);
        update(user);
    }

    public void unlock(String email) {
        User user = findUserByEmail(email);
        user.setFailedAttempts(0);
        update(user);
    }

}
