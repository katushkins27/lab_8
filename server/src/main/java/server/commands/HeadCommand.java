package server.commands;

import common.data.Ticket;
import common.auth.Credentials;
import common.network.Response;
import server.CollectionManager;

public class HeadCommand implements Command {

    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        Ticket ticket = collection.head();
        if (ticket == null) {
            return new Response(true, "Коллекция пустая");
        } else {
            return new Response(true, ticket.toString());
        }
    }

    @Override
    public String getDescription() {
        return "Первый билет коллекции";
    }
    @Override
    public String getName() {
        return "head";
    }
    @Override
    public boolean requiresTicket() { return false; }
}