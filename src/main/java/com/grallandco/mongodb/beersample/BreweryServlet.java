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

import com.google.gson.Gson;
import com.mongodb.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The BreweryServlet handles all Brewery-related HTTP Queries.
 *
 * The BreweryServlet is used to handle all HTTP queries under the /breweries
 * namespace. The "web.xml" defines a wildcard route for every /breweries/*
 * route, so the doGet() method needs to determine what should be dispatched.
 */
public class BreweryServlet extends HttpServlet {

  /**
   * Obtains the current MongoDB connection.
   */
  final DB db = ConnectionManager.getInstance();

  /**
   * Google GSON is used for JSON encoding/decoding.
   */
  final Gson gson = new Gson();

  /**
   * Dispatch all incoming GET HTTP requests.
   *
   * Since the /breweries/* routes are wildcarded and will all end up here, the
   * method needs to check agains the PATH (through getPathInfo()) and
   * determine which helper method should be called. The helper method then
   * does the actual request and response handling.
   *
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws javax.servlet.ServletException
   * @throws java.io.IOException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
      if(request.getPathInfo() == null) {
        handleIndex(request, response);
      } else if(request.getPathInfo().startsWith("/show")) {
        handleShow(request, response);
      } else if(request.getPathInfo().startsWith("/delete")) {
        handleDelete(request, response);
      } else if(request.getPathInfo().startsWith("/search")) {
        handleSearch(request, response);
      }
    } catch (InterruptedException ex) {
      Logger.getLogger(BreweryServlet.class.getName()).log(
        Level.SEVERE, null, ex);
    } catch (ExecutionException ex) {
      Logger.getLogger(BreweryServlet.class.getName()).log(
        Level.SEVERE, null, ex);
    }
  }

  /**
   * Handle the /breweries action.
   *
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws java.io.IOException
   * @throws javax.servlet.ServletException
   */
  private void handleIndex(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {


      DBCursor cursor = db.getCollection("brewery").find()
              .sort( BasicDBObjectBuilder.start("name", 1).get() )
              .limit(20);
      ArrayList<HashMap<String, String>> breweries   =
              new ArrayList<HashMap<String, String>>();
      while (cursor.hasNext()) {
          DBObject row = cursor.next();
          HashMap<String, String> brewery = new HashMap<String, String>();
          brewery.put("id", (String)row.get("_id"));
          brewery.put("name", (String)row.get("name"));
          breweries.add(brewery);
      }
    request.setAttribute("breweries", breweries);

    request.getRequestDispatcher("/WEB-INF/breweries/index.jsp")
      .forward(request, response);
  }

  /**
   * Handle the /breweries/show/<BREWERY-ID> action
   *
   * This method loads up a document based on the given brewery id.
   *
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws java.io.IOException
   * @throws javax.servlet.ServletException
   */
  private void handleShow(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {

    String breweryId = request.getPathInfo().split("/")[2];
    DBObject document = db.getCollection("brewery").findOne(breweryId);
    if(document != null) {
      HashMap<String, String> brewery = (HashMap<String,String>)document;
      request.setAttribute("brewery", brewery);
    }

    request.getRequestDispatcher("/WEB-INF/breweries/show.jsp")
      .forward(request, response);
  }

  /**
   * Handle the /breweries/delete/<BREWERY-ID> action
   *
   * This method deletes a brewery based on the given brewery id.
   *
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws java.io.IOException
   * @throws javax.servlet.ServletException
   * @throws InterruptedException
   * @throws java.util.concurrent.ExecutionException
   */
  private void handleDelete(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException,
    InterruptedException,
    ExecutionException {

    String breweryId = request.getPathInfo().split("/")[2];
      db.getCollection("brewery").remove( BasicDBObjectBuilder.start("_id",breweryId).get() );
      response.sendRedirect("/breweries");
  }

  /**
   * Handle the /breweries/search action.
   *
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws java.io.IOException
   * @throws javax.servlet.ServletException
   */
  private void handleSearch(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {

      String name = request.getParameter("value").toLowerCase();

      DBObject query = QueryBuilder.start()
              .put("name")
              .regex( java.util.regex.Pattern.compile(name, Pattern.CASE_INSENSITIVE) )
              .get();

      DBCursor cursor = db.getCollection("brewery").find(query)
              .sort( BasicDBObjectBuilder.start("name",1).get() )
              .limit(20);
      ArrayList<HashMap<String, String>> breweries   =
              new ArrayList<HashMap<String, String>>();
      while (cursor.hasNext()) {
          DBObject row = cursor.next();
          HashMap<String, String> brewery = new HashMap<String, String>();
          brewery.put("id", (String)row.get("_id"));
          brewery.put("name", (String)row.get("name"));
          breweries.add(brewery);
      }

    response.setContentType("application/json");
    PrintWriter out = response.getWriter();out.print(gson.toJson(breweries));
    out.flush();
  }

}
