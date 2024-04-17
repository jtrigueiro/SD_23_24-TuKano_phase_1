package tukano.servers.rest;

import java.util.List;

import jakarta.inject.Singleton;

import tukano.servers.java.ShortsServer;
import tukano.api.rest.RestShorts;
import tukano.api.java.Shorts;
import tukano.api.Short;

@Singleton
public class RestShortsResource extends RestResource implements RestShorts {

    final Shorts impl;

    public RestShortsResource() {
        this.impl = new ShortsServer();
    }

    @Override
    public Short createShort(String userId, String password) {
        return resultOrThrow(impl.createShort(userId, password));
    }

    @Override
    public void deleteShort(String shortId, String password) {
        resultOrThrow(impl.deleteShort(shortId, password));
    }

    @Override
    public Short getShort(String shortId) {
        return resultOrThrow(impl.getShort(shortId));
    }

    @Override
    public List<String> getShorts(String userId) {
        return resultOrThrow(impl.getShorts(userId));
    }

    @Override
    public void follow(String userId1, String userId2, boolean isFollowing, String password) {
        resultOrThrow(impl.follow(userId1, userId2, isFollowing, password));
    }

    @Override
    public List<String> followers(String userId, String password) {
        return resultOrThrow(impl.followers(userId, password));
    }

    @Override
    public void like(String shortId, String userId, boolean isLiked, String password) {
        resultOrThrow(impl.like(shortId, userId, isLiked, password));
    }

    @Override
    public List<String> likes(String shortId, String password) {
        return resultOrThrow(impl.likes(shortId, password));
    }

    @Override
    public List<String> getFeed(String userId, String password) {
        return resultOrThrow(impl.getFeed(userId, password));
    }

    @Override
    public void deleteUserShorts(String userId) {
        resultOrThrow(impl.deleteUserShorts(userId));
    }

}
