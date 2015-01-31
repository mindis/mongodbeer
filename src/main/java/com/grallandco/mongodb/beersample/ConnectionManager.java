/**
 * Copyright (C) 2009-2012 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */


package com.grallandco.mongodb.beersample;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ConnectionManager handles connecting, disconnecting and managing
 * of the MongoDB connection.
 *
 * To get the connection from a Servlet context, use the getInstance()
 * method.
 */
public class ConnectionManager implements ServletContextListener {

  /**
   * Holds the connected MongoDB instance.
   */
  private static DB db;

  /**
   * The Logger to use.
   */
  private static final Logger logger = Logger.getLogger(
    ConnectionManager.class.getName());

  /**
   * Connect to MongoDB when the Server starts.
   *
   * @param sce the ServletContextEvent (not used here).
   */
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
        db = new MongoClient().getDB("beers");
    } catch (IOException ex) {
      logger.log(Level.SEVERE, ex.getMessage());
    }
  }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }


    /**
   * Returns the current MongoDB DB object.
   *
   * @return the current MongoDB DB object.
   */
  public static DB getInstance() {
    return db;
  }

}
