package server.commands;

import common.auth.Credentials;
import common.network.Response;
import server.CollectionManager;

public class InfoCommand implements Command {
    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        return new Response(true, collection.getInfo());
    }

    @Override
    public String getDescription() {
        return "Вывод инфо о коллекции";
    }
    @Override
    public String getName() {
        return "info";
    }
    @Override
    public boolean requiresTicket() { return false; }
}