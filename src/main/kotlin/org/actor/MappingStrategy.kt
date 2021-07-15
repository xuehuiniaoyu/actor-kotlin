package org.actor

interface MappingStrategy {
    fun onMapping(from: Any): Any?
}