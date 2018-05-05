package com.soyoung.battle.field.rest;

public interface RestChannel {

    RestRequest request();

    /**
     * @return true iff an error response should contain additional details like exception traces.
     */
    boolean detailedErrorsEnabled();

    void sendResponse(RestResponse response);
}
