package client;
import common.auth.Credentials;
import common.network.Request;
import common.network.Response;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Launcher {
    public static void main(String[] args){
        String host = "localhost";
        int port = 8080;
        if (args.length>0) host = args[0];
        if (args.length>1) port = Integer.parseInt(args[1]);
        try (Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .jna(true)
                    .build()){
            //System.out.println("Terminal class: " + terminal.getClass());

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new StringsCompleter(
                            "add",
                            "show",
                            "update",
                            "remove_by_id",
                            "remove_head",
                            "head",
                            "clear",
                            "remove_greater",
                            "remove_all_by_price",
                            "remove_any_by_type",
                            "min_by_venue",
                            "help",
                            "info",
                            "exit",
                            "execute_script",
                            "login",
                            "register"
                    ))
                    .option(LineReader.Option.AUTO_MENU, true)
                    .build();

            Credentials credentials = null;
            boolean authenticated = false;

            System.out.println("ДОБРО ПОЖАЛОВАТЬ");
            System.out.println("Для работы с приложением необходимо авторизоваться или зарегистрироваться.\\n");

            while (!authenticated) {
                System.out.println("Введите 'login' для входа или 'register' для регистрации");
                System.out.println("Для выхода введите 'exit'");
                String command = AuthReader.readChoice(reader);
                if (command.equals("exit")) {
                    System.out.println("Сеанс завершен");
                    return;
                }

                if (command.equals("login")) {
                    credentials = handleLogin(host, port, reader);
                    if (credentials != null) {
                        authenticated = true;
                    }
                } else if (command.equals("register")) {
                    handleRegister(host, port, reader);
                } else {
                    System.out.println("Неизвестная команда. Введите 'login' или 'register'");
                }
            }
            try (Client client = new Client(host, port, credentials, reader)) {
                client.start();
            }
        }catch (Exception e) {
            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Credentials handleLogin(String host, int port, LineReader reader){
        Credentials loginCreds = AuthReader.readLoginOnly(reader);
        String login = loginCreds.getLogin();

        try (Client tempClient = new Client(host, port, loginCreds, reader)){
            Request checkRequest = new Request("check_user", "", loginCreds);
            Response checkResponse = tempClient.sendRequest(checkRequest);
            if (!checkResponse.isSuccess()){
                System.out.println("Пользователь '" + login + "' не найден.");
                String answer = AuthReader.readYesNo(reader, "Зарегистрироваться? (y/n): ");

                if (answer.equals("y") || answer.equals("yes") || answer.equals("да")){
                    Credentials newCreds = AuthReader.readPasswordOnly(reader, login);
                    Request regRequest = new Request("register", "", newCreds);
                    Response regResponse = tempClient.sendRequest(regRequest);
                    if (regResponse.isSuccess()) {
                        System.out.println("Регистрация успешна! Теперь войдите.");
                    } else {
                        System.out.println("Ошибка: " + regResponse.getMessage());
                    }
                }
                return null;
            }
        } catch (Exception e){
            System.err.println("Ошибка проверки: "+e.getMessage());
            return null;
        }
        while (true){
            Credentials creds = AuthReader.readPasswordOnly(reader, login);
            try (Client tempClient = new Client(host,port,creds, reader)){
                Request authRequest = new Request("auth", "", creds);
                Response authResponse = tempClient.sendRequest(authRequest);

                if (authResponse.isSuccess()) {
                    System.out.println("Авторизация успешна!");
                    return creds;
                } else {
                    System.out.println("Ошибка: " + authResponse.getMessage());
                    System.out.print("Повторить ввод пароля? (y/n) или 'exit' для выхода: ");
                    String choice = AuthReader.readChoice(reader);
                    if (choice.equals("exit")) {
                        return null;
                    }
                    if (!choice.equals("y") && !choice.equals("yes") && !choice.equals("да")) {
                        return null;
                    }
                }
            } catch (Exception e){
                System.err.println("Ошибка: "+e.getMessage());
                return null;
            }
        }
    }

    private static void handleRegister(String host , int port, LineReader reader){
        Credentials newCreds = AuthReader.readCredentials(reader);

        try (Client tempClient = new Client(host,port,newCreds, reader)){
            Request regRequest = new Request("register","",  newCreds);
            Response regResponse = tempClient.sendRequest(regRequest);
            if (regResponse.isSuccess()) {
                System.out.println("Регистрация успешна! Теперь войдите.");
            } else {
                System.out.println("Ошибка: " + regResponse.getMessage());
            }
        } catch (Exception e){
            System.err.println("Ошибка: "+e.getMessage());
        }
    }
}
