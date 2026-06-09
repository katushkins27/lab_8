package server.commands;

import common.data.Ticket;
import common.network.Response;
import server.CollectionManager;
import common.auth.Credentials;

import java.util.ArrayList;
import java.util.List;

public class ShowCommand implements Command {

    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        List<Ticket> ticketList = new ArrayList<>(collection.getCollection());
        String res = "Коллекция содержит " + ticketList.size() + " элементов.";
        return new Response(true, res, ticketList);
    }

    @Override
    public String getDescription() {
        return "Вывод всех элементов коллекции";
    }
    @Override
    public String getName() {
        return "show";
    }
    @Override
    public boolean requiresTicket() { return false; }
}