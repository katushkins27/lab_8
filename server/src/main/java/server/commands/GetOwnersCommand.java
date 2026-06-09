package server.commands;

import common.auth.Credentials;
import common.network.Response;
import server.CollectionManager;

public class GetOwnersCommand implements Command {

    @Override
    public Response execute(CollectionManager collection, String arg,
                            Object extraData, Credentials credentials) {
        return new Response(true, "ok", collection.getOwnerMap());
    }

    @Override
    public String getDescription() {
        return "Получить карту владельцев билетов (для GUI)";
    }

    @Override
    public String getName() {
        return "get_owners";
    }

    @Override
    public boolean requiresTicket() {
        return false;
    }
}
