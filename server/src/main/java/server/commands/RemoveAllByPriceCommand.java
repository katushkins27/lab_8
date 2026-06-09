package server.commands;
import common.auth.Credentials;
import common.network.Response;
import server.CollectionManager;

public class RemoveAllByPriceCommand implements Command {
    @Override
    public Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials) {
        try {
            Long price = (Long) extraData;
            int removed = collection.removeAllByPrice(price);
            return new Response(true, "Билеты удалены " + removed);
        } catch (NumberFormatException e) {
            return new Response(false, "Ошибка в цене. Введите число");
        }
    }


    @Override
    public String getDescription() {
        return "Удаление элементов из коллекции по заданной цене";
    }

    @Override
    public String getName() {
        return "remove_all_by_price";
    }

    @Override
    public boolean requiresTicket() { return false; }
}