package me.johnnywoof.databases;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class FileDatabase implements Database {

	private final ConcurrentHashMap<String, PlayerData> cache = new ConcurrentHashMap<>();

	private final File savedData;

	public FileDatabase(File savedData) {

		this.savedData = savedData;

	}

	public String getIP(String username) {

		PlayerData playerData = this.cache.get(username);

		if (playerData != null) {

			return playerData.ipAddress;

		} else {

			try {

				playerData = this.loadPlayerData(username);

			} catch (IOException e) {
				e.printStackTrace();
			}

			if (playerData != null) {

				this.cache.put(username, playerData);

				return playerData.ipAddress;

			}

		}

		return null;

	}

	public UUID getUUID(String username) {

		PlayerData playerData = this.cache.get(username);

		if (playerData != null) {

			return playerData.uuid;

		} else {

			try {

				playerData = this.loadPlayerData(username);

			} catch (IOException e) {
				e.printStackTrace();
			}

			if (playerData != null) {

				this.cache.put(username, playerData);

				return playerData.uuid;

			}

		}

		return null;

	}

	public void updatePlayer(String username, String ip, UUID uuid) {

		this.cache.put(username, new PlayerData(ip, uuid));

		try {

			this.save();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void save() throws Exception {

		ArrayList<String> existingLines = new ArrayList<>();

		if (this.savedData.exists()) {

			BufferedReader br = new BufferedReader(new FileReader(this.savedData));

			String l;

			while ((l = br.readLine()) != null) {

				existingLines.add(l);

			}

			br.close();

			ArrayList<String> toRemove = new ArrayList<>();

			for (String key : this.cache.keySet()) {

				for (String line : existingLines) {

					if (line.startsWith(key + "|")) {

						toRemove.add(line);

					}

				}

			}

			existingLines.removeAll(toRemove);

			toRemove.clear();

		}

		PrintWriter w = new PrintWriter(this.savedData);

		for (String line : existingLines) {

			w.println(line);

		}

		for (Map.Entry<String, PlayerData> en : this.cache.entrySet()) {

			w.println(en.getKey() + "|" + en.getValue().ipAddress + "|" + en.getValue().uuid.toString());

		}

		w.close();

	}

	@Override
	public void resetCache() {
		this.cache.clear();
	}

	private PlayerData loadPlayerData(String username) throws IOException {

		if (!this.savedData.exists()) {
			return null;
		}

		BufferedReader br = new BufferedReader(new FileReader(this.savedData));

		String l;

		String startWithKey = username + "|";

		PlayerData playerData = null;

		while ((l = br.readLine()) != null) {

			if (l.startsWith(startWithKey)) {

				String[] data = l.split(Pattern.quote("|"));

				playerData = new PlayerData(data[1], UUID.fromString(data[2]));

				break;

			}

		}

		br.close();

		return playerData;

	}

}
