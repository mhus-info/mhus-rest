package de.mhus.rest.core;

import org.apache.shiro.subject.Subject;

import de.mhus.rest.core.api.Node;

public interface RestAuthorisation {

    Subject authorize(RestRegistry restRegistry, String name, Class<? extends Node> lastNode, CallContext context);

}
