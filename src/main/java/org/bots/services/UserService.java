package org.bots.services;

import org.bots.model.datebase.Client;
import org.bots.model.datebase.User;
import org.bots.repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Service
public class UserService {

    private final UsersRepository usersRepository;
    private final NextSequenceService nextSequenceService;

    public UserService(UsersRepository usersRepository, NextSequenceService nextSequenceService) {
        this.usersRepository = usersRepository;
        this.nextSequenceService = nextSequenceService;
    }

    public boolean registerUser(String firstName, String username, Client client){
        User user = usersRepository.findByClients(client);
        if(user == null){
            user = new User();
            user.setId(nextSequenceService.getNextSequence(User.class));
            user.setFirstName(firstName);
            user.setUsername(username);
            user.setClients(new ArrayList<>());
            user.getClients().add(client);
            usersRepository.save(user);
            return true;
        }
        return false;
    }

    public String takeActiveCommand(Client client){
        User user = usersRepository.findByClients(client);
        String command = user.getActiveCommand();
        user.setActiveCommand(null);
        usersRepository.save(user);
        return command;
    }

    public void setActiveCommand(Client client, String command){
        User user = usersRepository.findByClients(client);
        user.setActiveCommand(command);
        usersRepository.save(user);
    }

    public void changeFavorites(Client client, Consumer<User> action) {
        User user = usersRepository.findByClients(client);
        action.accept(user);
        usersRepository.save(user);
    }

    public boolean checkFavorite(Client client, Integer movieId){
        User user = usersRepository.findByClients(client);
        return user.getFavorites().contains(movieId);
    }

    public List<Integer> getFavorites(Client client){
        User user = usersRepository.findByClients(client);
        if(user != null)
            return user.getFavorites();
        return Collections.emptyList();
    }
}
