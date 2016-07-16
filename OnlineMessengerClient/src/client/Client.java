package client;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import main.Main;

public class Client extends JFrame implements KeyListener, MouseListener {

	
	private static final long serialVersionUID = 1L;
	private Resender resend = new Resender();
	private StringBuilder messages = new StringBuilder();
	private JTextPane chatwindow = new JTextPane();
	private JTextPane textin = new JTextPane();
	private SimpleAttributeSet attribs = new SimpleAttributeSet();
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;
	private String ip;
	private String message;
	private boolean isConnected = false;

	public Client() {
		//Создание GUI
		super("Chat Messenger");
		setSize(400, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1));
		StyleConstants.setFontSize(attribs, 15);
		chatwindow.setParagraphAttributes(attribs, true);
		chatwindow.setBackground(Color.LIGHT_GRAY);
		chatwindow.setEditable(false);
		JScrollPane textoutscroll = new JScrollPane(chatwindow);
		textoutscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		DefaultCaret caret = (DefaultCaret)chatwindow.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		panel.add(textoutscroll);
		
		textin.setParagraphAttributes(attribs, true);
		textin.setText("Введите сообщение тут...");
		textin.addKeyListener(this);
		textin.addMouseListener(this);
		textin.setBackground(Color.LIGHT_GRAY);
		panel.add(textin);
		
		setContentPane(panel);
		setVisible(true);
		
		chatwindow.setText(messages.append("Чат запущен, добро пожаловать!").append("\n").toString());
		chatwindow.setText(messages.append("Введите IP для подключения к серверу.").append("\n").toString());
		chatwindow.setText(messages.append("Формат: xxx.xxx.xxx.xxx").append("\n").toString());
		
	}
	
	private void connect(){

		try {
			// Подключаемся к серверу и получаем потоки(in и out) для передачи сообщений
			socket = new Socket(ip, Main.Port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"), true);

			chatwindow.setText(messages.append("Введите свой ник:").append("\n").toString());

			// Запускаем вывод всех входящих сообщений
			resend.start();
			isConnected = true;

		} catch (Exception e) {
			StyleConstants.setForeground(attribs, Color.RED);
			chatwindow.setParagraphAttributes(attribs, true);
			chatwindow.setText(messages.append("Сервер недоступен.").append("\n").toString());
			e.printStackTrace();
		}
	}

	// Закрывает входной и выходной потоки и сокет
	private void close() {
		try {
			in.close();
			out.close();
			socket.close();
			isConnected = false;
		} catch (Exception e) {
			chatwindow.setText(messages.append("Ошибка закрытия потоков!").append("\n").toString());
			e.printStackTrace();
		}
	}

	// Принимает все сообщения от сервера, в отдельном потоке.
	private class Resender extends Thread {

		// Считывает все сообщения от сервера и выводит их в окно чата.
		@Override
		public void run() {
			try {
				while (isConnected) {
					String inputmessage = in.readLine();
					if(inputmessage != null){
					chatwindow.setText(messages.append(inputmessage).append("\n").toString());
					}
				}
			} catch (IOException e) {
				chatwindow.setText(messages.append("Ошибка при получении сообщений.").append("\n").toString());
				e.printStackTrace();
			}
		}
	}
	
	public void mouseClicked(MouseEvent e){
		if(textin.getText().equals("Введите сообщение тут...")){
		textin.setText(null);
		}
    }

	// Обработка нажатия кнопки ввода. Ввод ip, проверка и отправка сообщения на сервер
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER && textin.getText() != null) {
        	
          if(!isConnected){
        	ip = textin.getText();
        	connect();
        	e.consume();
        	textin.setText(null);
           } else {
        	message = textin.getText();
        	out.println(message);
        	e.consume();
        	textin.setText(null);
        	
        	if(message.equals("exit")){
        		close();
        		System.exit(0);
        	}
        }
        }
    }
    
	public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

}
