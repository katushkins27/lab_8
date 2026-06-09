package server;
import common.data.*;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    //private static final String URL = "jdbc:postgresql://pg/studs";
    private static final String URL = "jdbc:postgresql://localhost:5432/studs";
    private final String username;
    private final String password;

    public DatabaseManager(String username, String password){
        this.username = username;
        this.password = password;
        initTables();
    }

    private Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL, username,password);
    }

    private void initTables(){
        String createUsersSeq = """
    CREATE SEQUENCE IF NOT EXISTS users_id_seq
    """;

        String createVenuesSeq = """
    CREATE SEQUENCE IF NOT EXISTS venues_id_seq
    """;

        String createTicketsSeq = """
    CREATE SEQUENCE IF NOT EXISTS tickets_id_seq
    """;
        String createUsers= """
                CREATE TABLE IF NOT EXISTS users(
                id INT PRIMARY KEY DEFAULT nextval('users_id_seq'),
                login VARCHAR(50) UNIQUE NOT NULL,
                password_hash VARCHAR(32) NOT NULL
            )
        """;

        String createTickets = """
            CREATE TABLE IF NOT EXISTS tickets (
                id INT PRIMARY KEY DEFAULT nextval('tickets_id_seq'),
                name VARCHAR(100) NOT NULL,
                coord_x INT NOT NULL,
                coord_y BIGINT NOT NULL,
                creation_date TIMESTAMP NOT NULL,
                price BIGINT CHECK (price > 0),
                type VARCHAR(20) NOT NULL,
                venue_id INT REFERENCES venues(id) ON DELETE SET NULL,
                user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE
            )
        """;

        String createVenues = """
            CREATE TABLE IF NOT EXISTS venues (
                id INT PRIMARY KEY DEFAULT nextval('venues_id_seq'),
                name VARCHAR(100) NOT NULL,
                capacity INT NOT NULL CHECK (capacity > 0),
                street VARCHAR(61) NOT NULL,
                zip_code VARCHAR(20),
                location_x DOUBLE PRECISION,
                location_y BIGINT,
                location_z REAL,
                location_name VARCHAR(777)
            )
        """;

        try (Connection connect = getConnection()){
            connect.createStatement().execute(createUsersSeq);
            connect.createStatement().execute(createVenuesSeq);
            connect.createStatement().execute(createTicketsSeq);
            connect.createStatement().execute(createUsers);
            connect.createStatement().execute(createVenues);
            connect.createStatement().execute(createTickets);
            System.out.println("Таблицы БД иницилизированы");
        } catch (SQLException e){
            System.err.println("Ошибка иницилизации БД: "+e.getMessage());
        }
    }

    public Optional<Integer> authenticate(String login, String passwordHash){
        String sql = "SELECT id FROM users WHERE login = ? AND password_Hash = ?";
        try (Connection connect = getConnection();
        PreparedStatement stmt = connect.prepareStatement(sql)){
            stmt.setString(1,login);
            stmt.setString(2,passwordHash);
            ResultSet results = stmt.executeQuery();
            if (results.next()){
                return Optional.of(results.getInt("id"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean registers(String login, String passwordHash){
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?)";
        try (Connection connect = getConnection();
        PreparedStatement stmt = connect.prepareStatement(sql)){
            stmt.setString(1,login);
            stmt.setString(2,passwordHash);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e){
            return false;
        }
    }

    public boolean userExists(String login){
        String sql = "SELECT id FROM users WHERE login = ?";
        try (Connection connect = getConnection();
        PreparedStatement stmt = connect.prepareStatement(sql)){
            stmt.setString(1,login);
            ResultSet results = stmt.executeQuery();
            return results.next();
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean ticketExists(int id, int userId){
        String sql = "SELECT id FROM tickets WHERE id = ? AND user_id = ?";
        try (Connection connect = getConnection();
        PreparedStatement stmt = connect.prepareStatement(sql)){
            stmt.setInt(1,id);
            stmt.setInt(2,userId);
            ResultSet results = stmt.executeQuery();
            return results.next();
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Ticket> getAllTickets(){
        List<Ticket> tickets = new ArrayList<>();
        String sql = """
                SELECT
                t.id as ticket_id,
                t.name as ticket_name,
                t.coord_x,
                t.coord_y,
                t.creation_date,
                t.price,
                t.type,
                t.venue_id,
                t.user_id,
                v.id as venue_id,
                v.name as venue_name,
                v.capacity,
                v.street,
                v.zip_code,
                v.location_x,
                v.location_y,
                v.location_z,
                v.location_name
                FROM tickets t
                LEFT JOIN venues v ON t.venue_id = v.id
                ORDER BY t.id
            """;
        try (Connection connect = getConnection();
        Statement stmt = connect.createStatement();
        ResultSet results = stmt.executeQuery(sql)){
            while (results.next()){
                Ticket ticket = extractTicket(results);
                tickets.add(ticket);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return tickets;
    }

    public Ticket addTicket(Ticket ticket, int userId){
        String sql = """
               INSERT INTO tickets (name, coord_x, coord_y, creation_date, price, type, venue_id, user_id)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
             """;
        try (Connection connect = getConnection();
             PreparedStatement stmt = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            stmt.setString(1, ticket.getName());
            stmt.setInt(2,ticket.getCoordinates().getX());
            stmt.setLong(3,ticket.getCoordinates().getY());
            stmt.setTimestamp(4, Timestamp.valueOf(ticket.getCreationDate()));
            stmt.setObject(5, ticket.getPrice());
            stmt.setString(6, ticket.getType().name());

            Long venueId = null;
            if (ticket.getVenue()!=null){
                venueId = saveVenue (connect, ticket.getVenue());
                ticket.getVenue().setID(venueId);
            }
            stmt.setObject(7,venueId);
            stmt.setInt(8,userId);
            stmt.executeUpdate();
            ResultSet results = stmt.getGeneratedKeys();
            if (results.next()){
                ticket.setID(results.getInt(1));
            }
            return ticket;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateTicket (int id , Ticket newTicket, int userId){
        String sql = """
                UPDATE tickets
                            SET name = ?, coord_x = ?, coord_y = ?, price = ?, type = ?, venue_id = ?
                            WHERE id = ? AND user_id = ?
                """;
        try (Connection connect = getConnection();
             PreparedStatement stmt = connect.prepareStatement(sql)){
             stmt.setString(1, newTicket.getName());
             stmt.setInt(2, newTicket.getCoordinates().getX());
             stmt.setLong(3, newTicket.getCoordinates().getY());
             stmt.setObject(4,newTicket.getPrice());
             stmt.setString(5,newTicket.getType().name());

             Long venueId = null;
             if (newTicket.getVenue() != null){
                 venueId = saveVenue(connect, newTicket.getVenue());
             }
             stmt.setObject(6, venueId);

             stmt.setInt(7,id);
             stmt.setInt(8,userId);
             int updated = stmt.executeUpdate();
             return updated>0;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTicket(int id, int userId) {
        String sql = "DELETE FROM tickets WHERE id = ? AND user_id = ?";
        try (Connection connect = getConnection();
             PreparedStatement stmt = connect.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, userId);
            int deleted = stmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Long saveVenue(Connection connect, Venue venue) throws SQLException{
        if (venue.getID() > 0) {
            String sql = """
            UPDATE venues 
            SET name = ?, capacity = ?, street = ?, zip_code = ?,
                location_x = ?, location_y = ?, location_z = ?, location_name = ?
            WHERE id = ?
            """;
            try (PreparedStatement stmt = connect.prepareStatement(sql)) {
                stmt.setString(1, venue.getName());
                stmt.setInt(2, venue.getCapacity());
                stmt.setString(3, venue.getAddress().getStreet());
                stmt.setString(4, venue.getAddress().getZipCode());

                Location loc = venue.getAddress().getTown();
                if (loc != null) {
                    stmt.setDouble(5, loc.getX());
                    stmt.setLong(6, loc.getY());
                    stmt.setFloat(7, loc.getZ());
                    stmt.setString(8, loc.getName());
                } else {
                    stmt.setNull(5, Types.DOUBLE);
                    stmt.setNull(6, Types.BIGINT);
                    stmt.setNull(7, Types.REAL);
                    stmt.setNull(8, Types.VARCHAR);
                }

                stmt.setLong(9, venue.getID());
                stmt.executeUpdate();
                return venue.getID();
            }
        }

        String sql = """
            INSERT INTO venues (name, capacity, street, zip_code,
                                location_x, location_y, location_z, location_name)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = connect.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            stmt.setString(1, venue.getName());
            stmt.setInt(2,venue.getCapacity());
            stmt.setString(3,venue.getAddress().getStreet());
            stmt.setString(4,venue.getAddress().getZipCode());
            Location loc = venue.getAddress().getTown();
            if (loc != null){
                stmt.setDouble(5,loc.getX());
                stmt.setLong(6,loc.getY());
                stmt.setFloat(7, loc.getZ());
                stmt.setString(8,loc.getName());
            } else{
                stmt.setNull(5, Types.DOUBLE);
                stmt.setNull(6, Types.BIGINT);
                stmt.setNull(7, Types.REAL);
                stmt.setNull(8, Types.VARCHAR);
            }
            stmt.executeUpdate();
            ResultSet results = stmt.getGeneratedKeys();
            if (results.next()){
                return results.getLong(1);
            }
        }
        return null;
    }

    private Ticket extractTicket(ResultSet results) throws SQLException{
        Ticket ticket = new Ticket();
        ticket.setID(results.getInt("ticket_id"));
        ticket.setName(results.getString("ticket_name"));

        Coordinates coords = new Coordinates();
        coords.setX(results.getInt("coord_x"));
        coords.setY(results.getLong("coord_y"));
        ticket.setCoordinates(coords);

        ticket.setCreationDate(results.getTimestamp("creation_date").toLocalDateTime());

        Long price = results.getObject("price",Long.class);
        ticket.setPrice(price);
        ticket.setType(TicketType.valueOf(results.getString("type")));
        long venueId = results.getLong("venue_id");
        if (!results.wasNull()){
            Venue venue = new Venue();
            venue.setID(venueId);
            venue.setName(results.getString("venue_name"));
            venue.setCapacity(results.getInt("capacity"));

            Address address = new Address();
            address.setStreet(results.getString("street"));
            address.setZipCode(results.getString("zip_code"));

            double locX = results.getDouble("location_x");
            if (!results.wasNull()){
                Location location = new Location();
                location.setX(locX);
                location.setY(results.getLong("location_y"));
                location.setZ(results.getFloat("location_z"));
                address.setTown(location);
            }
            venue.setAddress(address);
            ticket.setVenue(venue);
        }
        return ticket;
    }
    public Map<Integer, String> getTicketOwners() {
        Map<Integer, String> owners = new HashMap<>();
        String sql = """
            SELECT t.id as ticket_id, u.login
            FROM tickets t
            JOIN users u ON t.user_id = u.id
            """;
        try (Connection connect = getConnection();
             Statement stmt = connect.createStatement();
             ResultSet results = stmt.executeQuery(sql)) {
            while (results.next()) {
                owners.put(results.getInt("ticket_id"), results.getString("login"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return owners;
    }
}
