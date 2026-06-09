package server.commands;
import common.auth.Credentials;
import common.commands.CommandInfo;
import common.network.Response;
import server.CollectionManager;

public interface Command extends CommandInfo{
    Response execute(CollectionManager collection, String arg, Object extraData, Credentials credentials);
    boolean requiresTicket();
}
