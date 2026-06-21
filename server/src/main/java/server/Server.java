package server;

import common.network.Request;
import common.network.Response;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final int READ_THREADS = 2;
    private static final int PROCESS_THREADS = 4;
    private final NetworkProvider network;
    private final RequestHandler requestHandler;
    private final ExecutorService readPool;
    private final ExecutorService processPool;
    private final CollectionManager collectionManager;
    private final CommandExecutor executor;
    private volatile boolean running = true;

    public Server(int port, String dbUser, String dbPassword) throws SocketException {
        DatabaseManager dbManager = new DatabaseManager(dbUser, dbPassword);
        AuthManager authManager = new AuthManager(dbManager);
        this.collectionManager = new CollectionManager(dbManager);
        this.network = new NetworkProvider(port);
        this.executor = new CommandExecutor(collectionManager, dbManager, authManager);
        this.requestHandler = new RequestHandler(executor, authManager);
        this.readPool = Executors.newFixedThreadPool(READ_THREADS);
        this.processPool = Executors.newFixedThreadPool(PROCESS_THREADS);

        logger.info("Сервер инициализирован на порту" + port);
    }

    public void start() {
        logger.info("Сервер запущен...");

        while (running) {
            try {
                DatagramPacket packet = network.receive();
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                byte[] data = network.getData(packet);
                readPool.submit(()-> {
                    try {
                        Request request = network.deserialRequest(data);
                        logger.info("Запрос " + request.getCommandName() + "от " + clientAddress);
                        processPool.submit(() ->{
                            try {
                                Response response = requestHandler.handle(request, clientAddress, clientPort);
                                new Thread(() -> {
                                    try {
                                        network.sendResponse(response, clientAddress, clientPort);
                                    } catch (Exception e){
                                        logger.severe("Ошибка отправки " + e.getMessage());
                                    }
                                }).start();


                            } catch (Exception e){
                                logger.severe("Ошибка обработки" + e.getMessage());
                                new Thread(() -> {
                                    try {
                                        network.sendResponse(new Response(false, "Ошибка " + e.getMessage()), clientAddress, clientPort);
                                    } catch (Exception ex){
                                        logger.severe("Ошибка отправки неисправности" + ex.getMessage());
                                    }
                                }).start();
                            }
                        });
                    } catch (Exception e) {
                        logger.severe("Ошибка десериализации: " + e.getMessage());
                        new Thread(() -> {
                            try {
                                network.sendResponse(new Response(false, "Ошибка пакета: " + e.getMessage()), clientAddress, clientPort);
                            } catch (Exception ex) {
                                logger.severe("Ошибка отправки: " + ex.getMessage());
                            }
                        }).start();
                    }
                });

            } catch (Exception e) {
                if (running) logger.severe("Ошибка цикла: " + e.getMessage());
            }
        }
        stop();
    }

    public void stop() {
        running = false;
        network.close();
        readPool.shutdown();
        processPool.shutdown();
        try {
            if (!readPool.awaitTermination(5, TimeUnit.SECONDS) || !processPool.awaitTermination(5, TimeUnit.SECONDS)) {
                readPool.shutdownNow();
                processPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            readPool.shutdownNow();
            processPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Сервер остановлен.");
    }

    public static void main(String[] args){
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        String dbUser = System.getenv().getOrDefault("DB_USER", "postgres");
        String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "");
        try {
            Server server = new Server(port, dbUser, dbPassword);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Завершение работы сервера...");
                server.stop();
            }));
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}