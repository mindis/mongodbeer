<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<t:layout>
    <jsp:body>
        <div class="span6">
          <div class="span12">
            <h4>Browse all Beers</h4>
            <a href="/beers" class="btn btn-warning">Show me all beers</a>
            <hr />
          </div>
          <div class="span12">
            <h4>Browse all Breweries</h4>
            <a href="/breweries" class="btn btn-info">Take me to the breweries</a>
          </div>
        </div>
        <div class="span6">
          <div class="span12">
            <h4>About this App</h4>
              <blockquote class="twitter-tweet" lang="en"><p>Moving my Java from Couchbase to MongoDB <a href="http://t.co/Wnn3pXfMGi">pic.twitter.com/Wnn3pXfMGi</a></p>&mdash; Tugdual Grall (@tgrall) <a href="https://twitter.com/tgrall/status/559664540041117696">January 26, 2015</a></blockquote>
              <script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>


          </div>
        </div>
    </jsp:body>
</t:layout>