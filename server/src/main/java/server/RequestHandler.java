package server;

import common.auth.Credentials;
import common.network.Request;
import common.network.Response;
import java.net.InetAddress;
import java.util.logging.Logger;

public class RequestHandler {
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());
    private final CommandExecutor executor;
    private final AuthManager authManager;

    public RequestHandler(CommandExecutor executor, AuthManager authManager) {
        this.executor = executor;
        this.authManager = authManager;
    }
    public Response handle(Request request, InetAddress clientAdress, int clientPort){
        Credentials credentials = request.getCredentials();
        if (credentials == null){
            return new Response(false, "Нет данных учетной записи");
        }

        String commandName = request.getCommandName();
        if (commandName.equals("check_user")) {
            if (authManager.userExists(credentials.getLogin())) {
                return new Response(true, "Пользователь существует");
            }
            return new Response(false, "Пользователь не найден");
        }

        if (commandName.equals("register")) {
            if (authManager.register(credentials)) {
                logger.info("Зарегистрирован новый пользователь: " + credentials.getLogin());
                return new Response(true, "Регистрация прошла успешно");
            }
            return new Response(false, "Произошла ошибка во время регистрации, возможно пользователь уже существует.");
        }
        if (commandName.equals("auth")) {
            if (authManager.authenticate(credentials)) {
                logger.info("Авторизован пользователь: " + credentials.getLogin());
                return new Response(true, "Авторизация прошла успешно");
            }
            logger.warning("Неудачная попытка входа: " + credentials.getLogin());
            return new Response(false, "Неверный логин или пароль");
        }
        if (!authManager.authenticate(credentials)){
            logger.warning("Неавторизованный запрос от " + clientAdress);
            return new Response(false, "Пользователь не авторизован. Проверьте логин или пароль :) ");
        }
        return executor.execute(request);
    }
}