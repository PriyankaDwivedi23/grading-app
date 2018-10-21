package com.RitCapstone.GradingApp.dao;

import java.io.File;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.RitCapstone.GradingApp.mongo.MongoFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@Repository
@Transactional
public class SubmissionDAOImpl implements SubmissionDAO {

	private static Logger log = Logger.getLogger(SubmissionDAOImpl.class);
	private static String log_prepend = String.format("[%s]", "SubmissionDAOImpl");

	@Override
	public File getSubmission(String homework, String username, String question) {

//		String collectionName = homework;
//		String databaseName = MongoFactory.getDatabaseName();
//
//		MongoCollection<Document> collection = MongoFactory.getCollection(databaseName, collectionName);
//
//		BasicDBObject searchQuery = new BasicDBObject();
//		searchQuery.put("username", username);
//		searchQuery.put("question", question);
//
//		FindIterable<Document> findIterable = collection.find(searchQuery);
//		MongoCursor<Document> cursor = findIterable.iterator();
//
//		Document doc;
//		if (cursor.hasNext()) {
//			doc = cursor.next();
//
//			return doc.get("file", File.class);
//
//		} else {
//			log.warn(String.format("%s No submission found: Homework (%s), username (%s), question (%s)", log_prepend,
//					homework, username, question));
//			return null;
//		}
		return null;

	}

	/**
	 * Method to create a submission
	 * 
	 * If a submission is already created, update the submission. Otherwise, 2
	 * records of same submission exists in the database
	 */
	@Override
	public boolean createSubmission(String homework, String username, String question, String zipPath,
			String zipFileName) {
		log.info(String.format("%s Creating new Submission, Homework (%s), username (%s), question (%s)", log_prepend,
				homework, username, question));

		String collectionName = homework;
		String databaseName = MongoFactory.getDatabaseName();

		try {
			MongoCollection<Document> collection = MongoFactory.getCollection(databaseName, collectionName);

			Document doc = new Document();
			doc.put("username", username);
			doc.put("question", question);
			doc.put("path", zipPath);
			doc.put("fileName", zipFileName);

			// save submission to MongoDB
			collection.insertOne(doc);
			return true;

		} catch (Exception e) {
			log.error(e.getMessage());
			log.info(String.format(
					"%s Error while creating new submission for Homework (%s), username (%s), question (%s)",
					log_prepend, homework, username, question));
			return false;
		}

	}

	/**
	 * Method to update a submission
	 * 
	 * If the submission is not created and the method is called, there is no effect
	 * because searchQuery return empty
	 */
	@Override
	public boolean updateSubmission(String homework, String username, String question, String zipPath,
			String zipFileName) {
		log.info(String.format("%s Updating Submission, Homework (%s), username (%s), question (%s)", log_prepend,
				homework, username, question));

		String collectionName = homework;
		String databaseName = MongoFactory.getDatabaseName();

		try {

			MongoCollection<Document> collection = MongoFactory.getCollection(databaseName, collectionName);

			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("username", username);
			searchQuery.put("question", question);

			BasicDBObject newDocument = new BasicDBObject();
			newDocument.put("fileName", zipFileName);
			newDocument.put("path", zipPath);

			BasicDBObject updateObject = new BasicDBObject();
			updateObject.put("$set", newDocument);

			// update submission to MongoDB
			collection.updateOne(searchQuery, updateObject);

			return true;

		} catch (Exception e) {
			log.error(e.getMessage());
			log.error(
					String.format("%s Error while updating submission for Homework (%s), username (%s), question (%s)",
							log_prepend, homework, username, question));
			return false;
		}
	}

}