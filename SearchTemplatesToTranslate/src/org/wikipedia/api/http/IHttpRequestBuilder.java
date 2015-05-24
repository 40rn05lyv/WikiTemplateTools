package org.wikipedia.api.http;

import com.mashape.unirest.request.HttpRequest;

interface IHttpRequestBuilder {
    
    public HttpRequest build(); 

}
