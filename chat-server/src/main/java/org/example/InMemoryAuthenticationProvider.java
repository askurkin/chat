package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InMemoryAuthenticationProvider implements AuthenticationProvider, AutoCloseable {
	private final List<User> users;

	private String dbFile = "chat-server\\chatdb.db";
	Connection connection;

	public InMemoryAuthenticationProvider() {
		this.users = new ArrayList<>();
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
		} catch (SQLException ex) {
			throw new RuntimeException(dbFile + " not found" + ex.getMessage());
		}
		loadFromDB(users);
	}

	public void loadFromDB(List<User> user_load) {
		String sql = "SELECT USER_NAME, USER_PASS, USER_NICK FROM USER_LIST";
		try (Statement statement = connection.createStatement();
			 ResultSet rs = statement.executeQuery(sql)) {
			while (rs.next()) {
				User user = new User(rs.getString("USER_NAME"), rs.getString("USER_PASS"), rs.getString("USER_NICK"));
				if (user.getUsername().toLowerCase().equals("admin")) {
					user.setRole(UserRoles.ADMIN);
				}
				user_load.add(user);
				System.out.println("load " + user.getUsername() + " role=" + user.getRole());
			}
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	@Override
	public String getUsernameByLoginAndPassword(String login, String password) {
		for (User user : users) {
			if (Objects.equals(user.getPassword(), password) && Objects.equals(user.getLogin(), login)) {
				return user.getUsername();
			}
		}
		return null;
	}

	@Override
	public UserRoles getUserRole(String username) {
		for (User user : users) {
			if (user.getUsername().equals(username)) {
				return user.getRole();
			}
		}
		return null;
	}

	@Override
	public synchronized boolean register(String login, String password, String username) {
		for (User user : users) {
			if (Objects.equals(user.getUsername(), username) && Objects.equals(user.getLogin(), login)) {
				return false;
			}
		}
		users.add(new User(login, password, username));

		String sql = "insert into user_list ( user_name, user_pass, user_nick) values ( ?, ?, ? )";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, login);
			ps.setString(2, password);
			ps.setString(3, username);
			ps.executeUpdate();
		} catch (SQLException ex) {
			throw new RuntimeException(ex.getMessage());
		}

		return true;
	}

	@Override
	public void close() throws SQLException {
		if (connection != null && !connection.isClosed()) {
			connection.close();
		}
	}
}
