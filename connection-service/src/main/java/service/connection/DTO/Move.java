package service.connection.DTO;

public class Move {
    public String username_1;
    public String username_2;
    public String payload;

    public Move (String username_1, String username_2, String payload){
        this.username_1 = username_1;
        this.username_2 = username_2;
        this.payload = payload;
    }


}
