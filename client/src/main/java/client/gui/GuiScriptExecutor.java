package client.gui;
import common.auth.Credentials;
import common.data.*;
import common.network.Request;
import common.network.Response;
import java.io.*;
import java.util.*;
public class GuiScriptExecutor {
    private final client.NetworkManager network;
    private final Credentials credentials;
    private final Set<String> executingScripts = new HashSet<>();

    public GuiScriptExecutor(client.NetworkManager network, Credentials credentials){
        this.network = network;
        this.credentials = credentials;
    }

    public void executeScript(String filePath) throws IOException{
        if (executingScripts.contains(filePath)){
            throw new IOException("Рекурсивный вызов скрипта: " + filePath);
        }
        executingScripts.add(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            Queue<String> lines = new LinkedList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            processLines(lines, filePath);
        } finally {
            executingScripts.remove(filePath);
        }
    }

    private void processLines(Queue<String> lines, String scriptPath) throws IOException{
        while (!lines.isEmpty()){
            String raw = lines.poll();
            if (raw == null) break;
            raw = raw.trim();
            if (raw.isEmpty() || raw.startsWith("#")) continue;
            String[] parts = raw.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1].trim() : "";
            if (command.equals("exit")) break;
            if (command.equals("execute_script")){
                if (!arg.isEmpty()){
                    File f = new File(arg);
                    if (!f.isAbsolute()){
                        File base = new File(scriptPath).getParentFile();
                        f = new File(base, arg);
                    }
                    executeScript(f.getAbsolutePath());
                }
                continue;
            }
            Request req = buildRequest(command , arg, lines);
            if (req != null){
                network.sendWithRetry(req);
            }
        }
    }

    private Request buildRequest(String command, String arg, Queue<String> lines){
        switch (command){
            case "add": {
                Ticket t = readTicketFromQueue(lines);
                return t != null ? new Request("add", t, credentials) : null;
            }
            case "update": {
                if (arg.isEmpty()) return null;
                Ticket t = readTicketFromQueue(lines);
                return t != null ? new Request("update", arg, t, credentials) : null;
            }
            case "remove_greater": {
                Ticket t = readTicketFromQueue(lines);
                return t != null ? new Request("remove_greater", t, credentials) : null;
            }
            case "remove_any_by_type": {
                try {
                    TicketType type = TicketType.valueOf(arg.toUpperCase());
                    return new Request("remove_any_by_type", type, credentials);
                } catch (IllegalArgumentException e) { return null; }
            }
            case "remove_all_by_price": {
                if (arg.isEmpty()) return null;
                try {
                    Long price = Long.valueOf(arg);
                    return new Request("remove_all_by_price", price, credentials);
                } catch (NumberFormatException e) { return null; }
            }
            case "remove_by_id":
            case "head":
            case "remove_head":
            case "clear":
            case "show":
            case "info":
            case "help":
            case "min_by_venue":
                return new Request(command, arg, credentials);
            default:
                return null;
        }
    }

    private Ticket readTicketFromQueue(Queue<String> lines){
        try{
            String name = nextLine(lines);
            int coordX = Integer.parseInt(nextLine(lines));
            long coordY = Long.parseLong(nextLine(lines));
            String priceStr = nextLine(lines);
            Long price = priceStr.isEmpty() ? null : Long.parseLong(priceStr);
            TicketType type = TicketType.valueOf(nextLine(lines).toUpperCase());
            long venueId = Long.parseLong(nextLine(lines));
            String venueName = nextLine(lines);
            int venueCap = Integer.parseInt(nextLine(lines));
            String street = nextLine(lines);
            String zip = nextLine(lines);
            double locX = Double.parseDouble(nextLine(lines));
            long locY = Long.parseLong(nextLine(lines));
            float locZ = Float.parseFloat(nextLine(lines));
            String locName = nextLine(lines);
            Location loc = new Location(locName.isEmpty() ? null : locName, locX, locY, locZ);
            Address addr = new Address(street, zip.isEmpty() ? null : zip, loc);
            Venue venue = new Venue(venueId, venueName, venueCap, addr);
            Ticket t = new Ticket();
            t.setName(name);
            t.setCoordinates(new Coordinates(coordX, coordY));
            t.setPrice(price);
            t.setType(type);
            t.setCreationDate(java.time.LocalDateTime.now());
            t.setVenue(venue);
            return t;
        } catch (Exception e){
            return null;
        }
    }

    private String nextLine(Queue<String> lines){
        while (!lines.isEmpty()){
            String l = lines.poll();
            if (l!= null){
                l=l.trim();
                if (!l.startsWith("#")) return l;
            }
        }
        return "";
    }
}
