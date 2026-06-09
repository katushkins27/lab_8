package common.auth;
import java.io.Serializable;

public class Credentials implements Serializable{
    private static final long serialVersionUID = 1L;
    private final String login;
    private final String password;

    public Credentials(String login, String password){
        this.login = login;
        this.password = password;
    }
    public String getLogin(){return login;}
    public String getPassword(){return password;}
}
