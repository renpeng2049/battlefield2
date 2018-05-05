package com.soyoung.battle.field.rest;

public abstract class AbstractRestChannel implements RestChannel {

    protected final RestRequest request;
    protected final boolean detailedErrorsEnabled;

    protected AbstractRestChannel(RestRequest request, boolean detailedErrorsEnabled) {
        this.request = request;
        this.detailedErrorsEnabled = detailedErrorsEnabled;

    }

    @Override
    public RestRequest request() {
        return this.request;
    }

    @Override
    public boolean detailedErrorsEnabled() {
        return detailedErrorsEnabled;
    }
}
