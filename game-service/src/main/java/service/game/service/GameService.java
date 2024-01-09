package service.game.service;

import java.lang.reflect.Array;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import service.game.DTO.Game;
import service.game.DTO.Move;
import service.game.DTO.OneVsOne;

@Service
public class GameService {

    private final RedisTemplate<String, Object> redisTemplate;

    public GameService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Game generateGame(OneVsOne players) {
        Game game = Game.builder()
                .id(UUID.randomUUID().toString())
                .x_player(players.username_1)
                .o_player(players.username_2)
                .allowed_to_move(players.username_1)
                .current_field("?,?,?/?,?,?/?,?,?")
                .winner(null)
                .tie(false)
                .build();

        this.redisTemplate.opsForValue().set("GAME-" + game.getId(), game, 1, TimeUnit.MINUTES);

        return game;
    }

    public Game makeMove(Move move) {

        Game game = (Game) this.redisTemplate.opsForValue().get("GAME-" + move.game_id);

        if (game != null) {

            String[][] array = this.convertFieldTo2dArray(game.getCurrent_field());

            if (move.overtime) {
                if (move.username.equals(game.getX_player()))
                    game.setWinner(game.getO_player());
                else
                    game.setWinner(game.getX_player());
                return game;
            }

            if (!array[move.row][move.column].equals("?") || !game.getAllowed_to_move().equals(move.username)) {
                return game;
            } else if (game.getAllowed_to_move() == game.getX_player())
                array[move.row][move.column] = "X";
            else if (game.getAllowed_to_move() == game.getO_player())
                array[move.row][move.column] = "O";

            int winner = this.getWinner(array);

            if (winner >= 3)
                game.setWinner(game.getX_player());
            else if (winner <= -3)
                game.setWinner(game.getO_player());
            else if (winner == 0)
                game.setTie(true);

            String field = this.convertFieldToString(array);

            game.setCurrent_field(field);
            game.swapPlayers();

            this.redisTemplate.opsForValue().set("GAME-" + game.getId(), game, 1, TimeUnit.MINUTES);

            return game;
        }

        return game;
    }

    public String[][] convertFieldTo2dArray(String field) {

        int size = Array.getLength(field.split("/"));
        String[][] array = new String[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                array[i][j] = field.split("/")[i].split(",")[j];
            }
        }
        return array;
    }

    public int getWinner(String[][] field) {
        int size = Array.getLength(field);
        int counter = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (field[i][j].equals("X"))
                    counter++;
                else if (field[i][j].equals("O"))
                    counter--;
                if (Math.abs(counter) >= 3)
                    return counter;
            }
            counter = 0;
        }

        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                if (field[i][j].equals("X"))
                    counter++;
                else if (field[i][j].equals("O"))
                    counter--;
                if (Math.abs(counter) >= 3)
                    return counter;
            }
            counter = 0;
        }

        for (int i = 0; i < size; i++) {
            if (field[i][i].equals("X"))
                counter++;
            else if (field[i][i].equals("O"))
                counter--;
            if (Math.abs(counter) >= 3)
                return counter;
        }

        counter = 0;

        for (int i = 0, j = size - 1; i < size && j >= 0; i++, j--) {
            if (field[i][j].equals("X"))
                counter++;
            else if (field[i][j].equals("O"))
                counter--;
            if (Math.abs(counter) >= 3)
                return counter;
        }

        counter = 0;

        // check for draw (tie)
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (field[i][j].equals("?"))
                    counter++;
            }
        }

        if (counter == 0)
            return 0;
        else
            return -1;
    }

    public String convertFieldToString(String[][] field) {

        String result = "";
        int size = Array.getLength(field);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result = result + field[i][j] + ",";
            }
            result = result.substring(0, result.length() - 1);
            result = result + "/";
        }

        result = result.substring(0, result.length() - 1);
        return result;
    }

}
