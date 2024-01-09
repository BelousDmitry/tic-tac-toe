package service.connection.DTO;

public class Invite {
    public String inviter;
    public String receiver;


    public Invite(String inviter, String receiver){
        this.inviter = inviter;
        this.receiver = receiver;
    }
}
