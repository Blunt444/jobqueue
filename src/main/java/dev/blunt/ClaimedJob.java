package dev.blunt;

public record ClaimedJob(Job job, long fencingToken) {

}
