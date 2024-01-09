package service.game.DTO;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Game implements Serializable {
    private String id;
    private String x_player;
    private String o_player;
    private String current_field;
    private String allowed_to_move;
    private String winner;
    private boolean tie;

    public void swapPlayers() {
        if (this.allowed_to_move == this.x_player) {
            this.allowed_to_move = this.o_player;
        } else {
            this.allowed_to_move = this.x_player;
        }
    }
}
