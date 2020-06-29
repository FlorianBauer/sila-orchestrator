package de.fau.clients.orchestrator.tasks;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A abstract class to enable serialization of task-models to JSON. If a new task-model has to be
 * added, simply extend from this class and ensure all containing properties can be handled properly
 * by jackson (e.g. by using annotations). Furthermore a new <code>@JsonSubTypes.Type</code>
 * annotation with the new introduced subclass has to be added to the <code>@JsonSubTypes</code>
 * list below. Also the import routines in the <code>TaskQueueData</code> class may require
 * adjustments.
 *
 * @see TaskQueueData
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CommandTaskModel.class, name = "command"),
    @JsonSubTypes.Type(value = DelayTaskModel.class, name = "delay")})
public abstract class TaskModel {
}
