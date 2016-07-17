package server;


public class Main {
	public static final int Port = 8283; // Порт на котором работает чат

	public static void main(String[] args) {
		
		System.out.println("Сервер чата успешно запущен");

		new Server();
		
		}

	}