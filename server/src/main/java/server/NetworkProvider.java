package server;
import common.network.Request;
import common.network.Response;
import java.io.*;
import java.net.*;
import java.util.Arrays;

public class NetworkProvider {
    private final DatagramSocket socket;

    public NetworkProvider(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
    }

    public DatagramPacket receive() throws IOException {
        byte[] buffer = new byte[65507];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return packet;
    }
    public byte[] getData(DatagramPacket packet){
        return Arrays.copyOf(packet.getData(), packet.getLength());
    }
    public Request deserialRequest(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (Request) ois.readObject();
        }
    }

    public void sendResponse(Response response, InetAddress address, int port) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(response);
            byte[] data = baos.toByteArray();
            socket.send(new DatagramPacket(data, data.length, address, port));
        }
    }

    public void close() {
        if (socket != null && !socket.isClosed()) socket.close();
    }
}
