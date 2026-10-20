package dev.blunt;

public class JobQueue {

    public static String encode(Job job) {
        String encodedString = job.id() + "\t" + job.type() + "\t" + job.payload();
        return encodedString;
    }

    public static Job decode(String encodedString) {
        String[] decodedString = encodedString.split("\t", 3);
        Job job = new Job(decodedString[0], decodedString[1], decodedString[2]);
        return job;
    }
}
