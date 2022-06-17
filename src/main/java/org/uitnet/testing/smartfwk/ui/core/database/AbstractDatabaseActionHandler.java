/*
 * SmartTestAutoFramework
 * Copyright 2021 and beyond
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.uitnet.testing.smartfwk.ui.core.database;

import java.util.Calendar;
import java.util.List;

import org.testng.Assert;
import org.uitnet.testing.smartfwk.api.core.reader.JsonDocumentReader;
import org.uitnet.testing.smartfwk.ui.core.config.DatabaseProfile;

import com.jayway.jsonpath.DocumentContext;

/**
 * 
 * @author Madhav Krishna
 *
 */
public abstract class AbstractDatabaseActionHandler<T> implements DatabaseConnectionProvider<T> {
	protected String appName;
	protected DatabaseManager databaseManager;
	protected String targetServerName;
	protected String activeDatabaseProfileName;
	protected DatabaseProfile activeDatabaseProfile;
	protected int sessionExpiryDurationInSeconds;
	protected long lastRequestAccessTimeInMs;

	protected DatabaseConnection<T> connection;

	public AbstractDatabaseActionHandler(String appName, int sessionExpiryDurationInSeconds) {
		this.appName = appName;
		this.sessionExpiryDurationInSeconds = sessionExpiryDurationInSeconds;
		databaseManager = SmartDatabaseManager.getInstance();
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public void setTargetServerName(String targetServerName) {
		this.targetServerName = targetServerName;
	}

	public DatabaseConnection<T> setActiveDatabaseProfileName(String profileName) {
		if (activeDatabaseProfileName == null || "".equals(activeDatabaseProfileName)) {
			authenticate(profileName);
			activeDatabaseProfileName = profileName;
			activeDatabaseProfile = databaseManager.getDatabaseProfile(appName, profileName);
			lastRequestAccessTimeInMs = Calendar.getInstance().getTimeInMillis();

		} else if (!activeDatabaseProfileName.equals(profileName)) {
			if (databaseManager == null) {
				disconnect();
			}
			authenticate(profileName);
			activeDatabaseProfileName = profileName;
			activeDatabaseProfile = databaseManager.getDatabaseProfile(appName, profileName);
			lastRequestAccessTimeInMs = Calendar.getInstance().getTimeInMillis();
		}

		return connection;
	}

	protected void authenticate(String profileName) {
		if (databaseManager != null) {
			DatabaseConnectionProvider<T> connProvider = databaseManager.getDatabaseConnectionProvider(appName,
					targetServerName, profileName);
			connection = connProvider.connect(databaseManager.getDatabaseProfile(appName, profileName));
		} else {
			connection = connect(databaseManager.getDatabaseProfile(appName, profileName));
		}
	}

	public String getActiveProfileName() {
		return activeDatabaseProfileName;
	}

	protected boolean isSessionExpired() {
		long currTimeInMs = Calendar.getInstance().getTimeInMillis();
		long durationInSeconds = (currTimeInMs - lastRequestAccessTimeInMs) / 1000;
		if (durationInSeconds >= sessionExpiryDurationInSeconds) {
			return true;
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public AbstractDatabaseActionHandler<Object> clone() {
		try {
			return (AbstractDatabaseActionHandler) this.getClass().getDeclaredConstructors()[0].newInstance();
		} catch (Exception ex) {
			Assert.fail("Failed to clone '" + this.getClass().getName() + "' class object.", ex);
		}
		return null;
	}

	public String getDataAsJsonString(String entityName, String searchStatement) {
		if (isSessionExpired()) {
			disconnect();
			setActiveDatabaseProfileName(activeDatabaseProfileName);
		} else {
			lastRequestAccessTimeInMs = Calendar.getInstance().getTimeInMillis();
		}

		return getDataAsJsonString(connection, entityName, searchStatement);
	}
	
	public DocumentContext getDataAsJsonDocument(String entityName, String searchStatement) {
		String jsonData = getDataAsJsonString(entityName, searchStatement);
		JsonDocumentReader reader = new JsonDocumentReader(jsonData);
		
		return reader.getDocumentContext();
	}

	public void updateData(String entityName, String updateStatement) {
		if (isSessionExpired()) {
			disconnect();
			setActiveDatabaseProfileName(activeDatabaseProfileName);
		} else {
			lastRequestAccessTimeInMs = Calendar.getInstance().getTimeInMillis();
		}

		updateData(connection, entityName, updateStatement);
	}

	public void deleteData(String entityName, String deleteStatement) {
		if (isSessionExpired()) {
			disconnect();
			setActiveDatabaseProfileName(activeDatabaseProfileName);
		} else {
			lastRequestAccessTimeInMs = Calendar.getInstance().getTimeInMillis();
		}

		deleteData(connection, entityName, deleteStatement);
	}

	public void insertData(String entityName, String insertStatement) {
		if (isSessionExpired()) {
			disconnect();
			setActiveDatabaseProfileName(activeDatabaseProfileName);
		} else {
			lastRequestAccessTimeInMs = Calendar.getInstance().getTimeInMillis();
		}

		insertData(connection, entityName, insertStatement);
	}

	public void insertDataInBatch(String entityName, List<String> insertStatements) {
		if (isSessionExpired()) {
			disconnect();
			setActiveDatabaseProfileName(activeDatabaseProfileName);
		} else {
			lastRequestAccessTimeInMs = Calendar.getInstance().getTimeInMillis();
		}

		insertDataInBatch(connection, entityName, insertStatements);
	}

	public void create(String entityName, String createStatement) {
		if (isSessionExpired()) {
			disconnect();
			setActiveDatabaseProfileName(activeDatabaseProfileName);
		} else {
			lastRequestAccessTimeInMs = Calendar.getInstance().getTimeInMillis();
		}

		create(connection, entityName, createStatement);
	}

	public void drop(String entityName, String dropStatement) {
		if (isSessionExpired()) {
			disconnect();
			setActiveDatabaseProfileName(activeDatabaseProfileName);
		} else {
			lastRequestAccessTimeInMs = Calendar.getInstance().getTimeInMillis();
		}

		drop(connection, entityName, dropStatement);
	}
	
	@Override
	public void disconnect() {
		disconnect(connection);
	}

	protected abstract void disconnect(DatabaseConnection<T> connection);
	
	protected abstract String getDataAsJsonString(DatabaseConnection<T> connection, String entityName,
			String searchStatement);

	protected abstract void updateData(DatabaseConnection<T> connection, String entityName, String updateStatement);

	protected abstract void deleteData(DatabaseConnection<T> connection, String entityName, String deleteStatement);

	protected abstract void insertData(DatabaseConnection<T> connection, String entityName, String insertStatement);

	protected abstract void insertDataInBatch(DatabaseConnection<T> connection, String entityName,
			List<String> insertStatements);

	protected abstract void create(DatabaseConnection<T> connection, String entityName, String createStatement);

	protected abstract void drop(DatabaseConnection<T> connection, String entityName, String dropStatement);
}