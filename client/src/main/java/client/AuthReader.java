package client;
import common.auth.Credentials;
import org.jline.reader.LineReader;

//import java.util.Optional;
import java.util.Scanner;
public class AuthReader {
    public static Credentials readCredentials(LineReader reader){
        System.out.println("АВТОРИЗАЦИЯ");
        String login = reader.readLine("Логин: ").trim();
        String password = reader.readLine("Пароль: ");
        return new Credentials(login, password);
    }

    public static Credentials readLoginOnly(LineReader reader){
        String login = reader.readLine("Логин: ").trim();
        return new Credentials(login, "");
    }

    public static Credentials readPasswordOnly(LineReader reader, String login){
        String password = reader.readLine("Пароль: ");
        return new Credentials(login, password);
    }

    public static String readChoice(LineReader reader){
        return reader.readLine("> ").trim().toLowerCase();
    }

    public static String readYesNo(LineReader reader, String prompt){
        return reader.readLine(prompt).trim().toLowerCase();
    }

}
