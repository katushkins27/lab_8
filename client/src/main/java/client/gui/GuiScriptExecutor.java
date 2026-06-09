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
                Ticket t = readTicketFromQueue(lines, false);
                return t != null ? new Request("add", t, credentials) : null;
            }
            case "update": {
                if (arg.isEmpty()) return null;
                Ticket t = readTicketFromQueue(lines, true);
                if (t != null) {
                    t.setID(Integer.parseInt(arg));
                    return new Request("update", t, credentials);
                }
                return null;
            }
            case "remove_greater": {
                Ticket t = readTicketFromQueue(lines, false);
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

    private Ticket readTicketFromQueue(Queue<String> lines, boolean isUpdate) {
        try {
            Ticket t = new Ticket();

            String name = nextLine(lines);
            if (name.isEmpty()) throw new IllegalArgumentException("Имя пустое");
            t.setName(name);

            int coordX = Integer.parseInt(nextLine(lines));
            long coordY = Long.parseLong(nextLine(lines));
            t.setCoordinates(new Coordinates(coordX, coordY));

            String priceStr = nextLine(lines);
            if (!priceStr.isEmpty()) t.setPrice(Long.parseLong(priceStr));

            t.setType(TicketType.valueOf(nextLine(lines).toUpperCase()));
            t.setCreationDate(java.time.LocalDateTime.now());

            String venueName = nextLine(lines);

            if (!venueName.isEmpty()) {
                int venueCap = Integer.parseInt(nextLine(lines));
                String street = nextLine(lines);
                String zip = nextLine(lines);

                String lxStr = nextLine(lines);
                String lyStr = nextLine(lines);
                String lzStr = nextLine(lines);
                String locName = nextLine(lines);

                Location loc = null;
                if (!lxStr.isEmpty() || !lyStr.isEmpty() || !lzStr.isEmpty() || !locName.isEmpty()) {
                    double lx = lxStr.isEmpty() ? 0.0 : Double.parseDouble(lxStr);
                    Long ly = Long.parseLong(lyStr);
                    Float lz = Float.parseFloat(lzStr);

                    loc = new Location(locName.isEmpty() ? null : locName, lx, ly, lz);
                }

                Address addr = new Address(street, zip.isEmpty() ? null : zip, loc);

                int venueId;
                if (isUpdate) {
                    String venueIdStr = nextLine(lines);
                    venueId = venueIdStr.isEmpty() ? 0 : Integer.parseInt(venueIdStr);
                } else {
                    venueId = 0;
                }
                Venue venue = new Venue(venueId, venueName, venueCap, addr);
                t.setVenue(venue);
            }
            return t;

        } catch (Exception e) {
            System.err.println("Ошибка валидации билета в скрипте: " + e.getMessage());
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
