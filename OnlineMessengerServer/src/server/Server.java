package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import main.Main;

public class Server {

	
	 // Специальная "обёртка" для ArrayList, которая обеспечивает доступ к массиву из разных потоков
	private List<Connection> connections = 
			Collections.synchronizedList(new ArrayList<Connection>());
	private ServerSocket server;

	// Конструктор создаёт сервер-сокет, затем для каждого подключения создаётся объект Connection 
	// и добавляет его в список подключений.
	public Server() {
		try {
			server = new ServerSocket(Main.Port);

			while (true) {
				Socket socket = server.accept();

				// Создаём объект Connection и добавляем его в список
				Connection connection = new Connection(socket);
				connections.add(connection);

				// Инициализирует поток и запускает метод run(),
				// которая выполняется одновременно с остальной программой
				connection.start();

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeAll();
		}
	}

	// Закрывает все потоки, всех соединений, а также серверный сокет
	private void closeAll() {
		try {
			server.close();
			
			// Перебор всех Connection и вызов метода close() для каждого. Блок
			// synchronized {} необходим для правильного доступа к одним данным иp разных потоков
			synchronized(connections) {
				Iterator<Connection> iter = connections.iterator();
				while(iter.hasNext()) {
					((Connection) iter.next()).close();
				}
			}
		} catch (Exception e) {
			System.err.println("Ошибка закрытия потоков!");
		}
	}

	// Класс содержит данные, относящиеся к конкретному подключению:
	// имя пользователя, сокет, входной поток BufferedReader, выходной поток PrintWriter
	// Расширяет Thread и в методе run() получает информацию от пользователя и пересылает её другим
	private class Connection extends Thread {
		private BufferedReader in;
		private PrintWriter out;
		private Socket socket;
	
		// Инициализирует поля объекта и получает имя пользователя
		// socket сокет, полученный из server.accept()
		public Connection(Socket socket) {
			this.socket = socket;
	
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
				out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"), true);
	
			} catch (IOException e) {
				e.printStackTrace();
				close();
			}
		}
	
		// Запрашивает имя пользователя, проверяет его и ожидает от него сообщений.
		// При получении сообщения, оно вместе с именем пользователя пересылается всем остальным.
		@Override
		public void run() {
			String name = "Unnamed";
			try {
				if(!in.readLine().isEmpty()){
				name = in.readLine();
				}
				
				// Отправляем всем клиентам сообщение о том, что зашёл новый пользователь
				synchronized(connections) {
					Iterator<Connection> iter = connections.iterator();
					while(iter.hasNext()) {
						((Connection) iter.next()).out.println(name + " присоеденился");
						System.out.println(name + " присоеденился");
					}
				}
				
				String message = "";
				while (true) {
					message = in.readLine();
					if(message.equals("exit")) break;
					
					// Отправляем всем клиентам очередное сообщение, проверив его на пустые символы
					synchronized(connections) {
						if(!message.isEmpty() && !message.startsWith(" ") && !message.startsWith(" ")){
						Iterator<Connection> iter = connections.iterator();
						while(iter.hasNext()) {
							((Connection) iter.next()).out.println(name + ": " + message);
							System.out.println(name + ": " + message);
						}
					}
					}
				}
				
				// Отправляем всем клиентам сообщение о том, что пользователь покинул чат
				synchronized(connections) {
					Iterator<Connection> iter = connections.iterator();
					while(iter.hasNext()) {
						((Connection) iter.next()).out.println(name + " покинул чат");
						System.out.println(name + " покинул чат");
					}
				}
			} catch (IOException e) {
				System.out.println(name + " " + e.getMessage());
				System.out.println(name + " покинул чат");
				out.println(name + " покинул чат");
			} finally {
				close();
			}
		}
	
		// Закрывает входной, выходной потоки и сокет
		public void close() {
			try {
				in.close();
				out.close();
				socket.close();
				connections.remove(this);

			} catch (Exception e) {
				System.err.println("Ошибка закрытия потоков!");
			}
		}
	}
}
