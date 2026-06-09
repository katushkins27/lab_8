package server.commands;

import common.auth.Credentials;
import common.data.Ticket;
import common.network.Response;
import server.AuthManager;
import server.CollectionManager;
import server.DatabaseManager;

public class AddCommand implements Command {
    private final DatabaseManager dbManager;
    private final AuthManager authManager;

    public AddCommand(DatabaseManager dbManager, AuthManager authManager) {
        this.dbManager = dbManager;
        this.authManager = authManager;
    }

    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        if (!(extraData instanceof Ticket)) {
            return new Response(false, "Данные билета не переданы");
        }
        Ticket ticket = (Ticket) extraData;
        int userId = authManager.getUserId(credentials);
        Ticket savedTicket = dbManager.addTicket(ticket, userId);
        if (savedTicket == null){
            return new Response(false, "Ошибка при сохранении в базу данных");
        }
        collection.addElement(ticket);
        return new Response(true, "Данные билета переданы с ID: " + ticket.getId());
    }

    @Override
    public String getDescription() {
        return "Добавление элемента в коллекцию";
    }
    @Override
    public String getName() {return "add";}
    @Override
    public boolean requiresTicket() { return true; }
}