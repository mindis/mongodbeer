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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The BeerServlet handles all Beer-related HTTP Queries.
 *
 * The BeerServlet is used to handle all HTTP queries under the /beers
 * namespace. The "web.xml" defines a wildcard route for every /beers/*
 * route, so the doGet() method needs to determine what should be dispatched.
 */
public class BeerServlet extends HttpServlet {

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
   * Since the /beers/* routes are wildcarded and will all end up here, the
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
      } else if(request.getPathInfo().startsWith("/edit")) {
        handleEdit(request, response);
      } else if(request.getPathInfo().startsWith("/search")) {
        handleSearch(request, response);
      }
    } catch (InterruptedException ex) {
      Logger.getLogger(BeerServlet.class.getName()).log(
        Level.SEVERE, null, ex);
    } catch (ExecutionException ex) {
      Logger.getLogger(BeerServlet.class.getName()).log(
        Level.SEVERE, null, ex);
    }
  }

  /**
   * Store and validate the beer form.
   *
   * @param request
   * @param response
   * @throws javax.servlet.ServletException
   * @throws java.io.IOException
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    String beerId = request.getPathInfo().split("/")[2];
    HashMap<String, String> beer = beer = new HashMap<String, String>();
    Enumeration<String> params = request.getParameterNames();
    while(params.hasMoreElements()) {
      String key = params.nextElement();
      if(!key.startsWith("beer_")) {
        continue;
      }
      String value = request.getParameter(key);
      beer.put(key.substring(5), value);
    }

    beer.put("type", "beer");
    beer.put("updated", new Date().toString());

      beer.put("_id", beerId); // adding the id in the document (since it is removed by the app logic)
      db.getCollection("beer").save(new BasicDBObject(beer));

    //db.set(beerId, 0, gson.toJson(beer));
    response.sendRedirect("/beers/show/" + beerId);
  }


  /**
   * Handle the /beers action.
   *
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws java.io.IOException
   * @throws javax.servlet.ServletException
   */
  private void handleIndex(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {

      DBCursor cursor = db.getCollection("beer").find()
                                                    .sort( BasicDBObjectBuilder.start("name",1).get() )
                                                    .limit(20);
      ArrayList<HashMap<String, String>> beers =
              new ArrayList<HashMap<String, String>>();
      while (cursor.hasNext()) {
          DBObject row = cursor.next();
          HashMap<String, String> beer = new HashMap<String, String>();
          beer.put("id", (String)row.get("_id"));
          beer.put("name", (String)row.get("name"));
          beer.put("brewery", (String)row.get("brewery_id"));
          beers.add(beer);
      }

    request.setAttribute("beers", beers);

    request.getRequestDispatcher("/WEB-INF/beers/index.jsp")
      .forward(request, response);
  }

  /**
   * Handle the /beers/show/<BEER-ID> action
   *
   * This method loads up a document based on the given beer id.
   *
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws java.io.IOException
   * @throws javax.servlet.ServletException
   */
  private void handleShow(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {

    String beerId = request.getPathInfo().split("/")[2];
    DBObject document = db.getCollection("beer").findOne(beerId);
    if(document != null) {
      HashMap<String, String> beer = (HashMap<String,String>)document;   //gson.fromJson(document, HashMap.class);
      request.setAttribute("beer", beer);
    }

    request.getRequestDispatcher("/WEB-INF/beers/show.jsp")
      .forward(request, response);
  }

  /**
   * Handle the /beers/delete/<BEER-ID> action
   *
   * This method deletes a beer based on the given beer id.
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

    String beerId = request.getPathInfo().split("/")[2];
    db.getCollection("beer").remove( BasicDBObjectBuilder.start("_id",beerId).get() );
    response.sendRedirect("/beers");
  }

  private void handleEdit(HttpServletRequest request,
    HttpServletResponse response) throws ServletException, IOException {

    String[] beerId = request.getPathInfo().split("/");
    if(beerId.length > 2) {
      DBObject document = db.getCollection("beer").findOne(beerId[2]);


        HashMap<String, String> beer = null;
      if(document != null) {
        beer =  (HashMap<String,String>)document;
        beer.put("id", beerId[2]);
        request.setAttribute("beer", beer);
      }
      request.setAttribute("title", "Modify Beer \"" + beer.get("name") + "\"");
    } else {
      request.setAttribute("title", "Create a new beer");
    }

    request.getRequestDispatcher("/WEB-INF/beers/edit.jsp")
      .forward(request, response);
  }

  /**
   * Handle the /beers/search action.
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

      DBCursor cursor = db.getCollection("beer").find(query)
              .sort( BasicDBObjectBuilder.start("name",1).get() )
              .limit(20);
      ArrayList<HashMap<String, String>> beers =
              new ArrayList<HashMap<String, String>>();
      while (cursor.hasNext()) {
          DBObject row = cursor.next();
          HashMap<String, String> beer = new HashMap<String, String>();
          beer.put("id", (String)row.get("_id"));
          beer.put("name", (String)row.get("name"));
          beer.put("brewery", (String)row.get("brewery_id"));
          beers.add(beer);
      }

    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    out.print(gson.toJson(beers));
    out.flush();
  }

}
