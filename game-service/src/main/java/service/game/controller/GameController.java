package service.game.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import service.game.DTO.Game;
import service.game.DTO.Move;
import service.game.DTO.OneVsOne;
import service.game.service.GameService;

@RestController
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService){
        this.gameService = gameService;
    }

    @PostMapping("/game")
    public Game game(@RequestBody OneVsOne players) {
        return gameService.generateGame(players);
    }

    @PostMapping("/move")
    public Game move(@RequestBody Move move){
        return gameService.makeMove(move);
    }

}
