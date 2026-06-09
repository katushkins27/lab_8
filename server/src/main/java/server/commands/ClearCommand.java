package server.commands;
import common.auth.Credentials;
import common.network.Response;
import server.CollectionManager;

public class ClearCommand implements Command {

    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        collection.clearCollection();
        return new Response(true, "Коллекция очищена");
    }

    @Override
    public String getDescription() {
        return "Очищение коллекции";
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public boolean requiresTicket() {
        return false;
    }

}