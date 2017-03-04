package com.flash.memcached.client;

import java.io.*;
import java.net.*;

/**
 * <p>
 * Creation Date: 2/27/2017 <br>
 * Creation Time: 7:32 AM <br>
 * </p>
 *
 * @author Jichao Zhang
 */

public class CommandServiceImpl implements CommandService {
    private String host;
    private int port;
    public CommandServiceImpl(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String executeCmd(String cmd) {
        try {
            Socket socket = openSocket(host, port);
            String result = writeToAndReadFromSocket(socket, cmd);
            socket.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String writeToAndReadFromSocket(Socket socket, String writeTo) throws Exception {
        try {
            // write text to the socket
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(writeTo);
            bufferedWriter.flush();

            // read text from the socket
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str + "\n");
            }

            // close the reader, and return the results as a String
            bufferedReader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Open a socket connection to the given server on the given port.
     * This method currently sets the socket timeout value to 10 seconds.
     * (A second version of this method could allow the user to specify this timeout.)
     */
    private Socket openSocket(String server, int port) throws Exception {
        Socket socket;

        // create a socket with a timeout
        try {
            InetAddress inetAddress = InetAddress.getByName(server);
            SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);

            // create a socket
            socket = new Socket();

            // this method will block no more than timeout ms.
            int timeoutInMs = 10 * 1000;   // 10 seconds
            System.out.println(socketAddress);
            socket.connect(socketAddress, timeoutInMs);

            return socket;
        } catch (SocketTimeoutException ste) {
            System.err.println("Timed out waiting for the socket.");
            ste.printStackTrace();
            throw ste;
        }
    }

}
