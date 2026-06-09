package common.network;

import common.data.*;
import java.io.Serializable;
import common.auth.Credentials;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String commandName;
    private final String arg;
    private final Ticket ticket;
    private final TicketType type;
    private final Long price;
    private final Credentials credentials;

    public Request(String commandName, String arg, Credentials credentials){
        this(commandName, arg, null, null, null, credentials);
    }
    public Request(String commandName, Ticket ticket, Credentials credentials){
        this(commandName, null, ticket, null, null, credentials);
    }
    public Request(String commandName, Long price, Credentials credentials){
        this(commandName, null, null, price, null, credentials);
    }
    public Request(String commandName, TicketType type, Credentials credentials){
        this(commandName, null, null, null, type, credentials);
    }
    public Request(String commandName, String arg, Ticket ticket, Credentials credentials) {
        this(commandName, arg, ticket, null, null, credentials);
    }

    private Request(String commandName, String arg, Ticket ticket, Long price, TicketType type, Credentials credentials){
        this.commandName = commandName;
        this.arg = arg;
        this.ticket = ticket;
        this.price = price;
        this.type = type;
        this.credentials = credentials;
    }
    public String getCommandName(){return commandName;}
    public String getArg(){return arg;}
    public Ticket getTicket(){return ticket;}
    public Long getPrice(){return price;}
    public TicketType getType() {return type;}
    public Credentials getCredentials() { return credentials; }
}
