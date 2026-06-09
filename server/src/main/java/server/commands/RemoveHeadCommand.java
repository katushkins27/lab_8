package server.commands;

import common.data.Ticket;
import common.auth.Credentials;
import common.network.Response;
import server.CollectionManager;


public class RemoveHeadCommand implements Command {

    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        Ticket ticket = collection.head();
        if (ticket == null) {
            return new Response(true, "Коллекция пустая");
        } else {
            collection.removeHead();
            return new Response(true, "Удален первый элемент коллекции");
        }
    }

    @Override
    public String getDescription() {
        return "Удаление первого элемента коллекции";
    }

    @Override
    public String getName() {
        return "remove_head";
    }

    @Override
    public boolean requiresTicket() { return false; }
}