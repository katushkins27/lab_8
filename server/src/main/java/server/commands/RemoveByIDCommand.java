package server.commands;
import common.auth.Credentials;
import common.network.Response;
import server.AuthManager;
import server.CollectionManager;
import server.DatabaseManager;

public class RemoveByIDCommand implements Command {
    private final DatabaseManager dbManager;
    private final AuthManager authManager;

    public RemoveByIDCommand(DatabaseManager dbManager, AuthManager authManager){
        this.dbManager = dbManager;
        this.authManager = authManager;
    }

    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        try {
            int id = Integer.parseInt(arg);
            int userId = authManager.getUserId(credentials);

            if (userId == -1){
                return new Response(false, "Ошибка авторизации!");
            }
            if(dbManager.deleteTicket(id,userId)){
                collection.removeById(id);
                return new Response(true, "Билет с ID "+id+" удален");
            } else{
                return new Response(false, "Билет с ID "+id+" не найден или нет прав");
            }

        } catch (NumberFormatException e) {
            return new Response(false, "Ошибка в ID. Введите число");
        }
    }

    @Override
    public String getDescription() {
        return "Удаление элемента из БД по ID";
    }

    @Override
    public String getName() {
        return "remove_by_id";
    }

    @Override
    public boolean requiresTicket() { return false; }
}