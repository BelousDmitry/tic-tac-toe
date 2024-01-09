package service.connection.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import service.connection.DTO.Invite;
import service.connection.DTO.Move;
import service.connection.DTO.OneVsOne;
import service.connection.service.ClientService;

@CrossOrigin(origins = "http://localhost:4200", exposedHeaders = "Access-Control-Allow-Origin")
@Controller
@RestController
public class ClientController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ClientService clientService;

    public ClientController(SimpMessagingTemplate messagingTemplate, ClientService clientService) {
        this.messagingTemplate = messagingTemplate;
        this.clientService = clientService;
    }

    // put player into the queue and check if it is enough 
    // (more >= 2) to make one vs one 
    // if yes, sends to game service
    // sends response to players
    @MessageMapping("/ready/random")
    public void ready(String username) {

        this.clientService.addPlayerToQueue(username);
        OneVsOne players = this.clientService.generatePlayersOneVsOne();

        if (players != null) {
            String game = this.clientService.sendToGameService(players);
            this.messagingTemplate.convertAndSend("/topic/game/" + players.username_1, game);
            this.messagingTemplate.convertAndSend("/topic/game/" + players.username_2, game);
        }
    }

    // sends the move of the players to game service
    // sends response to players
    @MessageMapping("/move")
    public void move(@RequestBody Move move) {

        String game = this.clientService.sendToGameService(move);

        this.messagingTemplate.convertAndSend("/topic/game/" + move.username_1, game);
        this.messagingTemplate.convertAndSend("/topic/game/" + move.username_2, game);

    }

    @MessageMapping("/invite/send")
    public void inviteSend(@RequestBody Invite invite) {

        if (this.clientService.isOnline(invite.receiver)) {
            this.messagingTemplate.convertAndSend("/topic/invite/" + invite.receiver, invite);
        }else{
            this.messagingTemplate.convertAndSend("/topic/notify/" + invite.inviter, "User is offline");
        }
    }

    @MessageMapping("/invite/accept")
    public void inviteAccept(@RequestBody Invite invite) {
        OneVsOne players = new OneVsOne(invite.inviter, invite.receiver);
        String game = this.clientService.sendToGameService(players);
        this.messagingTemplate.convertAndSend("/topic/game/" + players.username_1, game);
        this.messagingTemplate.convertAndSend("/topic/game/" + players.username_2, game);
    }

    @MessageMapping("/invite/deny")
    public void inviteDeny(@RequestBody Invite invite) {
        this.messagingTemplate.convertAndSend("/topic/notify/" + invite.inviter, invite.receiver + " denied your invitation");
    }

}
