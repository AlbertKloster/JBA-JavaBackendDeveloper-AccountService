package account.service;

import account.entity.BreachedPassword;
import account.repository.BreachedPasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BreachedPasswordRepositoryService {

    private final BreachedPasswordRepository repository;

    @Autowired
    public BreachedPasswordRepositoryService(BreachedPasswordRepository repository) {
        this.repository = repository;
    }

    public BreachedPassword save(BreachedPassword password) {
        return repository.save(password);
    }

    public List<BreachedPassword> findAll() {
        return repository.findAll();
    }

}
