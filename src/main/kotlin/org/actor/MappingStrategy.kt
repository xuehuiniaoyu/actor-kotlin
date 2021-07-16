package org.actor

interface MappingStrategy {
    fun onMapping(from: Any, expectedType: Class<*>): Any?
}