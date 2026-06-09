package server.commands;

import common.data.Ticket;
import common.auth.Credentials;
import common.network.Response;
import server.CollectionManager;

public class RemoveGreaterCommand implements Command {

    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        if (!(extraData instanceof Ticket)){
            return new Response(false,"Не передан элемент для сравнения");
        }
        Ticket compareTicket = (Ticket) extraData;
        int removed = collection.removeAllGreater(compareTicket);
        return new Response(true, "Элементы удалены. Количество удаленных элементов: " + removed);
    }


    @Override
    public String getDescription() {
        return "Удаление элементов из коллекции превышающие заданный";
    }

    @Override
    public String getName() {
        return "remove_greater";
    }

    @Override
    public boolean requiresTicket() { return true; }
}