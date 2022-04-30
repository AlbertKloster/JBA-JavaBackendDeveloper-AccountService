package account.service;

import account.entity.Event;
import account.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventRepositoryService {

    private final EventRepository repository;

    @Autowired
    public EventRepositoryService(EventRepository repository) {
        this.repository = repository;
    }

    public Object save(Event event) {
        return repository.save(event);
    }

    public Object findAll() {
        return repository.findAll();
    }

}
