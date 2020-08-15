package de.mhus.rest.core;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import de.mhus.rest.core.api.Node;

public class PublicRestAuthenticator implements RestAuthorisation {

    @Override
    public Subject authorize(
            RestRegistry restRegistry,
            String name,
            Class<? extends Node> lastNode,
            CallContext context) {
        Subject subject = SecurityUtils.getSubject();
        return subject;
    }
}
