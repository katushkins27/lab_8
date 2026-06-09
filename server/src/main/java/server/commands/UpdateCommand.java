package server.commands;

import common.auth.Credentials;
import common.data.Ticket;
import common.network.Response;
import server.AuthManager;
import server.CollectionManager;
import server.DatabaseManager;

import java.util.Collections;


public class UpdateCommand implements Command {
    private final DatabaseManager dbManager;
    private final AuthManager authManager;

    public UpdateCommand(DatabaseManager dbManager, AuthManager authManager){
        this.dbManager = dbManager;
        this.authManager = authManager;
    }



    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        if (arg.isEmpty()) {
            return new Response(false, "Укажите ID для обновления");
        }
        try {
            int id = Integer.parseInt(arg);

            int userId = authManager.getUserId(credentials);
            if (userId==-1){
                return new Response(false, "Ошибка авторизации!");
            }

            if (!dbManager.ticketExists(id,userId)){
                return new Response(false, "Билет с ID "+id+" не найден или нет прав");
            }

            Ticket updTicket = (Ticket) extraData;
            updTicket.setID(id);
            if (dbManager.updateTicket(id, updTicket, userId)){
                collection.update(id,updTicket);
                return new Response(true, "Билет обновлен с ID" + id);
            } else{
                return new Response(false, "Ошибка при обновлении билета в БД");
            }
        } catch (NumberFormatException e) {
            return new Response(false,"Ошибка в ID. Введите число ");
        }
    }



    @Override
    public String getDescription() {
        return "Обновление значения элемента по ID ";
    }
    @Override
    public String getName() {
        return "update";
    }
    @Override
    public boolean requiresTicket() { return true; }
}