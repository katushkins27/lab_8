package server;
import common.data.*;
import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class CollectionManager {
    //private static final Logger logger = Logger.getLogger(CollectionManager.class.getName());
    private final LocalDateTime date = LocalDateTime.now();
    private final DatabaseManager dbManager;
    private Collection<Ticket> collection = Collections.synchronizedCollection(new ArrayDeque<>());

    public CollectionManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        loadAllTickets();
    }


    private void loadAllTickets() {
        List<Ticket> tickets = dbManager.getAllTickets();
        synchronized (collection) {
            collection.addAll(tickets);
        }
        System.out.println("Загружено билетов из БД: " + tickets.size());
    }

    private String getTownName(Ticket ticket) {
        if (ticket.getVenue() != null &&
                ticket.getVenue().getAddress() != null &&
                ticket.getVenue().getAddress().getTown() != null &&
                ticket.getVenue().getAddress().getTown().getName() != null) {
            return ticket.getVenue().getAddress().getTown().getName();
        }
        return "";
    }

    private void sortCollectionByLocation() {
        collection = collection.stream()
                .sorted((t1, t2) -> getTownName(t1).compareTo(getTownName(t2)))
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    public void addElement(Ticket ticket) {
        synchronized (collection) {
            collection.add(ticket);
            sortCollectionByLocation();
        }
    }

    public boolean update(int id, Ticket newTicket) {
        synchronized (collection) {
            Optional<Ticket> optionalTicket = collection.stream().filter(t -> t.getId() == id).
                    findFirst();
            if (optionalTicket.isPresent()) {
                Ticket oldTicket = optionalTicket.get();
                collection.remove(oldTicket);
                collection.add(newTicket);
                sortCollectionByLocation();
                return true;
            }
            return false;
        }
    }

    public boolean removeById(int id) {
        synchronized (collection) {
            boolean removed = collection.removeIf(t -> t.getId() == id);
            if (removed) {
                sortCollectionByLocation();
            }
            return removed;
        }
    }

    public void clearCollection() {
        synchronized (collection) {
            collection.clear();
        }
    }

    public Ticket head() {
        synchronized (collection) {
            sortCollectionByLocation();
            if (collection.isEmpty()) return null;
            return ((Deque<Ticket>)collection).peekFirst();
        }
    }

    public void removeHead() {
        synchronized (collection) {
            if (collection.isEmpty()) return;
            sortCollectionByLocation();
            ((Deque<Ticket>)collection).pollFirst();
        }
    }

    public int removeAllGreater(Ticket newTicket) {
        synchronized (collection) {
            long count = collection.stream().filter(t -> t.compareTo(newTicket) < 0).count();
            if (count == 0) return 0;
            collection.removeIf(ticket -> ticket.compareTo(newTicket) < 0);
            sortCollectionByLocation();
            return (int) count;
        }
    }

    public int removeAllByPrice(Long price) {
        synchronized (collection) {
            long count = collection.stream().filter(t -> Objects.equals(price, t.getPrice()))
                    .count();
            if (count == 0) return 0;
            collection.removeIf(ticket -> Objects.equals(price, ticket.getPrice()));
            sortCollectionByLocation();
            return (int) count;
        }
    }

    public boolean removeByType(TicketType type) {
        synchronized (collection) {
            Optional<Ticket> ticketRemoved = collection.stream()
                    .filter(t -> t.getType() == type).findFirst();
            if (ticketRemoved.isPresent()){
                collection.remove(ticketRemoved.get());
                sortCollectionByLocation();
                return true;
            }
            return false;
        }
    }

    public Ticket getMinByVenue() {
        synchronized (collection) {
            return collection.stream().min(Comparator.comparing(Ticket::getVenue,
                    Comparator.nullsFirst(Comparator.naturalOrder()))).orElse(null);
        }
    }

    public String getInfo() {
        synchronized (collection) {
            return String.format("Тип коллекции: %s\nДата инициализации: %s\nРазмер коллекции: %d",
                    collection.getClass().getSimpleName(), date, collection.size());
        }
    }

    public String showAll() {
        synchronized (collection) {
            if (collection.isEmpty()) return "Коллекция пустая";
            return collection.stream().sorted((t1, t2) -> {
                String loc1 = getTownName(t1);
                String loc2 = getTownName(t2);
                return loc1.compareTo(loc2);
            }).map(Ticket::toString).collect(Collectors.joining("\n"));
        }
    }

    public Map<Integer, String> getOwnerMap() {
        return dbManager.getTicketOwners();
    }

    public ArrayDeque<Ticket> getCollection() {
        synchronized (collection) {
            return new ArrayDeque<>(collection);
        }
    }

}
